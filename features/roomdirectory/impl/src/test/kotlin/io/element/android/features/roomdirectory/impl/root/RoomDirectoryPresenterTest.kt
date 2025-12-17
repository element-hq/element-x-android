/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdirectory.impl.root

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryList
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryService
import io.element.android.libraries.matrix.test.roomdirectory.FakeRoomDirectoryList
import io.element.android.libraries.matrix.test.roomdirectory.FakeRoomDirectoryService
import io.element.android.libraries.matrix.test.roomdirectory.aRoomDescription
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoomDirectoryPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createRoomDirectoryPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.query).isEmpty()
            assertThat(initialState.displayEmptyState).isFalse()
            assertThat(initialState.roomDescriptions).isEmpty()
            assertThat(initialState.displayLoadMoreIndicator).isTrue()
        }
    }

    @Test
    fun `present - room directory list emits empty state`() = runTest {
        val directoryListStateFlow = MutableSharedFlow<RoomDirectoryList.SearchResult>(replay = 1)
        val roomDirectoryList = FakeRoomDirectoryList(directoryListStateFlow)
        val roomDirectoryService = FakeRoomDirectoryService { roomDirectoryList }
        val presenter = createRoomDirectoryPresenter(roomDirectoryService = roomDirectoryService)
        presenter.test {
            skipItems(1)
            directoryListStateFlow.emit(
                RoomDirectoryList.SearchResult(false, emptyList())
            )
            awaitItem().also { state ->
                assertThat(state.displayEmptyState).isTrue()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - room directory list emits non-empty state`() = runTest {
        val directoryListStateFlow = MutableSharedFlow<RoomDirectoryList.SearchResult>(replay = 1)
        val roomDirectoryList = FakeRoomDirectoryList(directoryListStateFlow)
        val roomDirectoryService = FakeRoomDirectoryService { roomDirectoryList }
        val presenter = createRoomDirectoryPresenter(roomDirectoryService = roomDirectoryService)
        presenter.test {
            skipItems(1)
            directoryListStateFlow.emit(
                RoomDirectoryList.SearchResult(
                    hasMoreToLoad = true,
                    items = listOf(aRoomDescription())
                )
            )
            awaitItem().also { state ->
                assertThat(state.displayEmptyState).isFalse()
                assertThat(state.roomDescriptions).hasSize(1)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - emit search event`() = runTest {
        val filterLambda = lambdaRecorder { _: String?, _: Int, _: String? ->
            Result.success(Unit)
        }
        val roomDirectoryList = FakeRoomDirectoryList(filterLambda = filterLambda)
        val roomDirectoryService = FakeRoomDirectoryService { roomDirectoryList }
        val presenter = createRoomDirectoryPresenter(roomDirectoryService = roomDirectoryService)
        presenter.test {
            awaitItem().also { state ->
                state.eventSink(RoomDirectoryEvents.Search("test"))
            }
            awaitItem().also { state ->
                assertThat(state.query).isEqualTo("test")
            }
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }
        assert(filterLambda)
            .isCalledOnce()
            .with(value("test"), any(), value(null))
    }

    @Test
    fun `present - emit load more event`() = runTest {
        val loadMoreLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val roomDirectoryList = FakeRoomDirectoryList(loadMoreLambda = loadMoreLambda)
        val roomDirectoryService = FakeRoomDirectoryService { roomDirectoryList }
        val presenter = createRoomDirectoryPresenter(roomDirectoryService = roomDirectoryService)
        presenter.test {
            awaitItem().also { state ->
                state.eventSink(RoomDirectoryEvents.LoadMore)
            }
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }
        assert(loadMoreLambda)
            .isCalledOnce()
            .withNoParameter()
    }
}

internal fun TestScope.createRoomDirectoryPresenter(
    roomDirectoryService: RoomDirectoryService = FakeRoomDirectoryService(
        createRoomDirectoryListFactory = { FakeRoomDirectoryList() }
    ),
): RoomDirectoryPresenter {
    return RoomDirectoryPresenter(
        dispatchers = testCoroutineDispatchers(),
        roomDirectoryService = roomDirectoryService,
    )
}
