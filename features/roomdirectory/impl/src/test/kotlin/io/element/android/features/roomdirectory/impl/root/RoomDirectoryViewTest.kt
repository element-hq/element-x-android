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

package io.element.android.features.roomdirectory.impl.root

import androidx.activity.ComponentActivity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.roomdirectory.api.RoomDescription
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDirectoryViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `typing text in search field emits the expected Event`() {
        val eventsRecorder = EventsRecorder<RoomDirectoryEvents>()
        rule.setRoomDirectoryView(
            state = aRoomDirectoryState(
                eventSink = eventsRecorder,
            )
        )
        rule.onNodeWithTag(TestTags.searchTextField.value).performTextInput(
            text = "Test"
        )
        eventsRecorder.assertSingle(RoomDirectoryEvents.Search("Test"))
    }

    @Test
    fun `clicking on room item then onResultClicked lambda is called once`() {
        val eventsRecorder = EventsRecorder<RoomDirectoryEvents>()
        val state = aRoomDirectoryState(
            roomDescriptions = aRoomDescriptionList(),
            eventSink = eventsRecorder,
        )
        val clickedRoom = state.roomDescriptions.first()
        ensureCalledOnceWithParam(clickedRoom) { callback ->
            rule.setRoomDirectoryView(
                state = state,
                onResultClicked = callback,
            )
            rule.onNodeWithText(clickedRoom.name).performClick()
        }
    }

    @Test
    fun `clicking on room item join cta emits the expected Event`() {
        val eventsRecorder = EventsRecorder<RoomDirectoryEvents>()
        val state = aRoomDirectoryState(
            roomDescriptions = aRoomDescriptionList(),
            eventSink = eventsRecorder,
        )
        rule.setRoomDirectoryView(state = state)
        val clickedRoom = state.roomDescriptions.first()
        rule.onAllNodesWithTag(TestTags.callToAction.value).onFirst().performClick()
        eventsRecorder.assertSingle(RoomDirectoryEvents.JoinRoom(clickedRoom.roomId))
    }

    @Test
    fun `composing load more indicator emits expected Event`() {
        val eventsRecorder = EventsRecorder<RoomDirectoryEvents>()
        val state = aRoomDirectoryState(
            displayLoadMoreIndicator = true,
            eventSink = eventsRecorder,
        )
        rule.setRoomDirectoryView(state = state)
        eventsRecorder.assertSingle(RoomDirectoryEvents.LoadMore)
    }

    @Test
    fun `when joining room with success then onRoomJoined lambda is called once`() {
        val eventsRecorder = EventsRecorder<RoomDirectoryEvents>(expectEvents = false)
        val roomDescriptions = aRoomDescriptionList()
        val joinedRoomId = roomDescriptions.first().roomId
        val state = aRoomDirectoryState(
            joinRoomAction = AsyncAction.Success(joinedRoomId),
            roomDescriptions = roomDescriptions,
            eventSink = eventsRecorder,
        )
        ensureCalledOnceWithParam(joinedRoomId) { callback ->
            rule.setRoomDirectoryView(
                state = state,
                onRoomJoined = callback,
            )
        }
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setRoomDirectoryView(
    state: RoomDirectoryState,
    onBackPressed: () -> Unit = EnsureNeverCalled(),
    onResultClicked: (RoomDescription) -> Unit = EnsureNeverCalledWithParam(),
    onRoomJoined: (RoomId) -> Unit = EnsureNeverCalledWithParam(),
) {
    setContent {
        RoomDirectoryView(
            state = state,
            onResultClicked = onResultClicked,
            onRoomJoined = onRoomJoined,
            onBackPressed = onBackPressed,
        )
    }
}
