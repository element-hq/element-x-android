/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.permissions.impl.troubleshoot

import android.os.Build
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.permissions.impl.FakePermissionStateProvider
import io.element.android.libraries.permissions.impl.action.FakePermissionActions
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NotificationTroubleshootCheckPermissionTestTest {
    @Test
    fun `test NotificationTroubleshootCheckPermissionTest below TIRAMISU success`() = runTest {
        val sut = NotificationTroubleshootCheckPermissionTest(
            permissionStateProvider = FakePermissionStateProvider(),
            sdkVersionProvider = FakeBuildVersionSdkIntProvider(sdkInt = Build.VERSION_CODES.TIRAMISU - 1),
            permissionActions = FakePermissionActions(),
            stringProvider = FakeStringProvider(),
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
        }
    }

    @Test
    fun `test NotificationTroubleshootCheckPermissionTest TIRAMISU success`() = runTest {
        val sut = NotificationTroubleshootCheckPermissionTest(
            permissionStateProvider = FakePermissionStateProvider(),
            sdkVersionProvider = FakeBuildVersionSdkIntProvider(sdkInt = Build.VERSION_CODES.TIRAMISU),
            permissionActions = FakePermissionActions(),
            stringProvider = FakeStringProvider(),
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
        }
    }

    @Test
    fun `test NotificationTroubleshootCheckPermissionTest TIRAMISU error`() = runTest {
        val permissionStateProvider = FakePermissionStateProvider(
            permissionGranted = false
        )
        val actions = FakePermissionActions(
            openSettingsAction = {
                permissionStateProvider.setPermissionGranted()
            }
        )
        val sut = NotificationTroubleshootCheckPermissionTest(
            permissionStateProvider = permissionStateProvider,
            sdkVersionProvider = FakeBuildVersionSdkIntProvider(sdkInt = Build.VERSION_CODES.TIRAMISU),
            permissionActions = actions,
            stringProvider = FakeStringProvider(),
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(true))
            // Quick fix
            launch {
                sut.quickFix(this)
                // Run the test again (IRL it will be done thanks to the resuming of the application)
                sut.run(this)
            }
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
        }
    }

    @Test
    fun `test NotificationTroubleshootCheckPermissionTest error and reset`() = runTest {
        val permissionStateProvider = FakePermissionStateProvider(
            permissionGranted = false
        )
        val actions = FakePermissionActions(
            openSettingsAction = {
                permissionStateProvider.setPermissionGranted()
            }
        )
        val sut = NotificationTroubleshootCheckPermissionTest(
            permissionStateProvider = permissionStateProvider,
            sdkVersionProvider = FakeBuildVersionSdkIntProvider(sdkInt = Build.VERSION_CODES.TIRAMISU),
            permissionActions = actions,
            stringProvider = FakeStringProvider(),
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(true))
            sut.reset()
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(true))
        }
    }
}
