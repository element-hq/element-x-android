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

package io.element.android.features.roomdetails.rolesandpermissions

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.roomdetails.impl.R
import io.element.android.features.roomdetails.impl.rolesandpermissions.RolesAndPermissionsNavigator
import io.element.android.features.roomdetails.impl.rolesandpermissions.RolesAndPermissionsState
import io.element.android.features.roomdetails.impl.rolesandpermissions.RolesAndPermissionsView
import io.element.android.features.roomdetails.impl.rolesandpermissions.aRolesAndPermissionsState
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RolesAndPermissionsViewTests {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `click on back invokes expected callback`() {
        ensureCalledOnce { callback ->
            rule.setRolesAndPermissionsView(
                goBack = callback,
            )
            rule.pressBack()
        }
    }

    @Test
    fun `tapping on Admins opens admin list`() {
        ensureCalledOnce { callback ->
            rule.setRolesAndPermissionsView(
                openAdminList = callback,
            )
            rule.clickOn(R.string.screen_room_roles_and_permissions_admins)
        }
    }

    @Test
    fun `tapping on Moderators opens moderators list`() {
        ensureCalledOnce { callback ->
            rule.setRolesAndPermissionsView(
                openModeratorList = callback,
            )
            rule.clickOn(R.string.screen_room_roles_and_permissions_moderators)
        }
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setRolesAndPermissionsView(
    state: RolesAndPermissionsState = aRolesAndPermissionsState(
         eventSink = EventsRecorder(expectEvents = false),
    ),
    goBack: () -> Unit = EnsureNeverCalled(),
    openAdminList: () -> Unit = EnsureNeverCalled(),
    openModeratorList: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        RolesAndPermissionsView(
            state = state,
            rolesAndPermissionsNavigator = object : RolesAndPermissionsNavigator {
                override fun onBackPressed() = goBack()
                override fun openAdminList() = openAdminList()
                override fun openModeratorList() = openModeratorList()
            }
        )
    }
}
