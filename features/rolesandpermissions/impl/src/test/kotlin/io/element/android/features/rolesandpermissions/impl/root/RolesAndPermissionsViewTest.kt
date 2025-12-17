/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rolesandpermissions.impl.root

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.rolesandpermissions.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledTimes
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.setSafeContent
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
                aRolesAndPermissionsState(
                    roomSupportsOwners = false,
                    eventSink = EventsRecorder(expectEvents = false)
                ),
                openAdminList = callback,
            )
            rule.clickOn(R.string.screen_room_roles_and_permissions_admins)
        }
    }

    @Test
    fun `tapping on Admins and Owners opens admin list`() {
        ensureCalledOnce { callback ->
            rule.setRolesAndPermissionsView(
                aRolesAndPermissionsState(
                    roomSupportsOwners = true,
                    eventSink = EventsRecorder(expectEvents = false)
                ),
                openAdminList = callback,
            )
            rule.clickOn(R.string.screen_room_roles_and_permissions_admins_and_owners)
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
    fun `tapping permission item open the change permissions screen`() {
        ensureCalledTimes(1) { callback ->
            rule.setRolesAndPermissionsView(
                openEditPermissions = callback,
            )
            rule.clickOn(R.string.screen_room_roles_and_permissions_permissions_header)
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
        recorder.assertSingle(RolesAndPermissionsEvents.DemoteSelfTo(RoomMember.Role.Moderator))
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
        recorder.assertSingle(RolesAndPermissionsEvents.DemoteSelfTo(RoomMember.Role.User))
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
        roomSupportsOwners = false,
        eventSink = EventsRecorder(expectEvents = false),
    ),
    goBack: () -> Unit = EnsureNeverCalled(),
    openAdminList: () -> Unit = EnsureNeverCalled(),
    openModeratorList: () -> Unit = EnsureNeverCalled(),
    openEditPermissions: () -> Unit = EnsureNeverCalled(),
) {
    setSafeContent {
        RolesAndPermissionsView(
            state = state,
            rolesAndPermissionsNavigator = object : RolesAndPermissionsNavigator {
                override fun onBackClick() = goBack()
                override fun openAdminList() = openAdminList()
                override fun openModeratorList() = openModeratorList()
                override fun openEditPermissions() = openEditPermissions()
            }
        )
    }
}
