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

package io.element.android.features.roomlist.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.roomlist.impl.components.RoomListMenuAction
import io.element.android.features.roomlist.impl.model.RoomSummaryDisplayType
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomListViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on close recovery key banner emits the expected Event`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        rule.setRoomListView(
            state = aRoomListState(
                contentState = aRoomsContentState(securityBannerState = SecurityBannerState.RecoveryKeyConfirmation),
                eventSink = eventsRecorder,
            )
        )
        val close = rule.activity.getString(CommonStrings.action_close)
        rule.onNodeWithContentDescription(close).performClick()
        eventsRecorder.assertSingle(RoomListEvents.DismissRecoveryKeyPrompt)
    }

    @Test
    fun `clicking on continue recovery key banner invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setRoomListView(
                state = aRoomListState(
                    contentState = aRoomsContentState(securityBannerState = SecurityBannerState.RecoveryKeyConfirmation),
                    eventSink = eventsRecorder,
                ),
                onConfirmRecoveryKeyClick = callback,
            )
            rule.clickOn(CommonStrings.action_continue)
        }
    }

    @Test
    fun `clicking on start chat when the session has no room invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>(expectEvents = false)
        ensureCalledOnce { callback ->
            rule.setRoomListView(
                state = aRoomListState(
                    eventSink = eventsRecorder,
                    contentState = anEmptyContentState(),
                ),
                onCreateRoomClick = callback,
            )
            rule.clickOn(CommonStrings.action_start_chat)
        }
    }

    @Test
    fun `clicking on a room invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>(expectEvents = false)
        val state = aRoomListState(
            eventSink = eventsRecorder,
        )
        val room0 = state.contentAsRooms().summaries.first {
            it.displayType == RoomSummaryDisplayType.ROOM
        }
        ensureCalledOnceWithParam(room0.roomId) { callback ->
            rule.setRoomListView(
                state = state,
                onRoomClick = callback,
            )
            rule.onNodeWithText(room0.lastMessage!!.toString()).performClick()
        }
    }

    @Test
    fun `long clicking on a room emits the expected Event`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        val state = aRoomListState(
            eventSink = eventsRecorder,
        )
        val room0 = state.contentAsRooms().summaries.first {
            it.displayType == RoomSummaryDisplayType.ROOM
        }
        rule.setRoomListView(
            state = state,
        )
        rule.onNodeWithText(room0.lastMessage!!.toString()).performTouchInput { longClick() }
        eventsRecorder.assertSingle(RoomListEvents.ShowContextMenu(room0))
    }

    @Test
    fun `clicking on a room setting invokes the expected callback and emits expected Event`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        val state = aRoomListState(
            contextMenu = aContextMenuShown(),
            eventSink = eventsRecorder,
        )
        val room0 = (state.contextMenu as RoomListState.ContextMenu.Shown).roomId
        ensureCalledOnceWithParam(room0) { callback ->
            rule.setRoomListView(
                state = state,
                onRoomSettingsClick = callback,
            )
            rule.clickOn(CommonStrings.common_settings)
        }
        eventsRecorder.assertSingle(RoomListEvents.HideContextMenu)
    }

    @Test
    fun `clicking on accept and decline invite emits the expected Events`() {
        val eventsRecorder = EventsRecorder<RoomListEvents>()
        val state = aRoomListState(
            eventSink = eventsRecorder,
        )
        val invitedRoom = state.contentAsRooms().summaries.first {
            it.displayType == RoomSummaryDisplayType.INVITE
        }
        rule.setRoomListView(state = state)
        rule.clickOn(CommonStrings.action_accept)
        rule.clickOn(CommonStrings.action_decline)
        eventsRecorder.assertList(
            listOf(RoomListEvents.AcceptInvite(invitedRoom), RoomListEvents.DeclineInvite(invitedRoom)),
        )
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setRoomListView(
    state: RoomListState,
    onRoomClick: (RoomId) -> Unit = EnsureNeverCalledWithParam(),
    onSettingsClick: () -> Unit = EnsureNeverCalled(),
    onConfirmRecoveryKeyClick: () -> Unit = EnsureNeverCalled(),
    onCreateRoomClick: () -> Unit = EnsureNeverCalled(),
    onRoomSettingsClick: (RoomId) -> Unit = EnsureNeverCalledWithParam(),
    onMenuActionClick: (RoomListMenuAction) -> Unit = EnsureNeverCalledWithParam(),
    onRoomDirectorySearchClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        RoomListView(
            state = state,
            onRoomClick = onRoomClick,
            onSettingsClick = onSettingsClick,
            onConfirmRecoveryKeyClick = onConfirmRecoveryKeyClick,
            onCreateRoomClick = onCreateRoomClick,
            onRoomSettingsClick = onRoomSettingsClick,
            onMenuActionClick = onMenuActionClick,
            onRoomDirectorySearchClick = onRoomDirectorySearchClick,
            acceptDeclineInviteView = { },
        )
    }
}
