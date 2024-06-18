/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.appnav.loggedin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import im.vector.app.features.analytics.plan.CryptoSessionStateChange
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.pushproviders.api.RegistrationFailure
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private val pusherTag = LoggerTag("Pusher", LoggerTag.PushLoggerTag)

class LoggedInPresenter @Inject constructor(
    private val matrixClient: MatrixClient,
    private val networkMonitor: NetworkMonitor,
    private val pushService: PushService,
    private val sessionVerificationService: SessionVerificationService,
    private val analyticsService: AnalyticsService,
    private val encryptionService: EncryptionService,
) : Presenter<LoggedInState> {
    @Composable
    override fun present(): LoggedInState {
        val coroutineScope = rememberCoroutineScope()
        val ignoreRegistrationError by remember {
            pushService.ignoreRegistrationError(matrixClient.sessionId)
        }.collectAsState(initial = false)
        val pusherRegistrationState = remember<MutableState<AsyncData<Unit>>> { mutableStateOf(AsyncData.Uninitialized) }
        LaunchedEffect(Unit) {
            sessionVerificationService.sessionVerifiedStatus
                .onEach { sessionVerifiedStatus ->
                    when (sessionVerifiedStatus) {
                        SessionVerifiedStatus.Unknown -> Unit
                        SessionVerifiedStatus.Verified -> {
                            ensurePusherIsRegistered(pusherRegistrationState)
                        }
                        SessionVerifiedStatus.NotVerified -> {
                            pusherRegistrationState.value = AsyncData.Failure(PusherRegistrationFailure.AccountNotVerified())
                        }
                    }
                }
                .launchIn(this)
        }
        val syncIndicator by matrixClient.roomListService.syncIndicator.collectAsState()
        val networkStatus by networkMonitor.connectivity.collectAsState()
        val showSyncSpinner by remember {
            derivedStateOf {
                networkStatus == NetworkStatus.Online && syncIndicator == RoomListService.SyncIndicator.Show
            }
        }
        val verificationState by sessionVerificationService.sessionVerifiedStatus.collectAsState()
        val recoveryState by encryptionService.recoveryStateStateFlow.collectAsState()
        LaunchedEffect(verificationState, recoveryState) {
            reportCryptoStatusToAnalytics(verificationState, recoveryState)
        }

        fun handleEvent(event: LoggedInEvents) {
            when (event) {
                is LoggedInEvents.CloseErrorDialog -> {
                    pusherRegistrationState.value = AsyncData.Uninitialized
                    if (event.doNotShowAgain) {
                        coroutineScope.launch {
                            pushService.setIgnoreRegistrationError(matrixClient.sessionId, true)
                        }
                    }
                }
            }
        }

        return LoggedInState(
            showSyncSpinner = showSyncSpinner,
            pusherRegistrationState = pusherRegistrationState.value,
            ignoreRegistrationError = ignoreRegistrationError,
            eventSink = ::handleEvent
        )
    }

    private suspend fun ensurePusherIsRegistered(pusherRegistrationState: MutableState<AsyncData<Unit>>) {
        Timber.tag(pusherTag.value).d("Ensure pusher is registered")
        val currentPushProvider = pushService.getCurrentPushProvider()
        val result = if (currentPushProvider == null) {
            Timber.tag(pusherTag.value).d("Register with the first available push provider with at least one distributor")
            val pushProvider = pushService.getAvailablePushProviders()
                .firstOrNull { it.getDistributors().isNotEmpty() }
            // Else fallback to the first available push provider (the list should never be empty)
                ?: pushService.getAvailablePushProviders().firstOrNull()
                ?: return Unit
                    .also { Timber.tag(pusherTag.value).w("No push providers available") }
                    .also { pusherRegistrationState.value = AsyncData.Failure(PusherRegistrationFailure.NoProvidersAvailable()) }
            val distributor = pushProvider.getDistributors().firstOrNull()
                ?: return Unit
                    .also { Timber.tag(pusherTag.value).w("No distributors available") }
                    .also {
                        // In this case, consider the push provider is chosen.
                        pushService.selectPushProvider(matrixClient, pushProvider)
                    }
                    .also { pusherRegistrationState.value = AsyncData.Failure(PusherRegistrationFailure.NoDistributorsAvailable()) }
            pushService.registerWith(matrixClient, pushProvider, distributor)
        } else {
            val currentPushDistributor = currentPushProvider.getCurrentDistributor(matrixClient)
            if (currentPushDistributor == null) {
                Timber.tag(pusherTag.value).d("Register with the first available distributor")
                val distributor = currentPushProvider.getDistributors().firstOrNull()
                    ?: return Unit
                        .also { Timber.tag(pusherTag.value).w("No distributors available") }
                        .also { pusherRegistrationState.value = AsyncData.Failure(PusherRegistrationFailure.NoDistributorsAvailable()) }
                pushService.registerWith(matrixClient, currentPushProvider, distributor)
            } else {
                Timber.tag(pusherTag.value).d("Re-register with the current distributor")
                pushService.registerWith(matrixClient, currentPushProvider, currentPushDistributor)
            }
        }
        result.fold(
            onSuccess = {
                Timber.tag(pusherTag.value).d("Pusher registered")
                pusherRegistrationState.value = AsyncData.Success(Unit)
            },
            onFailure = {
                Timber.tag(pusherTag.value).e(it, "Failed to register pusher")
                if (it is RegistrationFailure) {
                    pusherRegistrationState.value = AsyncData.Failure(
                        PusherRegistrationFailure.RegistrationFailure(it.clientException, it.isRegisteringAgain)
                    )
                } else {
                    pusherRegistrationState.value = AsyncData.Failure(it)
                }
            }
        )
    }

    private fun reportCryptoStatusToAnalytics(verificationState: SessionVerifiedStatus, recoveryState: RecoveryState) {
        // Update first the user property, to store the current status for that posthog user
        val userVerificationState = verificationState.toAnalyticsUserPropertyValue()
        val userRecoveryState = recoveryState.toAnalyticsUserPropertyValue()
        if (userRecoveryState != null && userVerificationState != null) {
            // we want to report when both value are known (if one is unknown we wait until we have them both)
            analyticsService.updateUserProperties(
                UserProperties(
                    verificationState = userVerificationState,
                    recoveryState = userRecoveryState
                )
            )
        }

        // Also report when there is a change in the state, to be able to track the changes
        val changeVerificationState = verificationState.toAnalyticsStateChangeValue()
        val changeRecoveryState = recoveryState.toAnalyticsStateChangeValue()
        if (changeVerificationState != null && changeRecoveryState != null) {
            analyticsService.capture(CryptoSessionStateChange(changeRecoveryState, changeVerificationState))
        }
    }
}
