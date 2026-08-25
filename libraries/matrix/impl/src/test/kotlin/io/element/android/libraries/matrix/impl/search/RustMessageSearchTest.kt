/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.libraries.matrix.impl.search

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.search.MessageSearchPaginationState
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustSearchServiceResult
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiSearchService
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.SearchServiceResultsUpdate
import uniffi.matrix_sdk_ui.SearchServicePaginationState

class RustMessageSearchTest {
    @Test
    fun `paginationState is seeded from the SDK and reflects later updates`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(dispatcher)
        val service = FakeFfiSearchService(initialPaginationState = SearchServicePaginationState.Idle(endReached = true))
        val search = RustMessageSearch(inner = service, onClose = {}, scope = scope, dispatcher = dispatcher)

        assertThat(search.paginationState.value).isEqualTo(MessageSearchPaginationState.Idle(endReached = true))

        service.paginationStateListener!!.onUpdate(SearchServicePaginationState.Loading)
        assertThat(search.paginationState.value).isEqualTo(MessageSearchPaginationState.Loading)

        scope.cancel()
    }

    @Test
    fun `setQuery failure surfaces as Result failure without throwing`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(dispatcher)
        val service = FakeFfiSearchService(setQueryLambda = { error("boom") })
        val search = RustMessageSearch(inner = service, onClose = {}, scope = scope, dispatcher = dispatcher)

        val result = search.setQuery("hello")

        assertThat(result.isFailure).isTrue()
        scope.cancel()
    }

    @Test
    fun `the query handed to the SDK is escaped and made conjunctive for tantivy`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(dispatcher)
        var queryReceived: String? = null
        val service = FakeFfiSearchService(setQueryLambda = { queryReceived = it })
        val search = RustMessageSearch(inner = service, onClose = {}, scope = scope, dispatcher = dispatcher)

        // Unescaped, tantivy reads `https` as a field name and fails the entire search. The `+`
        // makes the term mandatory: without it tantivy is OR-by-default and a multi-word query
        // matches anything containing any one of its words.
        search.setQuery("https://github.com/foo")

        assertThat(queryReceived).isEqualTo("+https\\://github.com/foo")

        scope.cancel()
    }

    @Test
    fun `paginate failure surfaces as Result failure without throwing`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(dispatcher)
        val service = FakeFfiSearchService(paginateLambda = { error("boom") })
        val search = RustMessageSearch(inner = service, onClose = {}, scope = scope, dispatcher = dispatcher)

        val result = search.paginate()

        assertThat(result.isFailure).isTrue()
        scope.cancel()
    }

    @Test
    fun `results are not subscribed to until the first setQuery, and only once`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(dispatcher)
        val service = FakeFfiSearchService()
        val search = RustMessageSearch(inner = service, onClose = {}, scope = scope, dispatcher = dispatcher)

        assertThat(service.subscribeToResultsCallCount).isEqualTo(0)

        search.setQuery("hello")
        assertThat(service.subscribeToResultsCallCount).isEqualTo(1)

        search.setQuery("world")
        assertThat(service.subscribeToResultsCallCount).isEqualTo(1)

        scope.cancel()
    }

    @Test
    fun `updates pushed by the SDK reach the results flow`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(dispatcher)
        val service = FakeFfiSearchService()
        val search = RustMessageSearch(inner = service, onClose = {}, scope = scope, dispatcher = dispatcher)

        search.setQuery("hello")
        service.resultsListener!!.onUpdate(
            listOf(SearchServiceResultsUpdate.Append(listOf(aRustSearchServiceResult("\$1"), aRustSearchServiceResult("\$2"))))
        )
        advanceUntilIdle()

        assertThat(search.results.value.map { it.eventId.value }).isEqualTo(listOf("\$1", "\$2"))

        scope.cancel()
    }

    @Test
    fun `a room filter hides other rooms without disturbing SDK indexing`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(dispatcher)
        val service = FakeFfiSearchService()
        val search = RustMessageSearch(inner = service, onClose = {}, scope = scope, dispatcher = dispatcher, roomId = A_ROOM_ID)

        search.setQuery("hello")
        // SDK list: [$1@room1, $2@room2, $3@room1, $4@room2]
        service.resultsListener!!.onUpdate(
            listOf(
                SearchServiceResultsUpdate.Append(
                    listOf(
                        aRustSearchServiceResult("\$1", roomId = A_ROOM_ID.value),
                        aRustSearchServiceResult("\$2", roomId = A_ROOM_ID_2.value),
                        aRustSearchServiceResult("\$3", roomId = A_ROOM_ID.value),
                        aRustSearchServiceResult("\$4", roomId = A_ROOM_ID_2.value),
                    )
                )
            )
        )
        advanceUntilIdle()

        assertThat(search.results.value.map { it.eventId.value }).isEqualTo(listOf("\$1", "\$3"))

        // Index 2 in the SDK's list is $3. Had the filter been applied inside the processor, the
        // filtered list would be [$1, $3] and index 2 would be out of bounds — the iOS bug.
        service.resultsListener!!.onUpdate(
            listOf(SearchServiceResultsUpdate.Set(index = 2u, value = aRustSearchServiceResult("\$3edited", roomId = A_ROOM_ID.value)))
        )
        advanceUntilIdle()

        assertThat(search.results.value.map { it.eventId.value }).isEqualTo(listOf("\$1", "\$3edited"))

        // Removing index 1 ($2, a room we filter out) must not shift anything the user can see.
        service.resultsListener!!.onUpdate(listOf(SearchServiceResultsUpdate.Remove(index = 1u)))
        advanceUntilIdle()

        assertThat(search.results.value.map { it.eventId.value }).isEqualTo(listOf("\$1", "\$3edited"))

        scope.cancel()
    }

    @Test
    fun `no room filter exposes every room`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(dispatcher)
        val service = FakeFfiSearchService()
        val search = RustMessageSearch(inner = service, onClose = {}, scope = scope, dispatcher = dispatcher, roomId = null)

        search.setQuery("hello")
        service.resultsListener!!.onUpdate(
            listOf(
                SearchServiceResultsUpdate.Append(
                    listOf(
                        aRustSearchServiceResult("\$1", roomId = A_ROOM_ID.value),
                        aRustSearchServiceResult("\$2", roomId = A_ROOM_ID_2.value),
                    )
                )
            )
        )
        advanceUntilIdle()

        assertThat(search.results.value.map { it.eventId.value }).isEqualTo(listOf("\$1", "\$2"))

        scope.cancel()
    }

    @Test
    fun `cancelling the scope closes the underlying service`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(dispatcher)
        var closed = false
        val service = FakeFfiSearchService()
        RustMessageSearch(inner = service, onClose = { closed = true }, scope = scope, dispatcher = dispatcher)

        assertThat(closed).isFalse()

        scope.cancel()
        advanceUntilIdle()

        assertThat(closed).isTrue()
    }
}
