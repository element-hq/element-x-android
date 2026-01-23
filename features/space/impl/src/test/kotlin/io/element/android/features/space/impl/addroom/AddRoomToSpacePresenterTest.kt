/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.space.impl.addroom

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.libraries.matrix.test.spaces.FakeSpaceRoomList
import io.element.android.libraries.matrix.test.spaces.FakeSpaceService
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AddRoomToSpacePresenterTest {
    @Test
    fun `present - initial state has empty selection and no search`() = runTest {
        val presenter = createAddRoomToSpacePresenter()
        presenter.test {
            val state = awaitItem()
            assertThat(state.selectedRooms).isEmpty()
            assertThat(state.searchQuery).isEmpty()
            assertThat(state.isSearchActive).isFalse()
            assertThat(state.saveAction).isEqualTo(AsyncAction.Uninitialized)
            assertThat(state.canSave).isFalse()
        }
    }

    @Test
    fun `present - ToggleRoom adds room to selection`() = runTest {
        val presenter = createAddRoomToSpacePresenter()
        presenter.test {
            val state = awaitItem()
            val room = aSelectRoomInfoList().first()
            state.eventSink(AddRoomToSpaceEvent.ToggleRoom(room))
            val updatedState = awaitItem()
            assertThat(updatedState.selectedRooms).hasSize(1)
            assertThat(updatedState.selectedRooms.first().roomId).isEqualTo(room.roomId)
            assertThat(updatedState.canSave).isTrue()
        }
    }

    @Test
    fun `present - ToggleRoom removes already selected room`() = runTest {
        val presenter = createAddRoomToSpacePresenter()
        presenter.test {
            val state = awaitItem()
            val room = aSelectRoomInfoList().first()
            // Add room
            state.eventSink(AddRoomToSpaceEvent.ToggleRoom(room))
            val stateWithRoom = awaitItem()
            assertThat(stateWithRoom.selectedRooms).hasSize(1)
            // Remove room
            stateWithRoom.eventSink(AddRoomToSpaceEvent.ToggleRoom(room))
            val stateWithoutRoom = awaitItem()
            assertThat(stateWithoutRoom.selectedRooms).isEmpty()
            assertThat(stateWithoutRoom.canSave).isFalse()
        }
    }

    @Test
    fun `present - UpdateSearchQuery updates query`() = runTest {
        val presenter = createAddRoomToSpacePresenter()
        presenter.test {
            val state = awaitItem()
            state.eventSink(AddRoomToSpaceEvent.UpdateSearchQuery("test"))
            val updatedState = awaitItem()
            assertThat(updatedState.searchQuery).isEqualTo("test")
        }
    }

    @Test
    fun `present - OnSearchActiveChanged activates search`() = runTest {
        val presenter = createAddRoomToSpacePresenter()
        presenter.test {
            val state = awaitItem()
            state.eventSink(AddRoomToSpaceEvent.OnSearchActiveChanged(true))
            val updatedState = awaitItem()
            assertThat(updatedState.isSearchActive).isTrue()
        }
    }

    @Test
    fun `present - OnSearchActiveChanged deactivates search and clears query`() = runTest {
        val presenter = createAddRoomToSpacePresenter()
        presenter.test {
            val state = awaitItem()
            // Activate search and set query
            state.eventSink(AddRoomToSpaceEvent.OnSearchActiveChanged(true))
            awaitItem()
            state.eventSink(AddRoomToSpaceEvent.UpdateSearchQuery("test"))
            awaitItem()
            // Deactivate search
            state.eventSink(AddRoomToSpaceEvent.OnSearchActiveChanged(false))
            advanceUntilIdle()
            val finalState = expectMostRecentItem()
            assertThat(finalState.isSearchActive).isFalse()
            assertThat(finalState.searchQuery).isEmpty()
        }
    }

    @Test
    fun `present - CloseSearch deactivates and clears query`() = runTest {
        val presenter = createAddRoomToSpacePresenter()
        presenter.test {
            val state = awaitItem()
            // Activate search and set query
            state.eventSink(AddRoomToSpaceEvent.OnSearchActiveChanged(true))
            awaitItem()
            state.eventSink(AddRoomToSpaceEvent.UpdateSearchQuery("test"))
            awaitItem()
            // Close search
            state.eventSink(AddRoomToSpaceEvent.CloseSearch)
            advanceUntilIdle()
            val finalState = expectMostRecentItem()
            assertThat(finalState.isSearchActive).isFalse()
            assertThat(finalState.searchQuery).isEmpty()
        }
    }

    @Test
    fun `present - searchResults shows Results when rooms available`() = runTest {
        val roomListService = FakeRoomListService()
        val presenter = createAddRoomToSpacePresenter(roomListService = roomListService)
        presenter.test {
            awaitItem() // Initial state
            // Post rooms to the service
            roomListService.postAllRooms(
                listOf(
                    aRoomSummary(
                        roomId = A_ROOM_ID,
                        name = "Room 1",
                        isDirect = false,
                        isSpace = false,
                        currentUserMembership = CurrentUserMembership.JOINED,
                    )
                )
            )
            advanceUntilIdle()
            val state = expectMostRecentItem()
            assertThat(state.searchResults).isInstanceOf(SearchBarResultState.Results::class.java)
        }
    }

    @Test
    fun `present - searchResults shows NoResultsFound when search active with query but no results`() = runTest {
        val presenter = createAddRoomToSpacePresenter()
        presenter.test {
            val state = awaitItem()
            state.eventSink(AddRoomToSpaceEvent.OnSearchActiveChanged(true))
            awaitItem()
            state.eventSink(AddRoomToSpaceEvent.UpdateSearchQuery("nonexistent"))
            advanceUntilIdle()
            val finalState = expectMostRecentItem()
            assertThat(finalState.isSearchActive).isTrue()
            assertThat(finalState.searchQuery).isEqualTo("nonexistent")
            assertThat(finalState.searchResults).isInstanceOf(SearchBarResultState.NoResultsFound::class.java)
        }
    }

    @Test
    fun `present - Save triggers addChildToSpace for all selected rooms`() = runTest {
        val addChildToSpaceResult = lambdaRecorder<RoomId, RoomId, Result<Unit>> { _, _ ->
            Result.success(Unit)
        }
        val spaceService = FakeSpaceService(
            addChildToSpaceResult = addChildToSpaceResult,
        )
        val presenter = createAddRoomToSpacePresenter(spaceService = spaceService)
        presenter.test {
            val state = awaitItem()
            // Select two rooms
            val room1 = aSelectRoomInfoList()[0]
            val room2 = aSelectRoomInfoList()[1]
            state.eventSink(AddRoomToSpaceEvent.ToggleRoom(room1))
            awaitItem()
            state.eventSink(AddRoomToSpaceEvent.ToggleRoom(room2))
            awaitItem()
            // Save
            state.eventSink(AddRoomToSpaceEvent.Save)
            // Wait for loading and success states
            skipItems(1) // Loading
            advanceUntilIdle()
            skipItems(1) // Success
            // Verify service was called for both rooms
            addChildToSpaceResult.assertions().isCalledExactly(2)
        }
    }

    @Test
    fun `present - Save success updates saveAction to Success`() = runTest {
        val spaceService = FakeSpaceService(
            addChildToSpaceResult = { _, _ -> Result.success(Unit) },
        )
        val presenter = createAddRoomToSpacePresenter(spaceService = spaceService)
        presenter.test {
            val state = awaitItem()
            val room = aSelectRoomInfoList().first()
            state.eventSink(AddRoomToSpaceEvent.ToggleRoom(room))
            awaitItem()
            state.eventSink(AddRoomToSpaceEvent.Save)
            // Wait for loading state
            val loadingState = awaitItem()
            assertThat(loadingState.saveAction).isEqualTo(AsyncAction.Loading)
            // Wait for success state
            advanceUntilIdle()
            val successState = awaitItem()
            assertThat(successState.saveAction).isInstanceOf(AsyncAction.Success::class.java)
        }
    }

    @Test
    fun `present - Save failure updates saveAction to Failure`() = runTest {
        val spaceService = FakeSpaceService(
            addChildToSpaceResult = { _, _ -> Result.failure(AN_EXCEPTION) },
        )
        val presenter = createAddRoomToSpacePresenter(spaceService = spaceService)
        presenter.test {
            val state = awaitItem()
            val room = aSelectRoomInfoList().first()
            state.eventSink(AddRoomToSpaceEvent.ToggleRoom(room))
            awaitItem()
            state.eventSink(AddRoomToSpaceEvent.Save)
            // Wait for loading state
            val loadingState = awaitItem()
            assertThat(loadingState.saveAction).isEqualTo(AsyncAction.Loading)
            // Wait for failure state
            advanceUntilIdle()
            val failureState = awaitItem()
            assertThat(failureState.saveAction).isInstanceOf(AsyncAction.Failure::class.java)
        }
    }

    @Test
    fun `present - ResetSaveAction resets to Uninitialized`() = runTest {
        val spaceService = FakeSpaceService(
            addChildToSpaceResult = { _, _ -> Result.success(Unit) },
        )
        val presenter = createAddRoomToSpacePresenter(spaceService = spaceService)
        presenter.test {
            val state = awaitItem()
            val room = aSelectRoomInfoList().first()
            state.eventSink(AddRoomToSpaceEvent.ToggleRoom(room))
            awaitItem()
            state.eventSink(AddRoomToSpaceEvent.Save)
            skipItems(1) // Loading
            advanceUntilIdle()
            val successState = awaitItem()
            assertThat(successState.saveAction).isInstanceOf(AsyncAction.Success::class.java)
            // Reset
            successState.eventSink(AddRoomToSpaceEvent.ResetSaveAction)
            val resetState = awaitItem()
            assertThat(resetState.saveAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `canSave is false when no rooms selected`() = runTest {
        val presenter = createAddRoomToSpacePresenter()
        presenter.test {
            val state = awaitItem()
            assertThat(state.selectedRooms).isEmpty()
            assertThat(state.canSave).isFalse()
        }
    }

    @Test
    fun `canSave is false when loading`() = runTest {
        val spaceService = FakeSpaceService(
            addChildToSpaceResult = { _, _ -> Result.success(Unit) },
        )
        val presenter = createAddRoomToSpacePresenter(spaceService = spaceService)
        presenter.test {
            val state = awaitItem()
            val room = aSelectRoomInfoList().first()
            state.eventSink(AddRoomToSpaceEvent.ToggleRoom(room))
            val stateWithRoom = awaitItem()
            assertThat(stateWithRoom.canSave).isTrue()
            stateWithRoom.eventSink(AddRoomToSpaceEvent.Save)
            val loadingState = awaitItem()
            assertThat(loadingState.saveAction).isEqualTo(AsyncAction.Loading)
            assertThat(loadingState.canSave).isFalse()
        }
    }

    private fun TestScope.createAddRoomToSpacePresenter(
        spaceRoomList: FakeSpaceRoomList = FakeSpaceRoomList(
            paginateResult = { Result.success(Unit) },
        ),
        spaceService: FakeSpaceService = FakeSpaceService(
            addChildToSpaceResult = { _, _ -> Result.success(Unit) },
        ),
        roomListService: FakeRoomListService = FakeRoomListService(),
        matrixClient: FakeMatrixClient = FakeMatrixClient(
            roomListService = roomListService,
        ),
    ): AddRoomToSpacePresenter {
        val dataSourceFactory = object : AddRoomToSpaceSearchDataSource.Factory {
            override fun create(coroutineScope: CoroutineScope) = AddRoomToSpaceSearchDataSource(
                coroutineScope = coroutineScope,
                roomListService = roomListService,
                spaceRoomList = spaceRoomList,
                matrixClient = matrixClient,
                coroutineDispatchers = testCoroutineDispatchers(),
            )
        }
        return AddRoomToSpacePresenter(
            spaceRoomList = spaceRoomList,
            spaceService = spaceService,
            dataSourceFactory = dataSourceFactory,
        )
    }
}
