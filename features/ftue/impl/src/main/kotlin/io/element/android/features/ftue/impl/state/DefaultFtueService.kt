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
import io.element.android.features.ftue.api.state.FtueService
import io.element.android.features.ftue.api.state.FtueState
import io.element.android.features.lockscreen.api.LockScreenService
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.permissions.api.PermissionStateProvider
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ContributesBinding(SessionScope::class)
class DefaultFtueService @Inject constructor(
    private val sdkVersionProvider: BuildVersionSdkIntProvider,
    coroutineScope: CoroutineScope,
    private val analyticsService: AnalyticsService,
    private val permissionStateProvider: PermissionStateProvider,
    private val lockScreenService: LockScreenService,
    private val sessionVerificationService: SessionVerificationService,
    private val sessionPreferencesStore: SessionPreferencesStore,
) : FtueService {
    override val state = MutableStateFlow<FtueState>(FtueState.Unknown)

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

    suspend fun getNextStep(currentStep: FtueStep? = null): FtueStep? =
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

    private suspend fun isAnyStepIncomplete(): Boolean {
        return listOf<suspend () -> Boolean>(
            { isSessionNotVerified() },
            { shouldAskNotificationPermissions() },
            { needsAnalyticsOptIn() },
            { shouldDisplayLockscreenSetup() },
        ).any { it() }
    }

    @OptIn(FlowPreview::class)
    private suspend fun isSessionNotVerified(): Boolean {
        // Wait for the first known (or ready) verification status
        val readyVerifiedSessionStatus = sessionVerificationService.sessionVerifiedStatus
            .filter { it != SessionVerifiedStatus.Unknown }
            // This is not ideal, but there are some very rare cases when reading the flow seems to get stuck
            .timeout(5.seconds)
            .catch {
                Timber.e(it, "Failed to get session verification status, assume it's not verified")
                emit(SessionVerifiedStatus.NotVerified)
            }
            .first()
        val skipVerification = suspend { sessionPreferencesStore.isSessionVerificationSkipped().first() }
        return readyVerifiedSessionStatus == SessionVerifiedStatus.NotVerified && !skipVerification()
    }

    private suspend fun needsAnalyticsOptIn(): Boolean {
        // We need this function to not be suspend, so we need to load the value through runBlocking
        return analyticsService.didAskUserConsent().first().not()
    }

    private suspend fun shouldAskNotificationPermissions(): Boolean {
        return if (sdkVersionProvider.isAtLeast(Build.VERSION_CODES.TIRAMISU)) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val isPermissionDenied = runBlocking { permissionStateProvider.isPermissionDenied(permission).first() }
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
        state.value = when {
            isAnyStepIncomplete() -> FtueState.Incomplete
            else -> FtueState.Complete
        }
    }
}

sealed interface FtueStep {
    data object SessionVerification : FtueStep
    data object NotificationsOptIn : FtueStep
    data object AnalyticsOptIn : FtueStep
    data object LockscreenSetup : FtueStep
}
