/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.rolesandpermissions.permissions

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.roomdetails.impl.R
import io.element.android.features.roomdetails.impl.rolesandpermissions.permissions.ChangeRoomPermissionsEvent
import io.element.android.features.roomdetails.impl.rolesandpermissions.permissions.ChangeRoomPermissionsSection
import io.element.android.features.roomdetails.impl.rolesandpermissions.permissions.ChangeRoomPermissionsState
import io.element.android.features.roomdetails.impl.rolesandpermissions.permissions.ChangeRoomPermissionsView
import io.element.android.features.roomdetails.impl.rolesandpermissions.permissions.RoomPermissionType
import io.element.android.features.roomdetails.impl.rolesandpermissions.permissions.aChangeRoomPermissionsState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.clickOnFirst
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.pressBackKey
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChangeRoomPermissionsViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `click on back icon invokes Exit`() {
        val recorder = EventsRecorder<ChangeRoomPermissionsEvent>()
        rule.setChangeRoomPermissionsRule(
            eventsRecorder = recorder,
        )
        rule.pressBack()
        recorder.assertSingle(ChangeRoomPermissionsEvent.Exit)
    }

    @Test
    fun `click on back key invokes Exit`() {
        val recorder = EventsRecorder<ChangeRoomPermissionsEvent>()
        rule.setChangeRoomPermissionsRule(
            eventsRecorder = recorder,
        )
        rule.pressBackKey()
        recorder.assertSingle(ChangeRoomPermissionsEvent.Exit)
    }

    @Test
    fun `when confirming exit with pending changes, using the back key actually exits`() {
        val recorder = EventsRecorder<ChangeRoomPermissionsEvent>()
        rule.setChangeRoomPermissionsRule(
            state = aChangeRoomPermissionsState(
                section = ChangeRoomPermissionsSection.RoomDetails,
                hasChanges = true,
                eventSink = recorder,
            ),
            eventsRecorder = recorder,
        )
        rule.pressBackKey()
        recorder.assertSingle(ChangeRoomPermissionsEvent.Exit)
    }

    @Test
    fun `when confirming exit with pending changes, clicking on 'discard' button in the dialog actually exits`() {
        val recorder = EventsRecorder<ChangeRoomPermissionsEvent>()
        rule.setChangeRoomPermissionsRule(
            state = aChangeRoomPermissionsState(
                section = ChangeRoomPermissionsSection.RoomDetails,
                hasChanges = true,
                confirmExitAction = AsyncAction.ConfirmingNoParams,
                eventSink = recorder,
            ),
            eventsRecorder = recorder,
        )
        rule.clickOn(CommonStrings.action_discard)
        recorder.assertSingle(ChangeRoomPermissionsEvent.Exit)
    }

    @Test
    fun `when confirming exit with pending changes, clicking on 'save' button in the dialog saves the changes`() {
        val recorder = EventsRecorder<ChangeRoomPermissionsEvent>()
        rule.setChangeRoomPermissionsRule(
            state = aChangeRoomPermissionsState(
                section = ChangeRoomPermissionsSection.RoomDetails,
                hasChanges = true,
                confirmExitAction = AsyncAction.ConfirmingNoParams,
                eventSink = recorder,
            ),
            eventsRecorder = recorder,
        )
        rule.clickOnFirst(CommonStrings.action_save)
        recorder.assertSingle(ChangeRoomPermissionsEvent.Save)
    }

    @Test
    fun `click on a role item triggers ChangeRole event`() {
        val recorder = EventsRecorder<ChangeRoomPermissionsEvent>()
        rule.setChangeRoomPermissionsRule(
            eventsRecorder = recorder,
        )
        val admins = rule.activity.getText(R.string.screen_room_change_permissions_administrators).toString()
        val moderators = rule.activity.getText(R.string.screen_room_change_permissions_moderators).toString()
        val users = rule.activity.getText(R.string.screen_room_change_permissions_everyone).toString()
        rule.onAllNodesWithText(admins).onFirst().performClick()
        rule.onAllNodesWithText(moderators).onFirst().performClick()
        rule.onAllNodesWithText(users).onFirst().performClick()
        recorder.assertList(
            listOf(
                ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_NAME, RoomMember.Role.ADMIN),
                ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_NAME, RoomMember.Role.MODERATOR),
                ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_NAME, RoomMember.Role.USER),
            )
        )
    }

    @Test
    fun `click on the Save menu item triggers Save event`() {
        val recorder = EventsRecorder<ChangeRoomPermissionsEvent>()
        rule.setChangeRoomPermissionsRule(
            state = aChangeRoomPermissionsState(
                section = ChangeRoomPermissionsSection.RoomDetails,
                hasChanges = true,
                eventSink = recorder,
            ),
            eventsRecorder = recorder,
        )
        rule.clickOn(CommonStrings.action_save)
        recorder.assertSingle(ChangeRoomPermissionsEvent.Save)
    }

    @Test
    fun `a successful save exits the screen`() {
        ensureCalledOnce { callback ->
            rule.setChangeRoomPermissionsRule(
                state = aChangeRoomPermissionsState(
                    section = ChangeRoomPermissionsSection.RoomDetails,
                    hasChanges = true,
                    saveAction = AsyncAction.Success(Unit),
                ),
                onBackClick = callback
            )
            rule.clickOn(CommonStrings.action_save)
        }
    }

    @Test
    fun `click on the Ok option in save error dialog triggers ResetPendingAction event`() {
        val recorder = EventsRecorder<ChangeRoomPermissionsEvent>()
        rule.setChangeRoomPermissionsRule(
            state = aChangeRoomPermissionsState(
                section = ChangeRoomPermissionsSection.RoomDetails,
                hasChanges = true,
                saveAction = AsyncAction.Failure(IllegalStateException("Failed to set room power levels")),
                eventSink = recorder,
            ),
            eventsRecorder = recorder,
        )
        rule.clickOn(CommonStrings.action_ok)
        recorder.assertSingle(ChangeRoomPermissionsEvent.ResetPendingActions)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setChangeRoomPermissionsRule(
    eventsRecorder: EventsRecorder<ChangeRoomPermissionsEvent> = EventsRecorder(expectEvents = false),
    state: ChangeRoomPermissionsState = aChangeRoomPermissionsState(
        section = ChangeRoomPermissionsSection.RoomDetails,
        eventSink = eventsRecorder,
    ),
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        ChangeRoomPermissionsView(
            state = state,
            onBackClick = onBackClick,
        )
    }
}
