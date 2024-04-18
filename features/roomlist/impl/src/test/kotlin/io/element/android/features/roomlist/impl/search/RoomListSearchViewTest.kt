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

package io.element.android.features.roomlist.impl.search

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.roomlist.impl.R
import io.element.android.features.roomlist.impl.RoomListEvents
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomListSearchViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on 'Browse all rooms' invokes the expected callback`() {
        val eventsRecorder = EventsRecorder<RoomListSearchEvents>(expectEvents = false)
        ensureCalledOnce {
            rule.setRoomListSearchView(
                aRoomListSearchState(
                    isSearchActive = true,
                    isRoomDirectorySearchEnabled = true,
                    eventSink = eventsRecorder,
                ),
                onRoomDirectorySearchClicked = it,
            )
            rule.clickOn(R.string.screen_roomlist_room_directory_button_title)
        }
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setRoomListSearchView(
    state: RoomListSearchState,
    eventSink: (RoomListEvents) -> Unit = EventsRecorder(expectEvents = false),
    onRoomClicked: (RoomId) -> Unit = EnsureNeverCalledWithParam(),
    onRoomDirectorySearchClicked: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        RoomListSearchView(
            state = state,
            eventSink = eventSink,
            onRoomClicked = onRoomClicked,
            onRoomDirectorySearchClicked = onRoomDirectorySearchClicked,
        )
    }
}

