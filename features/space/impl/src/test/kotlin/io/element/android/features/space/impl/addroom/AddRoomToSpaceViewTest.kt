/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.addroom

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class AddRoomToSpaceViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking back when search inactive invokes onBackClick`() {
        ensureCalledOnce {
            rule.setAddRoomToSpaceView(
                anAddRoomToSpaceState(
                    isSearchActive = false,
                ),
                onBackClick = it,
            )
            rule.pressBack()
        }
    }

    @Test
    fun `clicking back when search active emits CloseSearch event`() {
        val eventsRecorder = EventsRecorder<AddRoomToSpaceEvent>()
        rule.setAddRoomToSpaceView(
            anAddRoomToSpaceState(
                isSearchActive = true,
                eventSink = eventsRecorder,
            ),
        )
        rule.pressBack()
        eventsRecorder.assertSingle(AddRoomToSpaceEvent.CloseSearch)
    }

    @Test
    fun `clicking save emits Save event`() {
        val eventsRecorder = EventsRecorder<AddRoomToSpaceEvent>()
        rule.setAddRoomToSpaceView(
            anAddRoomToSpaceState(
                selectedRooms = aSelectRoomInfoList().take(1).toImmutableList(),
                eventSink = eventsRecorder,
            ),
        )
        rule.clickOn(CommonStrings.action_save)
        eventsRecorder.assertList(
            listOf(
                AddRoomToSpaceEvent.UpdateSearchQuery(""), // SearchBar initialization
                AddRoomToSpaceEvent.Save,
            )
        )
    }

    @Config(qualifiers = "h1024dp")
    @Test
    fun `clicking room in suggestions emits ToggleRoom event`() {
        val suggestions = aSelectRoomInfoList()
        val eventsRecorder = EventsRecorder<AddRoomToSpaceEvent>()
        rule.setAddRoomToSpaceView(
            anAddRoomToSpaceState(
                suggestions = suggestions,
                eventSink = eventsRecorder,
            ),
        )
        rule.onNodeWithText(suggestions.first().name!!).performClick()
        eventsRecorder.assertList(
            listOf(
                AddRoomToSpaceEvent.UpdateSearchQuery(""), // SearchBar initialization
                AddRoomToSpaceEvent.ToggleRoom(suggestions.first()),
            )
        )
    }

    @Test
    fun `onRoomsAdded called when saveAction is Success`() {
        ensureCalledOnce {
            rule.setAddRoomToSpaceView(
                anAddRoomToSpaceState(
                    saveAction = AsyncAction.Success(Unit),
                ),
                onRoomsAdded = it,
            )
        }
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setAddRoomToSpaceView(
    state: AddRoomToSpaceState,
    onBackClick: () -> Unit = EnsureNeverCalled(),
    onRoomsAdded: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        AddRoomToSpaceView(
            state = state,
            onBackClick = onBackClick,
            onRoomsAdded = onRoomsAdded,
        )
    }
}
