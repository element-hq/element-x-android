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

package io.element.android.features.roomdetails.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureCalledOnceWithTwoParams
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EnsureNeverCalledWithTwoParams
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBack
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class RoomDetailsViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `click on back invokes expected callback`() {
        ensureCalledOnce { callback ->
            rule.setRoomDetailView(
                goBack = callback,
            )
            rule.pressBack()
        }
    }

    @Test
    fun `click on share invokes expected callback`() {
        ensureCalledOnce { callback ->
            rule.setRoomDetailView(
                onShareRoom = callback,
            )
            rule.clickOn(CommonStrings.action_share)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `click on room members invokes expected callback`() {
        ensureCalledOnce { callback ->
            rule.setRoomDetailView(
                openRoomMemberList = callback,
            )
            rule.clickOn(CommonStrings.common_people)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `click on polls invokes expected callback`() {
        ensureCalledOnce { callback ->
            rule.setRoomDetailView(
                openPollHistory = callback,
            )
            rule.clickOn(R.string.screen_polls_history_title)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `click on notification invokes expected callback`() {
        ensureCalledOnce { callback ->
            rule.setRoomDetailView(
                openRoomNotificationSettings = callback,
            )
            rule.clickOn(R.string.screen_room_details_notification_title)
        }
    }

    @Test
    fun `click on invite invokes expected callback`() {
        ensureCalledOnce { callback ->
            rule.setRoomDetailView(
                state = aRoomDetailsState(
                    eventSink = EventsRecorder(expectEvents = false),
                    canInvite = true,
                ),
                invitePeople = callback,
            )
            rule.clickOn(CommonStrings.action_invite)
        }
    }

    @Test
    fun `click on call invokes expected callback`() {
        ensureCalledOnce { callback ->
            rule.setRoomDetailView(
                state = aRoomDetailsState(
                    eventSink = EventsRecorder(expectEvents = false),
                    canInvite = true,
                ),
                onJoinCallClicked = callback,
            )
            rule.clickOn(CommonStrings.action_call)
        }
    }

    @Test
    fun `click on add topic emit expected event`() {
        ensureCalledOnceWithParam<RoomDetailsAction>(RoomDetailsAction.AddTopic) { callback ->
            rule.setRoomDetailView(
                state = aRoomDetailsState(
                    eventSink = EventsRecorder(expectEvents = false),
                    roomTopic = RoomTopicState.CanAddTopic,
                ),
                onActionClicked = callback,
            )
            rule.clickOn(R.string.screen_room_details_add_topic_title)
        }
    }

    @Test
    fun `click on menu edit emit expected event`() {
        ensureCalledOnceWithParam<RoomDetailsAction>(RoomDetailsAction.Edit) { callback ->
            rule.setRoomDetailView(
                state = aRoomDetailsState(
                    eventSink = EventsRecorder(expectEvents = false),
                    canEdit = true,
                ),
                onActionClicked = callback,
            )
            val menuContentDescription = rule.activity.getString(CommonStrings.a11y_user_menu)
            rule.onNodeWithContentDescription(menuContentDescription).performClick()
            rule.clickOn(CommonStrings.action_edit)
        }
    }

    @Test
    fun `click on avatar test`() {
        val eventsRecorder = EventsRecorder<RoomDetailsEvent>(expectEvents = false)
        val state = aRoomDetailsState(
            eventSink = eventsRecorder,
            roomAvatarUrl = "an_avatar_url",
        )
        val callback = EnsureCalledOnceWithTwoParams(state.roomName, "an_avatar_url")
        rule.setRoomDetailView(
            state = state,
            openAvatarPreview = callback,
        )
        rule.onNodeWithTag(TestTags.roomDetailAvatar.value).performClick()
        callback.assertSuccess()
    }

    @Test
    fun `click on avatar test on DM`() {
        val eventsRecorder = EventsRecorder<RoomDetailsEvent>(expectEvents = false)
        val state = aRoomDetailsState(
            roomType = RoomDetailsType.Dm(aDmRoomMember(avatarUrl = "an_avatar_url")),
            eventSink = eventsRecorder,
        )
        val callback = EnsureCalledOnceWithTwoParams("Daniel", "an_avatar_url")
        rule.setRoomDetailView(
            state = state,
            openAvatarPreview = callback,
        )
        rule.onNodeWithTag(TestTags.memberDetailAvatar.value).performClick()
        callback.assertSuccess()
    }

    @Test
    fun `click on mute emit expected event`() {
        val eventsRecorder = EventsRecorder<RoomDetailsEvent>()
        val state = aRoomDetailsState(
            eventSink = eventsRecorder,
            roomNotificationSettings = aRoomNotificationSettings(mode = RoomNotificationMode.ALL_MESSAGES),
        )
        rule.setRoomDetailView(
            state = state,
        )
        rule.clickOn(CommonStrings.common_mute)
        eventsRecorder.assertSingle(RoomDetailsEvent.MuteNotification)
    }

    @Test
    fun `click on unmute emit expected event`() {
        val eventsRecorder = EventsRecorder<RoomDetailsEvent>()
        val state = aRoomDetailsState(
            eventSink = eventsRecorder,
            roomNotificationSettings = aRoomNotificationSettings(mode = RoomNotificationMode.MUTE),
        )
        rule.setRoomDetailView(
            state = state,
        )
        rule.clickOn(CommonStrings.common_unmute)
        eventsRecorder.assertSingle(RoomDetailsEvent.UnmuteNotification)
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `click on favorite emit expected Event`() {
        val eventsRecorder = EventsRecorder<RoomDetailsEvent>()
        rule.setRoomDetailView(
            state = aRoomDetailsState(
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.common_favourite)
        eventsRecorder.assertSingle(RoomDetailsEvent.SetFavorite(true))
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `click on leave emit expected Event`() {
        val eventsRecorder = EventsRecorder<RoomDetailsEvent>()
        rule.setRoomDetailView(
            state = aRoomDetailsState(
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(R.string.screen_room_details_leave_room_title)
        eventsRecorder.assertSingle(RoomDetailsEvent.LeaveRoom)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setRoomDetailView(
    state: RoomDetailsState = aRoomDetailsState(
        eventSink = EventsRecorder(expectEvents = false),
    ),
    goBack: () -> Unit = EnsureNeverCalled(),
    onActionClicked: (RoomDetailsAction) -> Unit = EnsureNeverCalledWithParam(),
    onShareRoom: () -> Unit = EnsureNeverCalled(),
    onShareMember: (RoomMember) -> Unit = EnsureNeverCalledWithParam(),
    openRoomMemberList: () -> Unit = EnsureNeverCalled(),
    openRoomNotificationSettings: () -> Unit = EnsureNeverCalled(),
    invitePeople: () -> Unit = EnsureNeverCalled(),
    openAvatarPreview: (name: String, url: String) -> Unit = EnsureNeverCalledWithTwoParams(),
    openPollHistory: () -> Unit = EnsureNeverCalled(),
    openAdminSettings: () -> Unit = EnsureNeverCalled(),
    onJoinCallClicked: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        RoomDetailsView(
            state = state,
            goBack = goBack,
            onActionClicked = onActionClicked,
            onShareRoom = onShareRoom,
            onShareMember = onShareMember,
            openRoomMemberList = openRoomMemberList,
            openRoomNotificationSettings = openRoomNotificationSettings,
            invitePeople = invitePeople,
            openAvatarPreview = openAvatarPreview,
            openPollHistory = openPollHistory,
            openAdminSettings = openAdminSettings,
            onJoinCallClicked = onJoinCallClicked,
        )
    }
}
