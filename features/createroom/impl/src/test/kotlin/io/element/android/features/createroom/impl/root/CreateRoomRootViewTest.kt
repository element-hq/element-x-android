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

package io.element.android.features.createroom.impl.root

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.createroom.impl.R
import io.element.android.features.createroom.impl.userlist.aRecentDirectRoomList
import io.element.android.features.createroom.impl.userlist.aUserListState
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.ui.model.getBestName
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
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
class CreateRoomRootViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on back invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<CreateRoomRootEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setCreateRoomRootView(
                aCreateRoomRootState(
                    eventSink = eventsRecorder,
                ),
                onCloseClick = it
            )
            rule.pressBack()
        }
    }

    @Test
    fun `clicking on New room invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<CreateRoomRootEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setCreateRoomRootView(
                aCreateRoomRootState(
                    eventSink = eventsRecorder,
                ),
                onNewRoomClick = it
            )
            rule.clickOn(R.string.screen_create_room_action_create_room)
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on Invite people invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<CreateRoomRootEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setCreateRoomRootView(
                aCreateRoomRootState(
                    applicationName = "test",
                    eventSink = eventsRecorder,
                ),
                onInviteFriendsClick = it
            )
            val text = rule.activity.getString(CommonStrings.action_invite_friends_to_app, "test")
            rule.onNodeWithText(text).performClick()
        }
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking on a user suggestion invokes the expected callback`() {
        val recentDirectRoomList = aRecentDirectRoomList()
        val firstRoom = recentDirectRoomList[0]
        val eventsRecorder = EventsRecorder<CreateRoomRootEvents>(expectEvents = false)
        ensureCalledOnceWithParam(firstRoom.roomId) {
            rule.setCreateRoomRootView(
                aCreateRoomRootState(
                    userListState = aUserListState(
                        recentDirectRooms = recentDirectRoomList
                    ),
                    eventSink = eventsRecorder,
                ),
                onOpenDM = it
            )
            rule.onNodeWithText(firstRoom.matrixUser.getBestName()).performClick()
        }
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setCreateRoomRootView(
    state: CreateRoomRootState,
    onCloseClick: () -> Unit = EnsureNeverCalled(),
    onNewRoomClick: () -> Unit = EnsureNeverCalled(),
    onOpenDM: (RoomId) -> Unit = EnsureNeverCalledWithParam(),
    onInviteFriendsClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        CreateRoomRootView(
            state = state,
            onCloseClick = onCloseClick,
            onNewRoomClick = onNewRoomClick,
            onOpenDM = onOpenDM,
            onInviteFriendsClick = onInviteFriendsClick,
        )
    }
}
