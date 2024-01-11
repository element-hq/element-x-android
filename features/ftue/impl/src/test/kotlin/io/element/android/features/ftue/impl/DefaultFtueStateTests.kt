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
import com.google.common.truth.Truth.assertThat
import io.element.android.features.ftue.impl.migration.InMemoryMigrationScreenStore
import io.element.android.features.ftue.impl.migration.MigrationScreenStore
import io.element.android.features.ftue.impl.state.DefaultFtueState
import io.element.android.features.ftue.impl.state.FtueStep
import io.element.android.features.ftue.impl.welcome.state.FakeWelcomeState
import io.element.android.features.lockscreen.api.LockScreenService
import io.element.android.features.lockscreen.test.FakeLockScreenService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.permissions.impl.FakePermissionStateProvider
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultFtueStateTests {
    @Test
    fun `given any check being false, should display flow is true`() = runTest {
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())
        val state = createState(coroutineScope)

        assertThat(state.shouldDisplayFlow.value).isTrue()

        // Cleanup
        coroutineScope.cancel()
    }

    @Test
    fun `given all checks being true, should display flow is false`() = runTest {
        val welcomeState = FakeWelcomeState()
        val analyticsService = FakeAnalyticsService()
        val migrationScreenStore = InMemoryMigrationScreenStore()
        val permissionStateProvider = FakePermissionStateProvider(permissionGranted = true)
        val lockScreenService = FakeLockScreenService()
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())

        val state = createState(
            coroutineScope = coroutineScope,
            welcomeState = welcomeState,
            analyticsService = analyticsService,
            migrationScreenStore = migrationScreenStore,
            permissionStateProvider = permissionStateProvider,
            lockScreenService = lockScreenService,
        )

        welcomeState.setWelcomeScreenShown()
        analyticsService.setDidAskUserConsent()
        migrationScreenStore.setMigrationScreenShown(A_SESSION_ID)
        permissionStateProvider.setPermissionGranted()
        lockScreenService.setIsPinSetup(true)
        state.updateState()

        assertThat(state.shouldDisplayFlow.value).isFalse()

        // Cleanup
        coroutineScope.cancel()
    }

    @Test
    fun `traverse flow`() = runTest {
        val welcomeState = FakeWelcomeState()
        val analyticsService = FakeAnalyticsService()
        val migrationScreenStore = InMemoryMigrationScreenStore()
        val permissionStateProvider = FakePermissionStateProvider(permissionGranted = false)
        val lockScreenService = FakeLockScreenService()
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())

        val state = createState(
            coroutineScope = coroutineScope,
            welcomeState = welcomeState,
            analyticsService = analyticsService,
            migrationScreenStore = migrationScreenStore,
            permissionStateProvider = permissionStateProvider,
            lockScreenService = lockScreenService,
        )
        val steps = mutableListOf<FtueStep?>()

        // First step, migration screen
        steps.add(state.getNextStep(steps.lastOrNull()))
        migrationScreenStore.setMigrationScreenShown(A_SESSION_ID)

        // Second step, welcome screen
        steps.add(state.getNextStep(steps.lastOrNull()))
        welcomeState.setWelcomeScreenShown()

        // Third step, notifications opt in
        steps.add(state.getNextStep(steps.lastOrNull()))
        permissionStateProvider.setPermissionGranted()

        // Fourth step, entering PIN code
        steps.add(state.getNextStep(steps.lastOrNull()))
        lockScreenService.setIsPinSetup(true)

        // Fifth step, analytics opt in
        steps.add(state.getNextStep(steps.lastOrNull()))
        analyticsService.setDidAskUserConsent()

        // Final step (null)
        steps.add(state.getNextStep(steps.lastOrNull()))

        assertThat(steps).containsExactly(
            FtueStep.MigrationScreen,
            FtueStep.WelcomeScreen,
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
        val analyticsService = FakeAnalyticsService()
        val migrationScreenStore = InMemoryMigrationScreenStore()
        val permissionStateProvider = FakePermissionStateProvider(permissionGranted = false)
        val lockScreenService = FakeLockScreenService()
        val state = createState(
            coroutineScope = coroutineScope,
            analyticsService = analyticsService,
            migrationScreenStore = migrationScreenStore,
            permissionStateProvider = permissionStateProvider,
            lockScreenService = lockScreenService,
        )

        // Skip first 4 steps
        migrationScreenStore.setMigrationScreenShown(A_SESSION_ID)
        state.setWelcomeScreenShown()
        permissionStateProvider.setPermissionGranted()
        lockScreenService.setIsPinSetup(true)

        assertThat(state.getNextStep()).isEqualTo(FtueStep.AnalyticsOptIn)

        analyticsService.setDidAskUserConsent()
        assertThat(state.getNextStep(FtueStep.WelcomeScreen)).isNull()

        // Cleanup
        coroutineScope.cancel()
    }

    @Test
    fun `if version is older than 13 we don't display the notification opt in screen`() = runTest {
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())
        val analyticsService = FakeAnalyticsService()
        val migrationScreenStore = InMemoryMigrationScreenStore()
        val lockScreenService = FakeLockScreenService()

        val state = createState(
            sdkIntVersion = Build.VERSION_CODES.M,
            coroutineScope = coroutineScope,
            analyticsService = analyticsService,
            migrationScreenStore = migrationScreenStore,
            lockScreenService = lockScreenService,
        )

        migrationScreenStore.setMigrationScreenShown(A_SESSION_ID)
        assertThat(state.getNextStep()).isEqualTo(FtueStep.WelcomeScreen)
        state.setWelcomeScreenShown()
        lockScreenService.setIsPinSetup(true)

        assertThat(state.getNextStep()).isEqualTo(FtueStep.AnalyticsOptIn)

        analyticsService.setDidAskUserConsent()
        assertThat(state.getNextStep(FtueStep.WelcomeScreen)).isNull()

        // Cleanup
        coroutineScope.cancel()
    }

    private fun createState(
        coroutineScope: CoroutineScope,
        welcomeState: FakeWelcomeState = FakeWelcomeState(),
        analyticsService: AnalyticsService = FakeAnalyticsService(),
        migrationScreenStore: MigrationScreenStore = InMemoryMigrationScreenStore(),
        permissionStateProvider: FakePermissionStateProvider = FakePermissionStateProvider(permissionGranted = false),
        matrixClient: MatrixClient = FakeMatrixClient(),
        lockScreenService: LockScreenService = FakeLockScreenService(),
        // First version where notification permission is required
        sdkIntVersion: Int = Build.VERSION_CODES.TIRAMISU,
    ) = DefaultFtueState(
        sdkVersionProvider = FakeBuildVersionSdkIntProvider(sdkIntVersion),
        coroutineScope = coroutineScope,
        analyticsService = analyticsService,
        welcomeScreenState = welcomeState,
        migrationScreenStore = migrationScreenStore,
        permissionStateProvider = permissionStateProvider,
        lockScreenService = lockScreenService,
        matrixClient = matrixClient,
    )
}
