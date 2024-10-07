/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.ftue.impl

import android.os.Build
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.ftue.api.state.FtueState
import io.element.android.features.ftue.impl.state.DefaultFtueService
import io.element.android.features.ftue.impl.state.FtueStep
import io.element.android.features.lockscreen.api.LockScreenService
import io.element.android.features.lockscreen.test.FakeLockScreenService
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.libraries.permissions.api.PermissionStateProvider
import io.element.android.libraries.permissions.impl.FakePermissionStateProvider
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultFtueServiceTest {
    @Test
    fun `given any check being false and session verification state being loaded, FtueState is Incomplete`() = runTest {
        val sessionVerificationService = FakeSessionVerificationService().apply {
            givenVerifiedStatus(SessionVerifiedStatus.Unknown)
        }
        val service = createDefaultFtueService(
            sessionVerificationService = sessionVerificationService,
        )

        service.state.test {
            // Verification state is unknown, we don't display the flow yet
            assertThat(awaitItem()).isEqualTo(FtueState.Unknown)

            // Verification state is known, we should display the flow if any check is false
            sessionVerificationService.givenVerifiedStatus(SessionVerifiedStatus.NotVerified)
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

        sessionVerificationService.givenVerifiedStatus(SessionVerifiedStatus.Verified)
        analyticsService.setDidAskUserConsent()
        permissionStateProvider.setPermissionGranted()
        lockScreenService.setIsPinSetup(true)
        service.updateState()

        assertThat(service.state.value).isEqualTo(FtueState.Complete)
    }

    @Test
    fun `traverse flow`() = runTest {
        val sessionVerificationService = FakeSessionVerificationService().apply {
            givenVerifiedStatus(SessionVerifiedStatus.NotVerified)
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
        val steps = mutableListOf<FtueStep?>()

        // Session verification
        steps.add(service.getNextStep(steps.lastOrNull()))
        sessionVerificationService.givenVerifiedStatus(SessionVerifiedStatus.NotVerified)

        // Notifications opt in
        steps.add(service.getNextStep(steps.lastOrNull()))
        permissionStateProvider.setPermissionGranted()

        // Entering PIN code
        steps.add(service.getNextStep(steps.lastOrNull()))
        lockScreenService.setIsPinSetup(true)

        // Analytics opt in
        steps.add(service.getNextStep(steps.lastOrNull()))
        analyticsService.setDidAskUserConsent()

        // Final step (null)
        steps.add(service.getNextStep(steps.lastOrNull()))

        assertThat(steps).containsExactly(
            FtueStep.SessionVerification,
            FtueStep.NotificationsOptIn,
            FtueStep.LockscreenSetup,
            FtueStep.AnalyticsOptIn,
            // Final state
            null,
        )
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
        sessionVerificationService.givenVerifiedStatus(SessionVerifiedStatus.Verified)
        permissionStateProvider.setPermissionGranted()
        lockScreenService.setIsPinSetup(true)

        assertThat(service.getNextStep()).isEqualTo(FtueStep.AnalyticsOptIn)

        analyticsService.setDidAskUserConsent()
        assertThat(service.getNextStep(null)).isNull()
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

        sessionVerificationService.givenVerifiedStatus(SessionVerifiedStatus.Verified)
        lockScreenService.setIsPinSetup(true)

        assertThat(service.getNextStep()).isEqualTo(FtueStep.AnalyticsOptIn)

        analyticsService.setDidAskUserConsent()
        assertThat(service.getNextStep(null)).isNull()
    }

    @Test
    fun `reset do the expected actions S`() = runTest {
        val resetAnalyticsLambda = lambdaRecorder<Unit> { }
        val analyticsService = FakeAnalyticsService(
            resetLambda = resetAnalyticsLambda
        )
        val resetPermissionLambda = lambdaRecorder<String, Unit> { }
        val permissionStateProvider = FakePermissionStateProvider(
            resetPermissionLambda = resetPermissionLambda
        )
        val service = createDefaultFtueService(
            sdkIntVersion = Build.VERSION_CODES.S,
            permissionStateProvider = permissionStateProvider,
            analyticsService = analyticsService,
        )
        service.reset()
        resetAnalyticsLambda.assertions().isCalledOnce()
        resetPermissionLambda.assertions().isNeverCalled()
    }

    @Test
    fun `reset do the expected actions TIRAMISU`() = runTest {
        val resetLambda = lambdaRecorder<Unit> { }
        val analyticsService = FakeAnalyticsService(
            resetLambda = resetLambda
        )
        val resetPermissionLambda = lambdaRecorder<String, Unit> { }
        val permissionStateProvider = FakePermissionStateProvider(
            resetPermissionLambda = resetPermissionLambda
        )
        val service = createDefaultFtueService(
            sdkIntVersion = Build.VERSION_CODES.TIRAMISU,
            permissionStateProvider = permissionStateProvider,
            analyticsService = analyticsService,
        )
        service.reset()
        resetLambda.assertions().isCalledOnce()
        resetPermissionLambda.assertions().isCalledOnce()
            .with(value("android.permission.POST_NOTIFICATIONS"))
    }

    private fun TestScope.createDefaultFtueService(
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
}
