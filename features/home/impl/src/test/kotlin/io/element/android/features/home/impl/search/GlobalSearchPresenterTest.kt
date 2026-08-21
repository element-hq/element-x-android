/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search

import com.google.common.truth.Truth.assertThat
import io.element.android.features.home.impl.datasource.aRoomListRoomSummaryFactory
import io.element.android.libraries.androidutils.filesize.FakeFileSizeFormatter
import io.element.android.libraries.androidutils.filesize.FileSizeFormatter
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.test.FakeDateFormatter
import io.element.android.libraries.eventformatter.api.RoomLatestEventFormatter
import io.element.android.libraries.eventformatter.test.FakeRoomLatestEventFormatter
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.search.MessageSearchService
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.matrix.test.roomlist.FakeDynamicRoomList
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.libraries.matrix.test.search.FakeMessageSearch
import io.element.android.libraries.matrix.test.search.FakeMessageSearchService
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Optional

class GlobalSearchPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createGlobalSearchPresenter()
        presenter.test {
            awaitItem().let { state ->
                assertThat(state.isEnabled).isFalse()
                assertThat(state.isSearchActive).isFalse()
                assertThat(state.queryState.text.toString()).isEmpty()
                assertThat(state.currentTarget).isEqualTo(GlobalSearchTarget.ROOMS)
                assertThat(state.results).isEqualTo(AsyncData.Uninitialized)
            }
        }
    }

    @Test
    fun `present - isEnabled reflects the feature flag`() = runTest {
        val presenter = createGlobalSearchPresenter(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(FeatureFlags.MessageSearch.key to true),
            ),
        )
        presenter.test {
            // isEnabled is emitted asynchronously via produceState, so it starts as false and flips to true
            var state = awaitItem()
            while (!state.isEnabled) {
                state = awaitItem()
            }
            assertThat(state.isEnabled).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - toggle search visibility`() = runTest {
        val presenter = createGlobalSearchPresenter()
        presenter.test {
            awaitItem().let { state ->
                assertThat(state.isSearchActive).isFalse()
                state.eventSink(GlobalSearchEvent.ToggleSearchVisibility)
            }
            awaitItem().let { state ->
                assertThat(state.isSearchActive).isTrue()
                state.eventSink(GlobalSearchEvent.ToggleSearchVisibility)
            }
            awaitItem().let { state ->
                assertThat(state.isSearchActive).isFalse()
            }
        }
    }

    @Test
    fun `present - update target`() = runTest {
        val presenter = createGlobalSearchPresenter()
        presenter.test {
            awaitItem().let { state ->
                assertThat(state.currentTarget).isEqualTo(GlobalSearchTarget.ROOMS)
                state.eventSink(GlobalSearchEvent.UpdateTarget(GlobalSearchTarget.MESSAGES))
            }
            awaitItem().let { state ->
                assertThat(state.currentTarget).isEqualTo(GlobalSearchTarget.MESSAGES)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - clear query`() = runTest {
        val presenter = createGlobalSearchPresenter()
        presenter.test {
            val initialState = awaitItem()
            initialState.queryState.edit { append("A query") }
            awaitItem().let { state ->
                assertThat(state.queryState.text.toString()).isEqualTo("A query")
                state.eventSink(GlobalSearchEvent.ClearQuery)
            }
            awaitItem().let { state ->
                assertThat(state.queryState.text.toString()).isEmpty()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - room search results are emitted when the room list changes`() = runTest {
        val roomList = FakeDynamicRoomList()
        val roomListService = FakeRoomListService(createRoomListLambda = { roomList })
        val presenter = createGlobalSearchPresenter(roomListService = roomListService)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.results).isEqualTo(AsyncData.Uninitialized)

            initialState.queryState.edit { append("A query") }
            // Let the query be propagated to the data source before emitting results
            testScheduler.advanceUntilIdle()
            roomList.summaries.emit(listOf(aRoomSummary()))

            // Wait for the presenter to emit a loading state before the success state
            consumeItemsUntilPredicate { it.results is AsyncData.Loading }

            val successState = awaitItem()
            val results = (successState.results.dataOrNull() as GlobalSearchResults.RoomListResults).results
            assertThat(results).hasSize(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - room search displays the uninitialized state when removing the query`() = runTest {
        val roomList = FakeDynamicRoomList()
        val roomListService = FakeRoomListService(createRoomListLambda = { roomList })
        val presenter = createGlobalSearchPresenter(roomListService = roomListService)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.results).isEqualTo(AsyncData.Uninitialized)

            initialState.queryState.edit { append("A query") }
            // Let the query be propagated to the data source before emitting results
            testScheduler.advanceUntilIdle()
            roomList.summaries.emit(listOf(aRoomSummary()))

            // Wait for the presenter to emit a loading state before the success state
            consumeItemsUntilPredicate { it.results is AsyncData.Loading }

            val successState = awaitItem()
            val results = (successState.results.dataOrNull() as GlobalSearchResults.RoomListResults).results
            assertThat(results).hasSize(1)

            // Remove the query
            successState.queryState.edit { replace(0, length, "") }
            // Skip the intermediate state where the query is empty but results are still present
            skipItems(1)
            // It's back to the uninitialized state
            assertThat(awaitItem().results).isInstanceOf(AsyncData.Uninitialized::class.java)
            // This happens even if for some reason the room results still contained any items
            assertThat(roomList.summaries.value).isNotEmpty()
        }
    }

    @Test
    fun `present - message search results are mapped when the message search emits`() = runTest {
        val messageSearch = FakeMessageSearch()
        val matrixClient = FakeMatrixClient().apply {
            getRoomInfoFlowLambda = { flowOf(Optional.of(aRoomInfo())) }
        }
        val presenter = createGlobalSearchPresenter(
            messageSearchService = FakeMessageSearchService(messageSearch),
            matrixClient = matrixClient,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(GlobalSearchEvent.UpdateTarget(GlobalSearchTarget.MESSAGES))
            initialState.queryState.edit { append("A query") }
            testScheduler.advanceUntilIdle()

            messageSearch.emitResults(persistentListOf(aMessageSearchResult()))
            testScheduler.advanceUntilIdle()

            val successState = consumeItemsUntilPredicate { it.results is AsyncData.Success }.last()
            val results = (successState.results.dataOrNull() as GlobalSearchResults.MessageSearchResults).results
            assertThat(results).hasSize(1)
            assertThat(results.first()).isInstanceOf(MessageSearchResultItem.Message::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - message search displays the uninitialized state after removing the query`() = runTest {
        val messageSearch = FakeMessageSearch()
        val matrixClient = FakeMatrixClient().apply {
            getRoomInfoFlowLambda = { flowOf(Optional.of(aRoomInfo())) }
        }
        val presenter = createGlobalSearchPresenter(
            messageSearchService = FakeMessageSearchService(messageSearch),
            matrixClient = matrixClient,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(GlobalSearchEvent.UpdateTarget(GlobalSearchTarget.MESSAGES))
            initialState.queryState.edit { append("A query") }
            testScheduler.advanceUntilIdle()

            messageSearch.emitResults(persistentListOf(aMessageSearchResult()))
            testScheduler.advanceUntilIdle()

            val successState = consumeItemsUntilPredicate { it.results is AsyncData.Success }.last()
            val results = (successState.results.dataOrNull() as GlobalSearchResults.MessageSearchResults).results
            assertThat(results).hasSize(1)
            assertThat(results.first()).isInstanceOf(MessageSearchResultItem.Message::class.java)

            // Remove the query
            successState.queryState.edit { replace(0, length, "") }
            // Skip the intermediate state where the query is empty but results are still present
            skipItems(1)
            // It's back to the uninitialized state
            assertThat(awaitItem().results).isInstanceOf(AsyncData.Uninitialized::class.java)
            // This happens even if for some reason the message results still contained any items
            assertThat(messageSearch.results.value).isNotEmpty()
        }
    }

    @Test
    fun `present - UpdateVisibleRange triggers pagination for messages when near the end`() = runTest {
        val messageSearch = FakeMessageSearch()
        val matrixClient = FakeMatrixClient().apply {
            getRoomInfoFlowLambda = { flowOf(Optional.of(aRoomInfo())) }
        }
        val presenter = createGlobalSearchPresenter(
            messageSearchService = FakeMessageSearchService(messageSearch),
            matrixClient = matrixClient,
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(GlobalSearchEvent.UpdateTarget(GlobalSearchTarget.MESSAGES))
            initialState.queryState.edit { append("A query") }
            testScheduler.advanceUntilIdle()

            messageSearch.emitResults(persistentListOf(aMessageSearchResult()))
            val successState = consumeItemsUntilPredicate { it.results is AsyncData.Success }.last()

            successState.eventSink(GlobalSearchEvent.UpdateVisibleRange(IntRange(0, 0)))
            runCurrent()
            testScheduler.advanceUntilIdle()

            assertThat(messageSearch.paginateCallCount).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private fun TestScope.createGlobalSearchPresenter(
    roomListService: RoomListService = FakeRoomListService(),
    messageSearchService: MessageSearchService = FakeMessageSearchService(),
    featureFlagService: FeatureFlagService = FakeFeatureFlagService(),
    latestEventFormatter: RoomLatestEventFormatter = FakeRoomLatestEventFormatter(),
    dateFormatter: DateFormatter = FakeDateFormatter(),
    fileSizeFormatter: FileSizeFormatter = FakeFileSizeFormatter(),
    permalinkParser: PermalinkParser = FakePermalinkParser(),
    matrixClient: MatrixClient = FakeMatrixClient(),
): GlobalSearchPresenter {
    return GlobalSearchPresenter(
        roomListSearchDataSourceFactory = object : RoomListSearchDataSource.Factory {
            override fun create(coroutineScope: CoroutineScope): RoomListSearchDataSource {
                return RoomListSearchDataSource(
                    coroutineScope = coroutineScope,
                    roomListService = roomListService,
                    coroutineDispatchers = testCoroutineDispatchers(),
                    roomSummaryFactory = aRoomListRoomSummaryFactory(),
                )
            }
        },
        messageSearchService = messageSearchService,
        featureFlagService = featureFlagService,
        latestEventFormatter = latestEventFormatter,
        dateFormatter = dateFormatter,
        fileSizeFormatter = fileSizeFormatter,
        permalinkParser = permalinkParser,
        coroutineDispatchers = testCoroutineDispatchers(),
        matrixClient = matrixClient,
    )
}
