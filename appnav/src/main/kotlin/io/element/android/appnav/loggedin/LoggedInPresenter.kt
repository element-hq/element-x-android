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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import im.vector.app.features.analytics.plan.CryptoSessionStateChange
import im.vector.app.features.analytics.plan.SuperProperties
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.SdkMetadata
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.push.api.PushService
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class LoggedInPresenter @Inject constructor(
    private val matrixClient: MatrixClient,
    private val networkMonitor: NetworkMonitor,
    private val pushService: PushService,
    private val sessionVerificationService: SessionVerificationService,
    private val analyticsService: AnalyticsService,
    private val encryptionService: EncryptionService,
    private val sdkMetadata: SdkMetadata,
) : Presenter<LoggedInState> {
    @Composable
    override fun present(): LoggedInState {
        val isVerified by remember {
            sessionVerificationService.sessionVerifiedStatus.map { it == SessionVerifiedStatus.Verified }
        }.collectAsState(initial = false)

        LaunchedEffect(isVerified) {
            if (isVerified) {
                // Ensure pusher is registered
                val currentPushProvider = pushService.getCurrentPushProvider()
                val result = if (currentPushProvider == null) {
                    // Register with the first available push provider
                    val pushProvider = pushService.getAvailablePushProviders().firstOrNull() ?: return@LaunchedEffect
                    val distributor = pushProvider.getDistributors().firstOrNull() ?: return@LaunchedEffect
                    pushService.registerWith(matrixClient, pushProvider, distributor)
                } else {
                    val currentPushDistributor = currentPushProvider.getCurrentDistributor(matrixClient)
                    if (currentPushDistributor == null) {
                        // Register with the first available distributor
                        val distributor = currentPushProvider.getDistributors().firstOrNull() ?: return@LaunchedEffect
                        pushService.registerWith(matrixClient, currentPushProvider, distributor)
                    } else {
                        // Re-register with the current distributor
                        pushService.registerWith(matrixClient, currentPushProvider, currentPushDistributor)
                    }
                }
                result.onFailure {
                    Timber.e(it, "Failed to register pusher")
                }
            }
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

        LaunchedEffect(Unit) {
            analyticsService.updateSuperProperties(
                SuperProperties(
                    cryptoSDK = SuperProperties.CryptoSDK.Rust,
                    appPlatform = SuperProperties.AppPlatform.EXA,
                    cryptoSDKVersion = sdkMetadata.sdkGitSha,
                )
            )
        }

        return LoggedInState(
            showSyncSpinner = showSyncSpinner,
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
