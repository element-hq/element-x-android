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

package io.element.android.features.ftue.impl

import android.os.Build
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.ftue.api.state.FtueState
import io.element.android.features.ftue.impl.state.DefaultFtueService
import io.element.android.features.ftue.impl.state.FtueStep
import io.element.android.features.lockscreen.api.LockScreenService
import io.element.android.features.lockscreen.test.FakeLockScreenService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.libraries.permissions.impl.FakePermissionStateProvider
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultFtueServiceTest {
    @Test
    fun `given any check being false and session verification state being loaded, FtueState is Incomplete`() = runTest {
        val sessionVerificationService = FakeSessionVerificationService().apply {
            givenVerifiedStatus(SessionVerifiedStatus.Unknown)
        }
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())
        val state = createState(coroutineScope, sessionVerificationService)

        state.state.test {
            // Verification state is unknown, we don't display the flow yet
            assertThat(awaitItem()).isEqualTo(FtueState.Unknown)

            // Verification state is known, we should display the flow if any check is false
            sessionVerificationService.givenVerifiedStatus(SessionVerifiedStatus.NotVerified)
            assertThat(awaitItem()).isEqualTo(FtueState.Incomplete)
        }

        // Cleanup
        coroutineScope.cancel()
    }

    @Test
    fun `given all checks being true, FtueState is Complete`() = runTest {
        val analyticsService = FakeAnalyticsService()
        val sessionVerificationService = FakeSessionVerificationService()
        val permissionStateProvider = FakePermissionStateProvider(permissionGranted = true)
        val lockScreenService = FakeLockScreenService()
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())

        val state = createState(
            coroutineScope = coroutineScope,
            sessionVerificationService = sessionVerificationService,
            analyticsService = analyticsService,
            permissionStateProvider = permissionStateProvider,
            lockScreenService = lockScreenService,
        )

        sessionVerificationService.givenVerifiedStatus(SessionVerifiedStatus.Verified)
        analyticsService.setDidAskUserConsent()
        permissionStateProvider.setPermissionGranted()
        lockScreenService.setIsPinSetup(true)
        state.updateState()

        assertThat(state.state.value).isEqualTo(FtueState.Complete)

        // Cleanup
        coroutineScope.cancel()
    }

    @Test
    fun `traverse flow`() = runTest {
        val sessionVerificationService = FakeSessionVerificationService().apply {
            givenVerifiedStatus(SessionVerifiedStatus.NotVerified)
        }
        val analyticsService = FakeAnalyticsService()
        val permissionStateProvider = FakePermissionStateProvider(permissionGranted = false)
        val lockScreenService = FakeLockScreenService()
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())

        val state = createState(
            coroutineScope = coroutineScope,
            sessionVerificationService = sessionVerificationService,
            analyticsService = analyticsService,
            permissionStateProvider = permissionStateProvider,
            lockScreenService = lockScreenService,
        )
        val steps = mutableListOf<FtueStep?>()

        // Session verification
        steps.add(state.getNextStep(steps.lastOrNull()))
        sessionVerificationService.givenVerifiedStatus(SessionVerifiedStatus.NotVerified)

        // Notifications opt in
        steps.add(state.getNextStep(steps.lastOrNull()))
        permissionStateProvider.setPermissionGranted()

        // Entering PIN code
        steps.add(state.getNextStep(steps.lastOrNull()))
        lockScreenService.setIsPinSetup(true)

        // Analytics opt in
        steps.add(state.getNextStep(steps.lastOrNull()))
        analyticsService.setDidAskUserConsent()

        // Final step (null)
        steps.add(state.getNextStep(steps.lastOrNull()))

        assertThat(steps).containsExactly(
            FtueStep.SessionVerification,
            FtueStep.NotificationsOptIn,
            FtueStep.LockscreenSetup,
            FtueStep.AnalyticsOptIn,
            // Final state
            null,
        )

        // Cleanup
        coroutineScope.cancel()
    }

    @Test
    fun `if a check for a step is true, start from the next one`() = runTest {
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())
        val sessionVerificationService = FakeSessionVerificationService()
        val analyticsService = FakeAnalyticsService()
        val permissionStateProvider = FakePermissionStateProvider(permissionGranted = false)
        val lockScreenService = FakeLockScreenService()
        val state = createState(
            coroutineScope = coroutineScope,
            sessionVerificationService = sessionVerificationService,
            analyticsService = analyticsService,
            permissionStateProvider = permissionStateProvider,
            lockScreenService = lockScreenService,
        )

        // Skip first 3 steps
        sessionVerificationService.givenVerifiedStatus(SessionVerifiedStatus.Verified)
        permissionStateProvider.setPermissionGranted()
        lockScreenService.setIsPinSetup(true)

        assertThat(state.getNextStep()).isEqualTo(FtueStep.AnalyticsOptIn)

        analyticsService.setDidAskUserConsent()
        assertThat(state.getNextStep(null)).isNull()

        // Cleanup
        coroutineScope.cancel()
    }

    @Test
    fun `if version is older than 13 we don't display the notification opt in screen`() = runTest {
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())
        val sessionVerificationService = FakeSessionVerificationService()
        val analyticsService = FakeAnalyticsService()
        val lockScreenService = FakeLockScreenService()

        val state = createState(
            sdkIntVersion = Build.VERSION_CODES.M,
            sessionVerificationService = sessionVerificationService,
            coroutineScope = coroutineScope,
            analyticsService = analyticsService,
            lockScreenService = lockScreenService,
        )

        sessionVerificationService.givenVerifiedStatus(SessionVerifiedStatus.Verified)
        lockScreenService.setIsPinSetup(true)

        assertThat(state.getNextStep()).isEqualTo(FtueStep.AnalyticsOptIn)

        analyticsService.setDidAskUserConsent()
        assertThat(state.getNextStep(null)).isNull()

        // Cleanup
        coroutineScope.cancel()
    }

    private fun createState(
        coroutineScope: CoroutineScope,
        sessionVerificationService: FakeSessionVerificationService = FakeSessionVerificationService(),
        analyticsService: AnalyticsService = FakeAnalyticsService(),
        permissionStateProvider: FakePermissionStateProvider = FakePermissionStateProvider(permissionGranted = false),
        lockScreenService: LockScreenService = FakeLockScreenService(),
        sessionPreferencesStore: InMemorySessionPreferencesStore = InMemorySessionPreferencesStore(),
        // First version where notification permission is required
        sdkIntVersion: Int = Build.VERSION_CODES.TIRAMISU,
    ) = DefaultFtueService(
        coroutineScope = coroutineScope,
        sessionVerificationService = sessionVerificationService,
        sdkVersionProvider = FakeBuildVersionSdkIntProvider(sdkIntVersion),
        analyticsService = analyticsService,
        permissionStateProvider = permissionStateProvider,
        lockScreenService = lockScreenService,
        sessionPreferencesStore = sessionPreferencesStore,
    )
}
