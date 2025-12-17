/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdirectory.impl.root

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.roomdirectory.api.RoomDescription
import io.element.android.libraries.testtags.TestTags
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
    fun `clicking on room item then onResultClick lambda is called once`() {
        val eventsRecorder = EventsRecorder<RoomDirectoryEvents>()
        val state = aRoomDirectoryState(
            roomDescriptions = aRoomDescriptionList(),
            eventSink = eventsRecorder,
        )
        val clickedRoom = state.roomDescriptions.first()
        ensureCalledOnceWithParam(clickedRoom) { callback ->
            rule.setRoomDirectoryView(
                state = state,
                onResultClick = callback,
            )
            rule.onNodeWithText(clickedRoom.computedName).performClick()
        }
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
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setRoomDirectoryView(
    state: RoomDirectoryState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
    onResultClick: (RoomDescription) -> Unit = EnsureNeverCalledWithParam(),
) {
    setContent {
        RoomDirectoryView(
            state = state,
            onResultClick = onResultClick,
            onBackClick = onBackClick,
        )
    }
}
