/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.home.impl.search

import androidx.activity.ComponentActivity
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import io.element.android.features.home.impl.R
import io.element.android.features.home.impl.model.aRoomListRoomSummary
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureCalledOnceWithTwoParams
import io.element.android.tests.testutils.EnsureNeverCalledWithTwoParams
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.pressBack
import io.element.android.tests.testutils.pressBackKey
import io.element.android.tests.testutils.robolectric.RobolectricTest
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

class GlobalSearchViewTest : RobolectricTest() {
    @Test
    fun `back button - emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<GlobalSearchEvent>()
        setGlobalSearchView(
            state = aGlobalSearchState(
                isSearchActive = true,
                eventSink = eventsRecorder,
            ),
        )
        // Remove automatic initial events
        eventsRecorder.clear()
        pressBack()
        eventsRecorder.assertSingle(GlobalSearchEvent.ToggleSearchVisibility)
    }

    @Test
    fun `back key - with search active toggles the search`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<GlobalSearchEvent>()
        setGlobalSearchView(
            state = aGlobalSearchState(
                isSearchActive = true,
                eventSink = eventsRecorder,
            ),
        )
        // Remove automatic initial events
        eventsRecorder.clear()
        pressBackKey()

        // Advance time to let the event be processed, as the search toggle might have some delay (e.g. for the animation)
        mainClock.advanceTimeBy(1)

        eventsRecorder.assertSingle(GlobalSearchEvent.ToggleSearchVisibility)
    }

    @Test
    fun `clear query button - emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<GlobalSearchEvent>()
        setGlobalSearchView(
            state = aGlobalSearchState(
                isSearchActive = true,
                queryState = TextFieldState("Query"),
                eventSink = eventsRecorder,
            ),
        )
        // Remove automatic initial events
        eventsRecorder.clear()
        val clearSearch = activity!!.getString(CommonStrings.a11y_clear_search_field)
        onNodeWithContentDescription(clearSearch).performClick()
        eventsRecorder.assertSingle(GlobalSearchEvent.ClearQuery)
    }

    @Test
    fun `search target selector - clicking on 'Chats' emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<GlobalSearchEvent>()
        setGlobalSearchView(
            state = aGlobalSearchState(
                isSearchActive = true,
                currentTarget = GlobalSearchTarget.MESSAGES,
                queryState = TextFieldState("Query"),
                eventSink = eventsRecorder,
            ),
        )
        // Remove automatic initial events
        eventsRecorder.clear()
        clickOn(R.string.search_section_chats)
        eventsRecorder.assertSingle(GlobalSearchEvent.UpdateTarget(GlobalSearchTarget.ROOMS))
    }

    @Test
    fun `search target selector - clicking on 'Messages' emits the expected Event`() = runAndroidComposeUiTest {
        val eventsRecorder = EventsRecorder<GlobalSearchEvent>()
        setGlobalSearchView(
            state = aGlobalSearchState(
                isSearchActive = true,
                currentTarget = GlobalSearchTarget.ROOMS,
                queryState = TextFieldState("Query"),
                eventSink = eventsRecorder,
            ),
        )
        // Remove automatic initial events
        eventsRecorder.clear()
        clickOn(R.string.search_section_messages)
        eventsRecorder.assertSingle(GlobalSearchEvent.UpdateTarget(GlobalSearchTarget.MESSAGES))
    }

    @Test
    fun `clicking on a room result - invokes the callback and saves it to the history`() = runAndroidComposeUiTest {
        val roomId = RoomId("!aRoom:server.org")
        val eventsRecorder = EventsRecorder<GlobalSearchEvent>()
        val onSelectSearchResult = EnsureCalledOnceWithTwoParams<RoomId, EventId?>(roomId, null)
        setGlobalSearchView(
            state = aGlobalSearchState(
                isSearchActive = true,
                currentTarget = GlobalSearchTarget.ROOMS,
                queryState = TextFieldState("Query"),
                results = AsyncData.Success(
                    GlobalSearchResults.RoomListResults(
                        persistentListOf(
                            aRoomListRoomSummary(id = roomId.value, name = "A room result"),
                        )
                    )
                ),
                eventSink = eventsRecorder,
            ),
            onSelectSearchResult = onSelectSearchResult,
        )
        // Remove automatic initial events
        eventsRecorder.clear()
        onNodeWithText("A room result").performClick()
        onSelectSearchResult.assertSuccess()
        eventsRecorder.assertSingle(GlobalSearchEvent.SaveRoomToHistory(roomId))
    }

    @Test
    fun `clicking on a message result - invokes the callback and saves it to the history`() = runAndroidComposeUiTest {
        val roomId = RoomId("!aMessageRoom:server.org")
        val eventId = EventId("\$anEvent:server.org")
        val eventsRecorder = EventsRecorder<GlobalSearchEvent>()
        val onSelectSearchResult = EnsureCalledOnceWithTwoParams<RoomId, EventId?>(roomId, eventId)
        setGlobalSearchView(
            state = aGlobalSearchState(
                isSearchActive = true,
                currentTarget = GlobalSearchTarget.MESSAGES,
                queryState = TextFieldState("Query"),
                results = AsyncData.Success(
                    GlobalSearchResults.MessageSearchResults(
                        persistentListOf(
                            MessageSearchResultItem.Message(
                                messageSearchResult = aMessageSearchResult(roomId = roomId, eventId = eventId),
                                body = "A message body",
                                roomInfo = aRoomInfo(id = roomId, name = "A message room"),
                                formattedTimestamp = "12:00",
                            ),
                        )
                    )
                ),
                eventSink = eventsRecorder,
            ),
            onSelectSearchResult = onSelectSearchResult,
        )
        // Remove automatic initial events
        eventsRecorder.clear()
        onNodeWithText("A message room").performClick()
        onSelectSearchResult.assertSuccess()
        eventsRecorder.assertSingle(GlobalSearchEvent.SaveRoomToHistory(roomId))
    }

    @Test
    fun `clicking on a history query result - emits the expected Event`() = runAndroidComposeUiTest {
        val queryResult = SearchHistoryResultItem.Query("Recent term")
        val eventsRecorder = EventsRecorder<GlobalSearchEvent>()
        setGlobalSearchView(
            state = aGlobalSearchState(
                isSearchActive = true,
                queryState = TextFieldState(),
                results = AsyncData.Uninitialized,
                history = AsyncData.Success(persistentListOf(queryResult)),
                eventSink = eventsRecorder,
            ),
            // A query history result must not trigger the search result callback
            onSelectSearchResult = EnsureNeverCalledWithTwoParams(),
        )
        // Remove automatic initial events
        eventsRecorder.clear()
        onNodeWithText("Recent term").performClick()
        eventsRecorder.assertSingle(GlobalSearchEvent.SearchHistoryResultSelected(queryResult))
    }

    @Test
    fun `clicking on a history room result - invokes the callback and emits the expected Events`() = runAndroidComposeUiTest {
        val roomId = RoomId("!aHistoryRoom:server.org")
        val roomResult = SearchHistoryResultItem.Room(
            roomId = roomId,
            roomInfo = aRoomInfo(id = roomId, name = "A history room"),
        )
        val eventsRecorder = EventsRecorder<GlobalSearchEvent>()
        val onSelectSearchResult = EnsureCalledOnceWithTwoParams<RoomId, EventId?>(roomId, null)
        setGlobalSearchView(
            state = aGlobalSearchState(
                isSearchActive = true,
                queryState = TextFieldState(),
                results = AsyncData.Uninitialized,
                history = AsyncData.Success(persistentListOf(roomResult)),
                eventSink = eventsRecorder,
            ),
            onSelectSearchResult = onSelectSearchResult,
        )
        // Remove automatic initial events
        eventsRecorder.clear()
        onNodeWithText("A history room").performClick()
        onSelectSearchResult.assertSuccess()
        eventsRecorder.assertList(
            listOf(
                GlobalSearchEvent.SaveRoomToHistory(roomId),
                GlobalSearchEvent.SearchHistoryResultSelected(roomResult),
            )
        )
    }

    @Test
    fun `with a query and uninitialized results - displays the start searching placeholder`() = runAndroidComposeUiTest {
        setGlobalSearchView(
            state = aGlobalSearchState(
                isSearchActive = true,
                queryState = TextFieldState("Query"),
                results = AsyncData.Uninitialized,
            ),
        )
        val startSearching = activity!!.getString(R.string.start_searching_title)
        onNodeWithText(startSearching).assertIsDisplayed()
    }

    @Test
    fun `with empty results - displays the no results placeholder`() = runAndroidComposeUiTest {
        setGlobalSearchView(
            state = aGlobalSearchState(
                isSearchActive = true,
                currentTarget = GlobalSearchTarget.ROOMS,
                queryState = TextFieldState("Query"),
                results = AsyncData.Success(GlobalSearchResults.RoomListResults(persistentListOf())),
            ),
        )
        val noResults = activity!!.getString(R.string.search_no_results_title)
        onNodeWithText(noResults).assertIsDisplayed()
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setGlobalSearchView(
        state: GlobalSearchState,
        onSelectSearchResult: (RoomId, EventId?) -> Unit = EnsureNeverCalledWithTwoParams(),
    ) {
        setContent {
            GlobalSearchView(
                state = state,
                onSelectSearchResult = onSelectSearchResult,
            )
        }
    }
}
