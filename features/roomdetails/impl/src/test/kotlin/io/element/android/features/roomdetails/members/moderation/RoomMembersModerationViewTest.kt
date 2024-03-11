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

package io.element.android.features.roomdetails.members.moderation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.roomdetails.impl.R
import io.element.android.features.roomdetails.impl.members.anAlice
import io.element.android.features.roomdetails.impl.members.moderation.ModerationAction
import io.element.android.features.roomdetails.impl.members.moderation.RoomMembersModerationEvents
import io.element.android.features.roomdetails.impl.members.moderation.RoomMembersModerationState
import io.element.android.features.roomdetails.impl.members.moderation.RoomMembersModerationView
import io.element.android.features.roomdetails.impl.members.moderation.aRoomMembersModerationState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBackKey
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class RoomMembersModerationViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Ignore("This test is not passing yet, need to investigate")
    @Test
    fun `clicking on back emits the expected event`() {
        val eventsRecorder = EventsRecorder<RoomMembersModerationEvents>()
        val roomMember = anAlice()
        val state = aRoomMembersModerationState(
            selectedRoomMember = roomMember,
            actions = listOf(
                ModerationAction.DisplayProfile(roomMember.userId),
            ),
            eventSink = eventsRecorder
        )
        rule.setRoomMembersModerationView(
            state = state,
        )
        rule.pressBackKey()
        // Give time for the bottom sheet to animate
        rule.mainClock.advanceTimeBy(1_000)
        eventsRecorder.assertSingle(RoomMembersModerationEvents.Reset)
    }

    @Test
    fun `clicking on 'See user info' invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<RoomMembersModerationEvents>(expectEvents = false)
        val roomMember = anAlice()
        val state = aRoomMembersModerationState(
            selectedRoomMember = roomMember,
            actions = listOf(
                ModerationAction.DisplayProfile(roomMember.userId),
            ),
            eventSink = eventsRecorder
        )
        ensureCalledOnceWithParam(roomMember.userId) { callback ->
            rule.setRoomMembersModerationView(
                state = state,
                onDisplayMemberProfile = callback
            )
            rule.clickOn(R.string.screen_room_member_list_manage_member_user_info)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on 'Remove member' emits the expected event`() {
        val eventsRecorder = EventsRecorder<RoomMembersModerationEvents>()
        val roomMember = anAlice()
        val state = aRoomMembersModerationState(
            selectedRoomMember = roomMember,
            actions = listOf(
                ModerationAction.DisplayProfile(roomMember.userId),
                ModerationAction.KickUser(roomMember.userId),
            ),
            eventSink = eventsRecorder
        )
        rule.setRoomMembersModerationView(
            state = state,
        )
        rule.clickOn(R.string.screen_room_member_list_manage_member_remove)
        // Give time for the bottom sheet to animate
        rule.mainClock.advanceTimeBy(1_000)
        eventsRecorder.assertSingle(RoomMembersModerationEvents.KickUser)
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on 'Remove and ban member' emits the expected event`() {
        val eventsRecorder = EventsRecorder<RoomMembersModerationEvents>()
        val roomMember = anAlice()
        val state = aRoomMembersModerationState(
            selectedRoomMember = roomMember,
            actions = listOf(
                ModerationAction.DisplayProfile(roomMember.userId),
                ModerationAction.KickUser(roomMember.userId),
                ModerationAction.BanUser(roomMember.userId),
            ),
            eventSink = eventsRecorder
        )
        rule.setRoomMembersModerationView(
            state = state,
        )
        // Note: the string key semantics is not perfect here :/
        rule.clickOn(R.string.screen_room_member_list_manage_member_remove_confirmation_ban)
        // Give time for the bottom sheet to animate
        rule.mainClock.advanceTimeBy(1_000)
        eventsRecorder.assertSingle(RoomMembersModerationEvents.BanUser)
    }

    @Test
    fun `cancelling 'Remove and ban member' confirmation emits the expected event`() {
        val eventsRecorder = EventsRecorder<RoomMembersModerationEvents>()
        val roomMember = anAlice()
        val state = aRoomMembersModerationState(
            selectedRoomMember = roomMember,
            banUserAsyncAction = AsyncAction.Confirming,
            eventSink = eventsRecorder
        )
        rule.setRoomMembersModerationView(
            state = state,
        )
        // Note: the string key semantics is not perfect here :/
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(RoomMembersModerationEvents.Reset)
    }

    @Test
    fun `confirming 'Remove and ban member' confirmation emits the expected event`() {
        val eventsRecorder = EventsRecorder<RoomMembersModerationEvents>()
        val roomMember = anAlice()
        val state = aRoomMembersModerationState(
            selectedRoomMember = roomMember,
            banUserAsyncAction = AsyncAction.Confirming,
            eventSink = eventsRecorder
        )
        rule.setRoomMembersModerationView(
            state = state,
        )
        // Note: the string key semantics is not perfect here :/
        rule.clickOn(R.string.screen_room_member_list_ban_member_confirmation_action)
        eventsRecorder.assertSingle(RoomMembersModerationEvents.BanUser)
    }

    @Test
    fun `cancelling 'Unban member' confirmation emits the expected event`() {
        val eventsRecorder = EventsRecorder<RoomMembersModerationEvents>()
        val roomMember = anAlice()
        val state = aRoomMembersModerationState(
            selectedRoomMember = roomMember,
            unbanUserAsyncAction = AsyncAction.Confirming,
            eventSink = eventsRecorder
        )
        rule.setRoomMembersModerationView(
            state = state,
        )
        // Note: the string key semantics is not perfect here :/
        rule.clickOn(CommonStrings.action_cancel)
        eventsRecorder.assertSingle(RoomMembersModerationEvents.Reset)
    }

    @Test
    fun `confirming 'Unban member' confirmation emits the expected event`() {
        val eventsRecorder = EventsRecorder<RoomMembersModerationEvents>()
        val roomMember = anAlice()
        val state = aRoomMembersModerationState(
            selectedRoomMember = roomMember,
            unbanUserAsyncAction = AsyncAction.Confirming,
            eventSink = eventsRecorder
        )
        rule.setRoomMembersModerationView(
            state = state,
        )
        // Note: the string key semantics is not perfect here :/
        rule.clickOn(R.string.screen_room_member_list_manage_member_unban_action)
        eventsRecorder.assertSingle(RoomMembersModerationEvents.UnbanUser)
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setRoomMembersModerationView(
    state: RoomMembersModerationState,
    onDisplayMemberProfile: (UserId) -> Unit = EnsureNeverCalledWithParam()
) {
    setContent {
        RoomMembersModerationView(
            state = state,
            onDisplayMemberProfile = onDisplayMemberProfile,
        )
    }
}
