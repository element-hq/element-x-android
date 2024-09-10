/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
                onRoomDirectorySearchClick = it,
            )
            rule.clickOn(R.string.screen_roomlist_room_directory_button_title)
        }
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setRoomListSearchView(
    state: RoomListSearchState,
    eventSink: (RoomListEvents) -> Unit = EventsRecorder(expectEvents = false),
    onRoomClick: (RoomId) -> Unit = EnsureNeverCalledWithParam(),
    onRoomDirectorySearchClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        RoomListSearchView(
            state = state,
            eventSink = eventSink,
            onRoomClick = onRoomClick,
            onRoomDirectorySearchClick = onRoomDirectorySearchClick,
        )
    }
}
