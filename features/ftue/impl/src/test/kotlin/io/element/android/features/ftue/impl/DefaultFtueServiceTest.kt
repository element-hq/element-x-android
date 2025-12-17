/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl

import android.os.Build
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.ftue.api.state.FtueState
import io.element.android.features.ftue.impl.state.DefaultFtueService
import io.element.android.features.ftue.impl.state.FtueStep
import io.element.android.features.ftue.impl.state.InternalFtueState
import io.element.android.features.lockscreen.api.LockScreenService
import io.element.android.features.lockscreen.test.FakeLockScreenService
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.libraries.permissions.api.PermissionStateProvider
import io.element.android.libraries.permissions.test.FakePermissionStateProvider
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.noop.NoopAnalyticsService
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultFtueServiceTest {
    @Test
    fun `given any check being false and session verification state being loaded, FtueState is Incomplete`() = runTest {
        val sessionVerificationService = FakeSessionVerificationService().apply {
            emitVerifiedStatus(SessionVerifiedStatus.Unknown)
        }
        val service = createDefaultFtueService(
            sessionVerificationService = sessionVerificationService,
        )

        service.state.test {
            // Verification state is unknown, we don't display the flow yet
            assertThat(awaitItem()).isEqualTo(FtueState.Unknown)

            // Verification state is known, we should display the flow if any check is false
            sessionVerificationService.emitVerifiedStatus(SessionVerifiedStatus.NotVerified)
            assertThat(awaitItem()).isEqualTo(FtueState.Incomplete)
        }
    }

    @Test
    fun `given all checks being true, FtueState is Complete`() = runTest {
        val analyticsService = FakeAnalyticsService()
        val sessionVerificationService = FakeSessionVerificationService()
        val permissionStateProvider = FakePermissionStateProvider(permissionGranted = true)
        val lockScreenService = FakeLockScreenService()
        val service = createDefaultFtueService(
            sessionVerificationService = sessionVerificationService,
            analyticsService = analyticsService,
            permissionStateProvider = permissionStateProvider,
            lockScreenService = lockScreenService,
        )

        sessionVerificationService.emitVerifiedStatus(SessionVerifiedStatus.Verified)
        analyticsService.setDidAskUserConsent()
        permissionStateProvider.setPermissionGranted()
        lockScreenService.setIsPinSetup(true)
        service.updateFtueStep()
        service.state.test {
            assertThat(awaitItem()).isEqualTo(FtueState.Unknown)
            assertThat(awaitItem()).isEqualTo(FtueState.Complete)
        }
    }

    @Test
    fun `given all checks being true with no analytics, FtueState is Complete`() = runTest {
        val analyticsService = NoopAnalyticsService()
        val sessionVerificationService = FakeSessionVerificationService()
        val permissionStateProvider = FakePermissionStateProvider(permissionGranted = true)
        val lockScreenService = FakeLockScreenService()
        val service = createDefaultFtueService(
            sessionVerificationService = sessionVerificationService,
            analyticsService = analyticsService,
            permissionStateProvider = permissionStateProvider,
            lockScreenService = lockScreenService,
        )

        sessionVerificationService.emitVerifiedStatus(SessionVerifiedStatus.Verified)
        permissionStateProvider.setPermissionGranted()
        lockScreenService.setIsPinSetup(true)
        service.updateFtueStep()
        service.state.test {
            assertThat(awaitItem()).isEqualTo(FtueState.Unknown)
            assertThat(awaitItem()).isEqualTo(FtueState.Complete)
        }
    }

    @Test
    fun `traverse flow`() = runTest {
        val sessionVerificationService = FakeSessionVerificationService().apply {
            emitVerifiedStatus(SessionVerifiedStatus.NotVerified)
        }
        val analyticsService = FakeAnalyticsService()
        val permissionStateProvider = FakePermissionStateProvider(permissionGranted = false)
        val lockScreenService = FakeLockScreenService()
        val service = createDefaultFtueService(
            sessionVerificationService = sessionVerificationService,
            analyticsService = analyticsService,
            permissionStateProvider = permissionStateProvider,
            lockScreenService = lockScreenService,
        )

        service.ftueStepStateFlow.test {
            assertThat(awaitItem()).isEqualTo(InternalFtueState.Unknown)
            // Session verification
            assertThat(awaitItem()).isEqualTo(InternalFtueState.Incomplete(FtueStep.SessionVerification))
            sessionVerificationService.emitVerifiedStatus(SessionVerifiedStatus.Verified)
            // User completes verification
            service.onUserCompletedSessionVerification()
            // Notifications opt in
            assertThat(awaitItem()).isEqualTo(InternalFtueState.Incomplete(FtueStep.NotificationsOptIn))
            permissionStateProvider.setPermissionGranted()
            // Simulate event from NotificationsOptInNode.Callback.onNotificationsOptInFinished
            service.updateFtueStep()
            // Entering PIN code
            assertThat(awaitItem()).isEqualTo(InternalFtueState.Incomplete(FtueStep.LockscreenSetup))
            lockScreenService.setIsPinSetup(true)
            // Simulate event from LockScreenEntryPoint.Callback.onSetupDone()
            service.updateFtueStep()
            // Analytics opt in
            assertThat(awaitItem()).isEqualTo(InternalFtueState.Incomplete(FtueStep.AnalyticsOptIn))
            analyticsService.setDidAskUserConsent()
            // Final step
            assertThat(awaitItem()).isEqualTo(InternalFtueState.Complete)
        }
    }

    @Test
    fun `if a check for a step is true, start from the next one`() = runTest {
        val sessionVerificationService = FakeSessionVerificationService()
        val analyticsService = FakeAnalyticsService()
        val permissionStateProvider = FakePermissionStateProvider(permissionGranted = false)
        val lockScreenService = FakeLockScreenService()
        val service = createDefaultFtueService(
            sessionVerificationService = sessionVerificationService,
            analyticsService = analyticsService,
            permissionStateProvider = permissionStateProvider,
            lockScreenService = lockScreenService,
        )

        // Skip first 3 steps
        sessionVerificationService.emitVerifiedStatus(SessionVerifiedStatus.Verified)
        permissionStateProvider.setPermissionGranted()
        lockScreenService.setIsPinSetup(true)

        service.ftueStepStateFlow.test {
            assertThat(awaitItem()).isEqualTo(InternalFtueState.Unknown)
            // Analytics opt in
            assertThat(awaitItem()).isEqualTo(InternalFtueState.Incomplete(FtueStep.AnalyticsOptIn))
            analyticsService.setDidAskUserConsent()
            assertThat(awaitItem()).isEqualTo(InternalFtueState.Complete)
        }
    }

    @Test
    fun `if version is older than 13 we don't display the notification opt in screen`() = runTest {
        val sessionVerificationService = FakeSessionVerificationService()
        val analyticsService = FakeAnalyticsService()
        val lockScreenService = FakeLockScreenService()

        val service = createDefaultFtueService(
            sdkIntVersion = Build.VERSION_CODES.M,
            sessionVerificationService = sessionVerificationService,
            analyticsService = analyticsService,
            lockScreenService = lockScreenService,
        )

        sessionVerificationService.emitVerifiedStatus(SessionVerifiedStatus.Verified)
        lockScreenService.setIsPinSetup(true)

        service.ftueStepStateFlow.test {
            assertThat(awaitItem()).isEqualTo(InternalFtueState.Unknown)
            // Analytics opt in
            assertThat(awaitItem()).isEqualTo(InternalFtueState.Incomplete(FtueStep.AnalyticsOptIn))
            analyticsService.setDidAskUserConsent()
            assertThat(awaitItem()).isEqualTo(InternalFtueState.Complete)
        }
    }
}

internal fun TestScope.createDefaultFtueService(
    sessionVerificationService: SessionVerificationService = FakeSessionVerificationService(),
    analyticsService: AnalyticsService = FakeAnalyticsService(),
    permissionStateProvider: PermissionStateProvider = FakePermissionStateProvider(permissionGranted = false),
    lockScreenService: LockScreenService = FakeLockScreenService(),
    sessionPreferencesStore: SessionPreferencesStore = InMemorySessionPreferencesStore(),
    // First version where notification permission is required
    sdkIntVersion: Int = Build.VERSION_CODES.TIRAMISU,
) = DefaultFtueService(
    sessionCoroutineScope = backgroundScope,
    sessionVerificationService = sessionVerificationService,
    sdkVersionProvider = FakeBuildVersionSdkIntProvider(sdkIntVersion),
    analyticsService = analyticsService,
    permissionStateProvider = permissionStateProvider,
    lockScreenService = lockScreenService,
    sessionPreferencesStore = sessionPreferencesStore,
)
