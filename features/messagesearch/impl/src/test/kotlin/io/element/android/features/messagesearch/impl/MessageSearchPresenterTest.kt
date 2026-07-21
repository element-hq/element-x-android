/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messagesearch.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.test.FakeDateFormatter
import io.element.android.libraries.eventformatter.api.RoomLatestEventFormatter
import io.element.android.libraries.eventformatter.test.FakeRoomLatestEventFormatter
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.search.MessageSearchPaginationState
import io.element.android.libraries.matrix.api.search.MessageSearchResult
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.search.FakeMessageSearch
import io.element.android.libraries.matrix.test.search.FakeMessageSearchService
import io.element.android.libraries.matrix.test.timeline.aMessageContent
import io.element.android.tests.testutils.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MessageSearchPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createMessageSearchPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.query).isEmpty()
            assertThat(initialState.results).isEmpty()
            assertThat(initialState.isSearching).isFalse()
            assertThat(initialState.displayInitialState).isTrue()
            assertThat(initialState.isRoomScoped).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - the search is scoped to the room it was created for`() = runTest {
        val service = FakeMessageSearchService()
        val presenter = createMessageSearchPresenter(
            roomId = A_ROOM_ID,
            matrixClient = FakeMatrixClient(messageSearchService = service),
        )
        presenter.test {
            awaitItem().also { state ->
                assertThat(state.isRoomScoped).isTrue()
            }
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(service.lastRoomId).isEqualTo(A_ROOM_ID)
        assertThat(service.createMessageSearchCallCount).isEqualTo(1)
    }

    @Test
    fun `present - a global search passes no room to the service`() = runTest {
        val service = FakeMessageSearchService()
        val presenter = createMessageSearchPresenter(
            roomId = null,
            matrixClient = FakeMatrixClient(messageSearchService = service),
        )
        presenter.test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(service.lastRoomId).isNull()
    }

    @Test
    fun `present - typing marks the state as searching before the debounce elapses`() = runTest {
        val messageSearch = FakeMessageSearch()
        val presenter = createMessageSearchPresenter(messageSearch = messageSearch)
        presenter.test {
            awaitItem().also { state ->
                state.eventSink(MessageSearchEvents.QueryChanged("hello"))
            }
            awaitItem().also { state ->
                assertThat(state.query).isEqualTo("hello")
            }
            // The indicator is driven by the raw keystroke, so it is already on while the
            // debounced query is still pending — this is what stops the empty state flashing.
            awaitItem().also { state ->
                assertThat(state.isSearching).isTrue()
                assertThat(state.displayEmptyState).isFalse()
            }
            assertThat(messageSearch.lastQuery).isNull()

            advanceUntilIdle()
            assertThat(messageSearch.lastQuery).isEqualTo("hello")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - a newer query supersedes one still inside the debounce window`() = runTest {
        val messageSearch = FakeMessageSearch()
        val presenter = createMessageSearchPresenter(messageSearch = messageSearch)
        presenter.test {
            awaitItem().also { state ->
                state.eventSink(MessageSearchEvents.QueryChanged("hel"))
            }
            skipItems(1)
            awaitItem().also { state ->
                state.eventSink(MessageSearchEvents.QueryChanged("hello"))
            }
            advanceUntilIdle()
            // Only the final query ever reached the SDK.
            assertThat(messageSearch.lastQuery).isEqualTo("hello")
            assertThat(messageSearch.setQueryCallCount).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - clearing the query returns to the initial state without searching`() = runTest {
        val messageSearch = FakeMessageSearch()
        val presenter = createMessageSearchPresenter(messageSearch = messageSearch)
        presenter.test {
            awaitItem().also { state ->
                state.eventSink(MessageSearchEvents.QueryChanged(""))
            }
            advanceUntilIdle()
            assertThat(messageSearch.lastQuery).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - a failed query is not presented as an empty result`() = runTest {
        val messageSearch = FakeMessageSearch(
            setQueryResult = { Result.failure(IllegalStateException("Search failed")) },
        ).apply {
            emitPaginationState(MessageSearchPaginationState.Idle(endReached = true))
        }
        val presenter = createMessageSearchPresenter(messageSearch = messageSearch)

        presenter.test {
            awaitItem().eventSink(MessageSearchEvents.QueryChanged("hello"))
            advanceUntilIdle()

            expectMostRecentItem().also { state ->
                assertThat(state.displayEmptyState).isFalse()
                assertThat(state.displayErrorState).isTrue()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - results are mapped for display`() = runTest {
        val messageSearch = FakeMessageSearch()
        val formatter = FakeRoomLatestEventFormatter().apply { givenFormatResult("a formatted preview") }
        val presenter = createMessageSearchPresenter(
            messageSearch = messageSearch,
            roomLatestEventFormatter = formatter,
        )
        presenter.test {
            awaitItem().also { state ->
                state.eventSink(MessageSearchEvents.QueryChanged("hello"))
            }
            advanceUntilIdle()
            messageSearch.emitResults(persistentListOf(aMessageSearchResult()))
            advanceUntilIdle()

            expectMostRecentItem().also { state ->
                assertThat(state.results).hasSize(1)
                val item = state.results.first()
                assertThat(item.eventId).isEqualTo(EventId("\$anEventId"))
                assertThat(item.roomId).isEqualTo(A_ROOM_ID)
                assertThat(item.preview).isEqualTo("a formatted preview")
                assertThat(item.senderName).isEqualTo(A_USER_ID.value)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - a room-scoped search paginates to the end while its room has nothing to show`() = runTest {
        val messageSearch = FakeMessageSearch(controllablePagination = true)
        val presenter = createMessageSearchPresenter(roomId = A_ROOM_ID, messageSearch = messageSearch)
        presenter.test {
            awaitItem().eventSink(MessageSearchEvents.QueryChanged("hello"))
            advanceTimeBy(251)
            runCurrent()

            // Globally-ranked results mean this room's matches may be arbitrarily deep in the
            // list, and the index is local, so the walk runs to the end rather than stopping at
            // an arbitrary page count. Seven pages pins that the old cap of five is gone.
            repeat(7) { completedPageCount ->
                assertThat(messageSearch.paginateCallCount).isEqualTo(completedPageCount + 1)
                assertThat(messageSearch.cancelledPaginateCallCount).isEqualTo(0)
                messageSearch.completePagination()
                runCurrent()
            }

            messageSearch.completePagination(endReached = true)
            advanceUntilIdle()
            assertThat(messageSearch.paginateCallCount).isEqualTo(8)
            expectMostRecentItem().also { state ->
                // Every page has been read, so "no results" is now an honest claim.
                assertThat(state.displayEmptyState).isTrue()
                assertThat(state.displayKeepLoadingPrompt).isFalse()
                assertThat(state.displaySearchingState).isFalse()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - a room-scoped search shows a searching state while the walk is running`() = runTest {
        val messageSearch = FakeMessageSearch(controllablePagination = true)
        val presenter = createMessageSearchPresenter(roomId = A_ROOM_ID, messageSearch = messageSearch)
        presenter.test {
            awaitItem().eventSink(MessageSearchEvents.QueryChanged("hello"))
            advanceTimeBy(251)
            runCurrent()

            assertThat(messageSearch.paginateCallCount).isEqualTo(1)
            expectMostRecentItem().also { state ->
                // Pages remain and the walk is in flight: neither "no results" nor a load-more
                // prompt would be honest, so the UI reports that it is still searching.
                assertThat(state.displaySearchingState).isTrue()
                assertThat(state.displayEmptyState).isFalse()
                assertThat(state.displayKeepLoadingPrompt).isFalse()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - auto-pagination stops as soon as the room has a result`() = runTest {
        val messageSearch = FakeMessageSearch()
        val presenter = createMessageSearchPresenter(roomId = A_ROOM_ID, messageSearch = messageSearch)
        presenter.test {
            awaitItem().also { state ->
                state.eventSink(MessageSearchEvents.QueryChanged("hello"))
            }
            messageSearch.emitResults(persistentListOf(aMessageSearchResult()))
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(messageSearch.paginateCallCount).isEqualTo(0)
    }

    @Test
    fun `present - a global search never auto-paginates`() = runTest {
        val messageSearch = FakeMessageSearch()
        val presenter = createMessageSearchPresenter(roomId = null, messageSearch = messageSearch)
        presenter.test {
            awaitItem().also { state ->
                state.eventSink(MessageSearchEvents.QueryChanged("hello"))
            }
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }
        // Global results are already ranked across every room, so there is nothing to skip past.
        assertThat(messageSearch.paginateCallCount).isEqualTo(0)
    }

    @Test
    fun `present - auto-pagination does not run once the end has been reached`() = runTest {
        val messageSearch = FakeMessageSearch().apply {
            emitPaginationState(MessageSearchPaginationState.Idle(endReached = true))
        }
        val presenter = createMessageSearchPresenter(roomId = A_ROOM_ID, messageSearch = messageSearch)
        presenter.test {
            awaitItem().also { state ->
                state.eventSink(MessageSearchEvents.QueryChanged("hello"))
            }
            advanceUntilIdle()
            awaitItem().also { state ->
                // Everything has been searched and this room genuinely has no match.
                assertThat(state.displayEmptyState).isTrue()
                assertThat(state.displayKeepLoadingPrompt).isFalse()
            }
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(messageSearch.paginateCallCount).isEqualTo(0)
    }

    @Test
    fun `present - a populated list keeps paginating while its end is on screen`() = runTest {
        // The bug this pins: pagination used to be driven by the row count growing. Room scoping is
        // applied below this presenter, so a page of globally-ranked hits with none for this room
        // leaves the count identical — which was read as "there is nothing more" and stopped the
        // search for good, under a spinner that never went away.
        val messageSearch = FakeMessageSearch(controllablePagination = true)
        val presenter = createMessageSearchPresenter(roomId = A_ROOM_ID, messageSearch = messageSearch)
        presenter.test {
            awaitItem().eventSink(MessageSearchEvents.QueryChanged("hello"))
            messageSearch.emitResults(persistentListOf(aMessageSearchResult()))
            advanceTimeBy(251)
            runCurrent()
            // The end of the list has not been reported yet, so nothing is fetched.
            assertThat(messageSearch.paginateCallCount).isEqualTo(0)

            expectMostRecentItem().eventSink(MessageSearchEvents.ListEndVisible(true))
            runCurrent()
            assertThat(messageSearch.paginateCallCount).isEqualTo(1)

            // The page lands and brings this room nothing. Pagination must continue anyway.
            messageSearch.completePagination()
            runCurrent()
            assertThat(messageSearch.paginateCallCount).isEqualTo(2)

            messageSearch.completePagination(endReached = true)
            runCurrent()
            assertThat(messageSearch.paginateCallCount).isEqualTo(2)
            assertThat(messageSearch.cancelledPaginateCallCount).isEqualTo(0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - a pagination state still reading Loading is waited for, not treated as the end`() = runTest {
        // The SDK publishes its pagination state from a listener on its own thread, so Loading can
        // outlive the paginate() call that caused it. Sampling the state instead of awaiting it
        // would end the search on that race, with nothing left to restart it.
        val messageSearch = FakeMessageSearch(controllablePagination = true).apply {
            emitPaginationState(MessageSearchPaginationState.Loading)
        }
        val presenter = createMessageSearchPresenter(roomId = A_ROOM_ID, messageSearch = messageSearch)
        presenter.test {
            awaitItem().eventSink(MessageSearchEvents.QueryChanged("hello"))
            advanceUntilIdle()
            assertThat(messageSearch.paginateCallCount).isEqualTo(0)

            messageSearch.emitPaginationState(MessageSearchPaginationState.Idle(endReached = false))
            runCurrent()

            assertThat(messageSearch.paginateCallCount).isEqualTo(1)
            messageSearch.completePagination(endReached = true)
            advanceUntilIdle()
            assertThat(messageSearch.paginateCallCount).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - scrolling away from the end stops after the page in flight`() = runTest {
        // Scroll position must not cancel a request that is already out: the SDK would be left
        // believing it is still loading, and its own paginate() no-ops in that state.
        val messageSearch = FakeMessageSearch(controllablePagination = true)
        val presenter = createMessageSearchPresenter(roomId = A_ROOM_ID, messageSearch = messageSearch)
        presenter.test {
            awaitItem().eventSink(MessageSearchEvents.QueryChanged("hello"))
            messageSearch.emitResults(persistentListOf(aMessageSearchResult()))
            advanceTimeBy(251)
            runCurrent()

            expectMostRecentItem().eventSink(MessageSearchEvents.ListEndVisible(true))
            runCurrent()
            assertThat(messageSearch.paginateCallCount).isEqualTo(1)

            expectMostRecentItem().eventSink(MessageSearchEvents.ListEndVisible(false))
            runCurrent()
            messageSearch.completePagination()
            advanceUntilIdle()

            assertThat(messageSearch.completedPaginateCallCount).isEqualTo(1)
            assertThat(messageSearch.cancelledPaginateCallCount).isEqualTo(0)
            assertThat(messageSearch.paginateCallCount).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - an explicit load more pulls another page for a global search`() = runTest {
        val messageSearch = FakeMessageSearch()
        val presenter = createMessageSearchPresenter(roomId = null, messageSearch = messageSearch)
        presenter.test {
            awaitItem().also { state ->
                state.eventSink(MessageSearchEvents.QueryChanged("hello"))
            }
            advanceUntilIdle()
            assertThat(messageSearch.paginateCallCount).isEqualTo(0)

            expectMostRecentItem().also { state ->
                state.eventSink(MessageSearchEvents.LoadMore)
            }
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(messageSearch.paginateCallCount).isEqualTo(1)
    }
}

private fun aMessageSearchResult(
    eventId: EventId = EventId("\$anEventId"),
    roomId: RoomId = A_ROOM_ID,
) = MessageSearchResult(
    roomId = roomId,
    eventId = eventId,
    senderId = A_USER_ID,
    senderProfile = ProfileDetails.Unavailable,
    content = aMessageContent(body = "hello world"),
    timestamp = 0L,
)

internal fun createMessageSearchPresenter(
    roomId: RoomId? = null,
    messageSearch: FakeMessageSearch = FakeMessageSearch(),
    matrixClient: FakeMatrixClient = FakeMatrixClient(
        messageSearchService = FakeMessageSearchService(messageSearch = messageSearch),
    ),
    roomLatestEventFormatter: RoomLatestEventFormatter = FakeRoomLatestEventFormatter(),
    dateFormatter: DateFormatter = FakeDateFormatter { _, _, _ -> "12:34" },
): MessageSearchPresenter {
    return MessageSearchPresenter(
        roomId = roomId,
        matrixClient = matrixClient,
        roomLatestEventFormatter = roomLatestEventFormatter,
        dateFormatter = dateFormatter,
    )
}
