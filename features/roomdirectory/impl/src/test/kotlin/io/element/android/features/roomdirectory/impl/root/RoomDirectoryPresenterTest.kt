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

package io.element.android.features.roomdirectory.impl.root

import com.google.common.truth.Truth.assertThat
import io.element.android.features.roomdirectory.impl.root.di.JoinRoom
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryList
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryService
import io.element.android.libraries.matrix.test.A_ROOM_ID
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

@OptIn(ExperimentalCoroutinesApi::class) class RoomDirectoryPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createRoomDirectoryPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.query).isEmpty()
            assertThat(initialState.displayEmptyState).isFalse()
            assertThat(initialState.joinRoomAction).isEqualTo(AsyncAction.Uninitialized)
            assertThat(initialState.roomDescriptions).isEmpty()
            assertThat(initialState.displayLoadMoreIndicator).isTrue()
        }
    }

    @Test
    fun `present - room directory list emits empty state`() = runTest {
        val directoryListStateFlow = MutableSharedFlow<RoomDirectoryList.State>(replay = 1)
        val roomDirectoryList = FakeRoomDirectoryList(directoryListStateFlow)
        val roomDirectoryService = FakeRoomDirectoryService { roomDirectoryList }
        val presenter = createRoomDirectoryPresenter(roomDirectoryService = roomDirectoryService)
        presenter.test {
            skipItems(1)
            directoryListStateFlow.emit(
                RoomDirectoryList.State(false, emptyList())
            )
            awaitItem().also { state ->
                assertThat(state.displayEmptyState).isTrue()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - room directory list emits non-empty state`() = runTest {
        val directoryListStateFlow = MutableSharedFlow<RoomDirectoryList.State>(replay = 1)
        val roomDirectoryList = FakeRoomDirectoryList(directoryListStateFlow)
        val roomDirectoryService = FakeRoomDirectoryService { roomDirectoryList }
        val presenter = createRoomDirectoryPresenter(roomDirectoryService = roomDirectoryService)
        presenter.test {
            skipItems(1)
            directoryListStateFlow.emit(
                RoomDirectoryList.State(
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
        val filterLambda = lambdaRecorder { _: String?, _: Int ->
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
            .with(value("test"), any())
    }

    @Test
    fun `present - emit load more event`() = runTest {
        val loadMoreLambda = lambdaRecorder { ->
            Result.success(Unit)
        }
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

    @Test
    fun `present - emit join room event`() = runTest {
        val joinRoomSuccess = lambdaRecorder { roomId: RoomId ->
            Result.success(roomId)
        }
        val joinRoomFailure = lambdaRecorder { roomId: RoomId ->
            Result.failure<RoomId>(RuntimeException("Failed to join room $roomId"))
        }
        val fakeJoinRoom = FakeJoinRoom(joinRoomSuccess)
        val presenter = createRoomDirectoryPresenter(joinRoom = fakeJoinRoom)
        presenter.test {
            awaitItem().also { state ->
                state.eventSink(RoomDirectoryEvents.JoinRoom(A_ROOM_ID))
            }
            awaitItem().also { state ->
                assertThat(state.joinRoomAction).isEqualTo(AsyncAction.Success(A_ROOM_ID))
                fakeJoinRoom.lambda = joinRoomFailure
                state.eventSink(RoomDirectoryEvents.JoinRoom(A_ROOM_ID))
            }
            awaitItem().also { state ->
                assertThat(state.joinRoomAction).isInstanceOf(AsyncAction.Failure::class.java)
            }
        }
        assert(joinRoomSuccess)
            .isCalledOnce()
            .with(value(A_ROOM_ID))
        assert(joinRoomFailure)
            .isCalledOnce()
            .with(value(A_ROOM_ID))
    }

    private fun TestScope.createRoomDirectoryPresenter(
        roomDirectoryService: RoomDirectoryService = FakeRoomDirectoryService(
            createRoomDirectoryListFactory = { FakeRoomDirectoryList() }
        ),
        joinRoom: JoinRoom = FakeJoinRoom { Result.success(it) },
    ): RoomDirectoryPresenter {
        return RoomDirectoryPresenter(
            dispatchers = testCoroutineDispatchers(),
            joinRoom = joinRoom,
            roomDirectoryService = roomDirectoryService,
        )
    }
}
