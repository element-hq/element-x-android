/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.rolesandpermissions

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.roomdetails.impl.R
import io.element.android.features.roomdetails.impl.rolesandpermissions.RolesAndPermissionsEvents
import io.element.android.features.roomdetails.impl.rolesandpermissions.RolesAndPermissionsNavigator
import io.element.android.features.roomdetails.impl.rolesandpermissions.RolesAndPermissionsState
import io.element.android.features.roomdetails.impl.rolesandpermissions.RolesAndPermissionsView
import io.element.android.features.roomdetails.impl.rolesandpermissions.aRolesAndPermissionsState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledTimes
import io.element.android.tests.testutils.pressBack
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class RolesAndPermissionsViewTest {
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

    @Test
    @Config(qualifiers = "h640dp")
    fun `tapping on any of the permission items open the change permissions screen`() {
        ensureCalledTimes(3) { callback ->
            rule.setRolesAndPermissionsView(
                openPermissionScreens = callback,
            )
            rule.clickOn(R.string.screen_room_roles_and_permissions_room_details)
            rule.clickOn(R.string.screen_room_roles_and_permissions_messages_and_content)
            rule.clickOn(R.string.screen_room_roles_and_permissions_member_moderation)
        }
    }

    @Test
    @Config(qualifiers = "h640dp")
    fun `tapping on reset permissions triggers ResetPermissions event`() {
        val recorder = EventsRecorder<RolesAndPermissionsEvents>()
        rule.setRolesAndPermissionsView(
            state = aRolesAndPermissionsState(
                eventSink = recorder,
            ),
        )
        rule.clickOn(R.string.screen_room_roles_and_permissions_reset)
        recorder.assertSingle(RolesAndPermissionsEvents.ResetPermissions)
    }

    @Test
    fun `tapping on Reset in the reset permissions confirmation dialog triggers ResetPermissions event`() {
        val recorder = EventsRecorder<RolesAndPermissionsEvents>()
        rule.setRolesAndPermissionsView(
            state = aRolesAndPermissionsState(
                resetPermissionsAction = AsyncAction.ConfirmingNoParams,
                eventSink = recorder,
            ),
        )
        rule.clickOn(CommonStrings.action_reset)
        recorder.assertSingle(RolesAndPermissionsEvents.ResetPermissions)
    }

    @Test
    fun `tapping on Cancel in the reset permissions confirmation dialog triggers CancelPendingAction event`() {
        val recorder = EventsRecorder<RolesAndPermissionsEvents>()
        rule.setRolesAndPermissionsView(
            state = aRolesAndPermissionsState(
                resetPermissionsAction = AsyncAction.ConfirmingNoParams,
                eventSink = recorder,
            ),
        )
        rule.clickOn(CommonStrings.action_cancel)
        recorder.assertSingle(RolesAndPermissionsEvents.CancelPendingAction)
    }

    @Test
    fun `tapping on 'Demote to moderator' in the demote self bottom sheet triggers the right event`() {
        val recorder = EventsRecorder<RolesAndPermissionsEvents>()
        rule.setRolesAndPermissionsView(
            state = aRolesAndPermissionsState(
                changeOwnRoleAction = AsyncAction.ConfirmingNoParams,
                eventSink = recorder,
            ),
        )
        rule.clickOn(R.string.screen_room_roles_and_permissions_change_role_demote_to_moderator)
        rule.mainClock.advanceTimeBy(1_000L)
        recorder.assertSingle(RolesAndPermissionsEvents.DemoteSelfTo(RoomMember.Role.MODERATOR))
    }

    @Test
    fun `tapping on 'Demote to member' in the demote self bottom sheet triggers the right event`() = runTest {
        val recorder = EventsRecorder<RolesAndPermissionsEvents>()
        rule.setRolesAndPermissionsView(
            state = aRolesAndPermissionsState(
                changeOwnRoleAction = AsyncAction.ConfirmingNoParams,
                eventSink = recorder,
            ),
        )
        rule.clickOn(R.string.screen_room_roles_and_permissions_change_role_demote_to_member)
        rule.mainClock.advanceTimeBy(1_000L)
        recorder.assertSingle(RolesAndPermissionsEvents.DemoteSelfTo(RoomMember.Role.USER))
    }

    @Test
    fun `tapping on 'Cancel' in the demote self bottom sheet triggers the right event`() {
        val recorder = EventsRecorder<RolesAndPermissionsEvents>()
        rule.setRolesAndPermissionsView(
            state = aRolesAndPermissionsState(
                changeOwnRoleAction = AsyncAction.ConfirmingNoParams,
                eventSink = recorder,
            ),
        )
        rule.clickOn(CommonStrings.action_cancel)
        rule.mainClock.advanceTimeBy(1_000L)
        recorder.assertSingle(RolesAndPermissionsEvents.CancelPendingAction)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setRolesAndPermissionsView(
    state: RolesAndPermissionsState = aRolesAndPermissionsState(
         eventSink = EventsRecorder(expectEvents = false),
    ),
    goBack: () -> Unit = EnsureNeverCalled(),
    openAdminList: () -> Unit = EnsureNeverCalled(),
    openModeratorList: () -> Unit = EnsureNeverCalled(),
    openPermissionScreens: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        RolesAndPermissionsView(
            state = state,
            rolesAndPermissionsNavigator = object : RolesAndPermissionsNavigator {
                override fun onBackClick() = goBack()
                override fun openAdminList() = openAdminList()
                override fun openModeratorList() = openModeratorList()
                override fun openEditRoomDetailsPermissions() = openPermissionScreens()
                override fun openModerationPermissions() = openPermissionScreens()
                override fun openMessagesAndContentPermissions() = openPermissionScreens()
            }
        )
    }
}
