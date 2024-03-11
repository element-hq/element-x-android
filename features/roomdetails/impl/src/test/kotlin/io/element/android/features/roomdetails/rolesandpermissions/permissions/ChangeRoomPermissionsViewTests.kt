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
import io.element.android.tests.testutils.pressBack
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChangeRoomPermissionsViewTests {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `click on back invokes Exit`() {
        val recorder = EventsRecorder<ChangeRoomPermissionsEvent>()
        rule.setChangeRoomPermissionsRule(
            eventsRecorder = recorder,
        )
        rule.pressBack()
        recorder.assertSingle(ChangeRoomPermissionsEvent.Exit)
        rule.pressBack()
        recorder.assertList(listOf(ChangeRoomPermissionsEvent.Exit, ChangeRoomPermissionsEvent.Exit))
    }

    @Test
    fun `when confirming exit with pending changes, clicking on 'go back' button in the dialog actually exits`() {
        val recorder = EventsRecorder<ChangeRoomPermissionsEvent>()
        rule.setChangeRoomPermissionsRule(
            state = aChangeRoomPermissionsState(
                section = ChangeRoomPermissionsSection.RoomDetails,
                hasChanges = true,
                confirmExitAction = AsyncAction.Confirming,
                eventSink = recorder,
            ),
            eventsRecorder = recorder,
        )
        rule.clickOn(CommonStrings.action_go_back)
        recorder.assertSingle(ChangeRoomPermissionsEvent.Exit)
    }

    @Test
    fun `click on back with pending changes invokes Exit and displays a dialog, then clicking 'save' button in the dialog invokes Save`() {
        val recorder = EventsRecorder<ChangeRoomPermissionsEvent>()
        rule.setChangeRoomPermissionsRule(
            state = aChangeRoomPermissionsState(
                section = ChangeRoomPermissionsSection.RoomDetails,
                hasChanges = true,
                eventSink = recorder,
            ),
            eventsRecorder = recorder,
        )
        rule.pressBack()
        recorder.assertSingle(ChangeRoomPermissionsEvent.Exit)
        rule.clickOn(CommonStrings.action_save)
        recorder.assertList(listOf(ChangeRoomPermissionsEvent.Exit, ChangeRoomPermissionsEvent.Save))
    }

    @Test
    fun `click on a role item triggers ChangeRole event`() {
        val recorder = EventsRecorder<ChangeRoomPermissionsEvent>()
        rule.setChangeRoomPermissionsRule(
            eventsRecorder = recorder,
        )
        val text = rule.activity.getText(R.string.screen_room_change_permissions_moderators).toString()
        rule.onAllNodesWithText(text).onFirst().performClick()
        recorder.assertSingle(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_NAME, RoomMember.Role.MODERATOR))
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
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setChangeRoomPermissionsRule(
    eventsRecorder: EventsRecorder<ChangeRoomPermissionsEvent> = EventsRecorder(expectEvents = false),
    state: ChangeRoomPermissionsState = aChangeRoomPermissionsState(
        section = ChangeRoomPermissionsSection.RoomDetails,
        eventSink = eventsRecorder,
    ),
    onBackPressed: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        ChangeRoomPermissionsView(
            state = state,
            onBackPressed = onBackPressed,
        )
    }
}
