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

package io.element.android.features.ftue.impl.state

import android.Manifest
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.ftue.api.state.FtueState
import io.element.android.features.lockscreen.api.LockScreenService
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.permissions.api.PermissionStateProvider
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultFtueState @Inject constructor(
    private val sdkVersionProvider: BuildVersionSdkIntProvider,
    coroutineScope: CoroutineScope,
    private val analyticsService: AnalyticsService,
    private val permissionStateProvider: PermissionStateProvider,
    private val lockScreenService: LockScreenService,
    private val sessionVerificationService: SessionVerificationService,
) : FtueState {
    override val shouldDisplayFlow = MutableStateFlow(isSessionVerificationServiceReady() && isAnyStepIncomplete())

    override suspend fun reset() {
        analyticsService.reset()
        if (sdkVersionProvider.isAtLeast(Build.VERSION_CODES.TIRAMISU)) {
            permissionStateProvider.resetPermission(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    init {
        sessionVerificationService.sessionVerifiedStatus
            .onEach { updateState() }
            .launchIn(coroutineScope)

        analyticsService.didAskUserConsent()
            .onEach { updateState() }
            .launchIn(coroutineScope)
    }

    fun getNextStep(currentStep: FtueStep? = null): FtueStep? =
        when (currentStep) {
            null -> if (isSessionNotVerified()) {
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

    private fun isAnyStepIncomplete(): Boolean {
        return listOf(
            { isSessionNotVerified() },
            { shouldAskNotificationPermissions() },
            { needsAnalyticsOptIn() },
            { shouldDisplayLockscreenSetup() },
        ).any { it() }
    }

    private fun isSessionVerificationServiceReady(): Boolean {
        return sessionVerificationService.sessionVerifiedStatus.value != SessionVerifiedStatus.Unknown
    }

    private fun isSessionNotVerified(): Boolean {
        return sessionVerificationService.sessionVerifiedStatus.value == SessionVerifiedStatus.NotVerified
    }

    private fun needsAnalyticsOptIn(): Boolean {
        // We need this function to not be suspend, so we need to load the value through runBlocking
        return runBlocking { analyticsService.didAskUserConsent().first().not() }
    }

    private fun shouldAskNotificationPermissions(): Boolean {
        return if (sdkVersionProvider.isAtLeast(Build.VERSION_CODES.TIRAMISU)) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val isPermissionDenied = runBlocking { permissionStateProvider.isPermissionDenied(permission).first() }
            val isPermissionGranted = permissionStateProvider.isPermissionGranted(permission)
            !isPermissionGranted && !isPermissionDenied
        } else {
            false
        }
    }

    private fun shouldDisplayLockscreenSetup(): Boolean {
        return runBlocking {
            lockScreenService.isSetupRequired().first()
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun updateState() {
        shouldDisplayFlow.value = isSessionVerificationServiceReady() && isAnyStepIncomplete()
    }
}

sealed interface FtueStep {
    data object SessionVerification : FtueStep
    data object NotificationsOptIn : FtueStep
    data object AnalyticsOptIn : FtueStep
    data object LockscreenSetup : FtueStep
}
