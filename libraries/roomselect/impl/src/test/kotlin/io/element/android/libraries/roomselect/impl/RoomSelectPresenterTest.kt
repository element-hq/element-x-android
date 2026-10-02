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
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.matrix.test.roomlist.FakeDynamicRoomList
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.libraries.matrix.ui.model.toSelectRoomInfo
import io.element.android.libraries.roomselect.api.RoomSelectEntryPoint
import io.element.android.libraries.roomselect.api.RoomSelectMode
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.awaitLastSequentialItem
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
            assertThat(initialState.selectedAccount).isEqualTo(MatrixUser(A_SESSION_ID))
            assertThat(initialState.otherAccounts).isEmpty()
            assertThat(initialState.isAccountListExpanded).isFalse()
            assertThat(initialState.showAccountSwitch).isFalse()
        }
    }

    @Test
    fun `present - share mode with a single account does not show the account switch`() = runTest {
        val presenter = createRoomSelectPresenter(
            mode = RoomSelectMode.Share,
            sessionStore = InMemorySessionStore(
                initialList = listOf(aSessionData(sessionId = A_SESSION_ID.value)),
            ),
        )
        presenter.test {
            val state = awaitLastSequentialItem()
            assertThat(state.selectedAccount.userId).isEqualTo(A_SESSION_ID)
            assertThat(state.otherAccounts).isEmpty()
            assertThat(state.showAccountSwitch).isFalse()
        }
    }

    @Test
    fun `present - share mode with several accounts shows the account switch`() = runTest {
        val presenter = createRoomSelectPresenter(
            mode = RoomSelectMode.Share,
            sessionStore = aSessionStoreWithTwoAccounts(),
        )
        presenter.test {
            val state = awaitLastSequentialItem()
            assertThat(state.selectedAccount.userId).isEqualTo(A_SESSION_ID)
            assertThat(state.selectedAccount.displayName).isEqualTo("Alice")
            assertThat(state.otherAccounts.map { it.userId }).containsExactly(A_SESSION_ID_2)
            assertThat(state.showAccountSwitch).isTrue()
            state.eventSink(RoomSelectEvent.ToggleAccountListExpanded)
            assertThat(awaitItem().isAccountListExpanded).isTrue()
            state.eventSink(RoomSelectEvent.ToggleAccountListExpanded)
            assertThat(awaitItem().isAccountListExpanded).isFalse()
        }
    }

    @Test
    fun `present - select another account displays the rooms of this account`() = runTest {
        val roomSummary1 = aRoomSummary(roomId = A_ROOM_ID)
        val roomSummary2 = aRoomSummary(roomId = A_ROOM_ID_2)
        val roomListServices = mapOf(
            A_SESSION_ID to FakeRoomListService(createRoomListLambda = { FakeDynamicRoomList(summaries = MutableStateFlow(listOf(roomSummary1))) }),
            A_SESSION_ID_2 to FakeRoomListService(createRoomListLambda = { FakeDynamicRoomList(summaries = MutableStateFlow(listOf(roomSummary2))) }),
        )
        val presenter = createRoomSelectPresenter(
            mode = RoomSelectMode.Share,
            sessionStore = aSessionStoreWithTwoAccounts(),
            matrixClientProvider = FakeMatrixClientProvider { sessionId ->
                Result.success(FakeMatrixClient(sessionId = sessionId, roomListService = roomListServices.getValue(sessionId)))
            },
        )
        presenter.test {
            val state = awaitLastSequentialItem()
            assertThat(state.selectedAccount.userId).isEqualTo(A_SESSION_ID)
            assertThat((state.resultState as SearchBarResultState.Results).results.map { it.roomId }).containsExactly(A_ROOM_ID)
            state.eventSink(RoomSelectEvent.ToggleSelectedRoom(roomSummary1.toSelectRoomInfo()))
            state.eventSink(RoomSelectEvent.ToggleAccountListExpanded)
            state.eventSink(RoomSelectEvent.SelectAccount(A_SESSION_ID_2))
            val finalState = awaitLastSequentialItem()
            assertThat(finalState.selectedAccount.userId).isEqualTo(A_SESSION_ID_2)
            assertThat(finalState.otherAccounts.map { it.userId }).containsExactly(A_SESSION_ID)
            assertThat(finalState.selectedRooms).isEmpty()
            assertThat(finalState.isAccountListExpanded).isFalse()
            assertThat((finalState.resultState as SearchBarResultState.Results).results.map { it.roomId }).containsExactly(A_ROOM_ID_2)
        }
    }

    @Test
    fun `present - forward mode with several accounts does not show the account switch`() = runTest {
        val presenter = createRoomSelectPresenter(
            mode = RoomSelectMode.Forward,
            sessionStore = aSessionStoreWithTwoAccounts(),
        )
        presenter.test {
            val state = awaitLastSequentialItem()
            assertThat(state.otherAccounts).isNotEmpty()
            assertThat(state.showAccountSwitch).isFalse()
        }
    }

    private fun aSessionStoreWithTwoAccounts() = InMemorySessionStore(
        initialList = listOf(
            aSessionData(sessionId = A_SESSION_ID.value, userDisplayName = "Alice"),
            aSessionData(sessionId = A_SESSION_ID_2.value, userDisplayName = "Bob"),
        ),
    )

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
    sessionStore: SessionStore = InMemorySessionStore(),
    matrixClientProvider: MatrixClientProvider = FakeMatrixClientProvider { sessionId ->
        Result.success(FakeMatrixClient(sessionId = sessionId, roomListService = roomListService))
    },
) = RoomSelectPresenter(
    initialSessionId = A_SESSION_ID,
    mode = mode,
    maxNumberOfRooms = maxNumberOfRooms,
    dataSourceFactory = object : RoomSelectSearchDataSource.Factory {
        override fun create(coroutineScope: CoroutineScope, roomListService: RoomListService): RoomSelectSearchDataSource {
            return RoomSelectSearchDataSource(
                coroutineScope = coroutineScope,
                roomListService = roomListService,
                coroutineDispatchers = testCoroutineDispatchers(),
            )
        }
    },
    matrixClientProvider = matrixClientProvider,
    sessionStore = sessionStore,
)
