/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.roomselect.impl

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.matrix.test.roomlist.FakeDynamicRoomList
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.libraries.matrix.ui.model.toSelectRoomInfo
import io.element.android.libraries.roomselect.api.RoomSelectEntryPoint
import io.element.android.libraries.roomselect.api.RoomSelectMode
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class RoomSelectPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createRoomSelectPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.selectedRooms).isEmpty()
            assertThat(initialState.resultState).isInstanceOf(SearchBarResultState.Initial::class.java)
            assertThat(initialState.isSearchActive).isFalse()
            assertThat(initialState.maxNumberOfRooms).isEqualTo(10)
            assertThat(initialState.canSelectMoreRooms).isTrue()
        }
    }

    @Test
    fun `present - toggle search active`() = runTest {
        val presenter = createRoomSelectPresenter()
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomSelectEvent.ToggleSearchActive)
            assertThat(awaitItem().isSearchActive).isTrue()
            initialState.eventSink(RoomSelectEvent.ToggleSearchActive)
            assertThat(awaitItem().isSearchActive).isFalse()
        }
    }

    @Test
    fun `present - update query`() = runTest {
        val roomSummary = aRoomSummary()
        val roomList = FakeDynamicRoomList(
            summaries = MutableStateFlow(listOf(roomSummary))
        )
        val roomListService = FakeRoomListService(
            createRoomListLambda = { roomList }
        )
        val presenter = createRoomSelectPresenter(
            roomListService = roomListService
        )
        presenter.test {
            val initialState = awaitItem()
            val expectedRoomInfo = roomSummary.toSelectRoomInfo()
            // Do not compare the lambda because they will be different. So copy the lambda from expectedRoomSummary to result
            val result = (awaitItem().resultState as SearchBarResultState.Results).results
            assertThat(result).isEqualTo(listOf(expectedRoomInfo))
            initialState.eventSink(RoomSelectEvent.ToggleSearchActive)
            skipItems(1)
            initialState.searchQuery.setTextAndPlaceCursorAtEnd("string not contained")
            assertThat(
                roomList.currentFilter.value
            ).isEqualTo(
                RoomListFilter.NormalizedMatchRoomName("string not contained")
            )
            assertThat(awaitItem().searchQuery.text.toString()).isEqualTo("string not contained")
            roomList.summaries.emit(
                emptyList()
            )
            assertThat(awaitItem().resultState).isInstanceOf(SearchBarResultState.NoResultsFound::class.java)
        }
    }

    @Test
    fun `present - select and remove a room`() = runTest {
        val roomSummary = aRoomSummary()
        val roomList = FakeDynamicRoomList(
            summaries = MutableStateFlow(listOf(roomSummary))
        )
        val roomListService = FakeRoomListService(
            createRoomListLambda = { roomList }
        )
        val presenter = createRoomSelectPresenter(
            maxNumberOfRooms = 1,
            roomListService = roomListService,
        )
        presenter.test {
            val initialState = awaitItem()
            val roomInfo = roomSummary.toSelectRoomInfo()
            initialState.eventSink(RoomSelectEvent.ToggleSelectedRoom(roomInfo))
            awaitItem().let {
                assertThat(it.selectedRooms).isEqualTo(persistentListOf(roomInfo))
                assertThat(it.canSelectMoreRooms).isFalse()
                it.eventSink(RoomSelectEvent.ToggleSelectedRoom(roomInfo))
            }
            awaitItem().let {
                assertThat(it.selectedRooms).isEmpty()
                assertThat(it.canSelectMoreRooms).isTrue()
            }
            cancel()
        }
    }

    @Test
    fun `present - UpdateVisibleRange triggers pagination when near end`() = runTest {
        val loadMoreLambda = lambdaRecorder<Unit> { }
        val roomList = FakeDynamicRoomList(
            summaries = MutableStateFlow(listOf()),
            loadMoreLambda = loadMoreLambda,
        )
        val roomListService = FakeRoomListService(
            createRoomListLambda = { roomList }
        )
        val presenter = createRoomSelectPresenter(roomListService = roomListService)
        presenter.test {
            val initialState = awaitItem()
            // Post some rooms to simulate loaded content
            val rooms = (1..10).map { aRoomSummary() }
            roomList.summaries.emit(rooms)
            skipItems(1)

            // UpdateVisibleRange near end should trigger loadMore
            initialState.eventSink(RoomSelectEvent.UpdateVisibleRange(IntRange(0, 9)))
            // Give time for the coroutine to complete
            testScheduler.advanceUntilIdle()

            assert(loadMoreLambda).isCalledOnce()
        }
    }
}

internal fun TestScope.createRoomSelectPresenter(
    mode: RoomSelectMode = RoomSelectMode.Forward,
    maxNumberOfRooms: Int = RoomSelectEntryPoint.DEFAULT_MAX_NUMBER_OF_ROOMS,
    roomListService: RoomListService = FakeRoomListService(),
) = RoomSelectPresenter(
    mode = mode,
    maxNumberOfRooms = maxNumberOfRooms,
    dataSourceFactory = object : RoomSelectSearchDataSource.Factory {
        override fun create(coroutineScope: CoroutineScope): RoomSelectSearchDataSource {
            return RoomSelectSearchDataSource(
                coroutineScope = coroutineScope,
                roomListService = roomListService,
                coroutineDispatchers = testCoroutineDispatchers(),
            )
        }
    }
)
