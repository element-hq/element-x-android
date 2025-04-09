/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl.state

import android.Manifest
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.ftue.api.state.FtueService
import io.element.android.features.ftue.api.state.FtueState
import io.element.android.features.lockscreen.api.LockScreenService
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.permissions.api.PermissionStateProvider
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
@SingleIn(SessionScope::class)
class DefaultFtueService @Inject constructor(
    private val sdkVersionProvider: BuildVersionSdkIntProvider,
    @SessionCoroutineScope sessionCoroutineScope: CoroutineScope,
    private val analyticsService: AnalyticsService,
    private val permissionStateProvider: PermissionStateProvider,
    private val lockScreenService: LockScreenService,
    private val sessionVerificationService: SessionVerificationService,
    private val sessionPreferencesStore: SessionPreferencesStore,
) : FtueService {
    override val state = MutableStateFlow<FtueState>(FtueState.Unknown)

    /**
     * This flow emits true when the FTUE flow is ready to be displayed.
     * In this case, the FTUE flow is ready when the session verification status is known.
     */
    val isVerificationStatusKnown = sessionVerificationService.sessionVerifiedStatus
        .map { it != SessionVerifiedStatus.Unknown }
        .distinctUntilChanged()

    override suspend fun reset() {
        analyticsService.reset()
        if (sdkVersionProvider.isAtLeast(Build.VERSION_CODES.TIRAMISU)) {
            permissionStateProvider.resetPermission(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    init {
        sessionVerificationService.sessionVerifiedStatus
            .onEach { updateState() }
            .launchIn(sessionCoroutineScope)

        analyticsService.didAskUserConsentFlow
            .distinctUntilChanged()
            .onEach { updateState() }
            .launchIn(sessionCoroutineScope)
    }

    suspend fun getNextStep(currentStep: FtueStep? = null): FtueStep? =
        when (currentStep) {
            null -> if (!isSessionVerificationStateReady()) {
                FtueStep.WaitingForInitialState
            } else {
                getNextStep(FtueStep.WaitingForInitialState)
            }
            FtueStep.WaitingForInitialState -> if (isSessionNotVerified()) {
                FtueStep.SessionVerification
            } else {
                getNextStep(FtueStep.SessionVerification)
            }
            FtueStep.SessionVerification -> if (shouldAskNotificationPermissions()) {
                FtueStep.NotificationsOptIn
            } else {
                getNextStep(FtueStep.NotificationsOptIn)
            }
            FtueStep.NotificationsOptIn -> if (shouldDisplayLockscreenSetup()) {
                FtueStep.LockscreenSetup
            } else {
                getNextStep(FtueStep.LockscreenSetup)
            }
            FtueStep.LockscreenSetup -> if (needsAnalyticsOptIn()) {
                FtueStep.AnalyticsOptIn
            } else {
                getNextStep(FtueStep.AnalyticsOptIn)
            }
            FtueStep.AnalyticsOptIn -> null
        }

    private fun isSessionVerificationStateReady(): Boolean {
        return sessionVerificationService.sessionVerifiedStatus.value != SessionVerifiedStatus.Unknown
    }

    private suspend fun isSessionNotVerified(): Boolean {
        // Wait until the session verification status is known
        isVerificationStatusKnown.filter { it }.first()

        return sessionVerificationService.sessionVerifiedStatus.value == SessionVerifiedStatus.NotVerified && !canSkipVerification()
    }

    private suspend fun canSkipVerification(): Boolean {
        return sessionPreferencesStore.isSessionVerificationSkipped().first()
    }

    private suspend fun needsAnalyticsOptIn(): Boolean {
        return analyticsService.didAskUserConsentFlow.first().not()
    }

    private suspend fun shouldAskNotificationPermissions(): Boolean {
        return if (sdkVersionProvider.isAtLeast(Build.VERSION_CODES.TIRAMISU)) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val isPermissionDenied = permissionStateProvider.isPermissionDenied(permission).first()
            val isPermissionGranted = permissionStateProvider.isPermissionGranted(permission)
            !isPermissionGranted && !isPermissionDenied
        } else {
            false
        }
    }

    private suspend fun shouldDisplayLockscreenSetup(): Boolean {
        return lockScreenService.isSetupRequired().first()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun updateState() {
        val nextStep = getNextStep()
        state.value = when {
            // Final state, there aren't any more next steps
            nextStep == null -> FtueState.Complete
            else -> FtueState.Incomplete
        }
    }
}

sealed interface FtueStep {
    data object WaitingForInitialState : FtueStep
    data object SessionVerification : FtueStep
    data object NotificationsOptIn : FtueStep
    data object AnalyticsOptIn : FtueStep
    data object LockscreenSetup : FtueStep
}
