/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
import androidx.compose.runtime.setValue
import im.vector.app.features.analytics.plan.CryptoSessionStateChange
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.oidc.AccountManagementAction
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.sync.SlidingSyncVersion
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.pushproviders.api.RegistrationFailure
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private val pusherTag = LoggerTag("Pusher", LoggerTag.PushLoggerTag)

class LoggedInPresenter @Inject constructor(
    private val matrixClient: MatrixClient,
    private val syncService: SyncService,
    private val pushService: PushService,
    private val sessionVerificationService: SessionVerificationService,
    private val analyticsService: AnalyticsService,
    private val encryptionService: EncryptionService,
    private val buildMeta: BuildMeta,
) : Presenter<LoggedInState> {
    @Composable
    override fun present(): LoggedInState {
        val coroutineScope = rememberCoroutineScope()
        val ignoreRegistrationError by remember {
            pushService.ignoreRegistrationError(matrixClient.sessionId)
        }.collectAsState(initial = false)
        val pusherRegistrationState = remember<MutableState<AsyncData<Unit>>> { mutableStateOf(AsyncData.Uninitialized) }
        LaunchedEffect(Unit) { preloadAccountManagementUrl() }
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
        val isOnline by syncService.isOnline.collectAsState()
        val showSyncSpinner by remember {
            derivedStateOf {
                isOnline && syncIndicator == RoomListService.SyncIndicator.Show
            }
        }
        var forceNativeSlidingSyncMigration by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            combine(
                sessionVerificationService.sessionVerifiedStatus,
                encryptionService.recoveryStateStateFlow
            ) { verificationState, recoveryState ->
                reportCryptoStatusToAnalytics(verificationState, recoveryState)
            }.launchIn(this)
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
                LoggedInEvents.CheckSlidingSyncProxyAvailability -> coroutineScope.launch {
                    forceNativeSlidingSyncMigration = matrixClient.needsForcedNativeSlidingSyncMigration().getOrDefault(false)
                }
                LoggedInEvents.LogoutAndMigrateToNativeSlidingSync -> coroutineScope.launch {
                    // Force the logout since Native Sliding Sync is already enforced by the SDK
                    matrixClient.logout(userInitiated = true, ignoreSdkError = true)
                }
            }
        }

        return LoggedInState(
            showSyncSpinner = showSyncSpinner,
            pusherRegistrationState = pusherRegistrationState.value,
            ignoreRegistrationError = ignoreRegistrationError,
            forceNativeSlidingSyncMigration = forceNativeSlidingSyncMigration,
            appName = buildMeta.applicationName,
            eventSink = ::handleEvent
        )
    }

    // Force the user to log out if they were using the proxy sliding sync as it's no longer supported by the SDK
    private suspend fun MatrixClient.needsForcedNativeSlidingSyncMigration(): Result<Boolean> = runCatchingExceptions {
        val currentSlidingSyncVersion = currentSlidingSyncVersion().getOrThrow()
        currentSlidingSyncVersion == SlidingSyncVersion.Proxy
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
                        pushService.selectPushProvider(matrixClient.sessionId, pushProvider)
                    }
                    .also { pusherRegistrationState.value = AsyncData.Failure(PusherRegistrationFailure.NoDistributorsAvailable()) }
            pushService.registerWith(matrixClient, pushProvider, distributor)
        } else {
            val currentPushDistributor = currentPushProvider.getCurrentDistributor(matrixClient.sessionId)
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

    private fun CoroutineScope.preloadAccountManagementUrl() = launch {
        matrixClient.getAccountManagementUrl(AccountManagementAction.Profile)
        matrixClient.getAccountManagementUrl(AccountManagementAction.SessionsList)
    }
}
