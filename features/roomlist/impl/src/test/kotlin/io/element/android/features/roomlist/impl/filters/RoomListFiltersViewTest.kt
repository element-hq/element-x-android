/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.filters

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.features.roomlist.impl.R
import io.element.android.features.roomlist.impl.filters.selection.FilterSelectionState
import io.element.android.libraries.testtags.TestTags
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.pressTag
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomListFiltersViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking on filters generates expected Event`() {
        val eventsRecorder = EventsRecorder<RoomListFiltersEvents>()
        rule.setContent {
            RoomListFiltersView(
                state = aRoomListFiltersState(eventSink = eventsRecorder),
            )
        }
        rule.clickOn(R.string.screen_roomlist_filter_rooms)
        eventsRecorder.assertList(
            listOf(
                RoomListFiltersEvents.ToggleFilter(RoomListFilter.Rooms),
            )
        )
    }

    @Test
    fun `clicking on clear filters generates expected Event`() {
        val eventsRecorder = EventsRecorder<RoomListFiltersEvents>()
        rule.setContent {
            RoomListFiltersView(
                state = aRoomListFiltersState(
                    filterSelectionStates = RoomListFilter.entries.map { FilterSelectionState(it, isSelected = true) },
                    eventSink = eventsRecorder
                ),
            )
        }
        rule.pressTag(TestTags.homeScreenClearFilters.value)
        eventsRecorder.assertList(
            listOf(
                RoomListFiltersEvents.ClearSelectedFilters,
            )
        )
    }
}
