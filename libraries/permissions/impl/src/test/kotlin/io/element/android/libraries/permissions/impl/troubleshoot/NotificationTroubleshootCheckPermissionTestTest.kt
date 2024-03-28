/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.permissions.impl.troubleshoot

import android.os.Build
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.notifications.NotificationTroubleshootTestState
import io.element.android.libraries.permissions.impl.FakePermissionStateProvider
import io.element.android.libraries.permissions.impl.action.FakePermissionActions
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
}
