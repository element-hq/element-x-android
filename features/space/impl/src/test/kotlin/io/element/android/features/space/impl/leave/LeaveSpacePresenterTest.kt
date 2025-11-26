/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.leave

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.LeaveSpaceHandle
import io.element.android.libraries.matrix.api.spaces.LeaveSpaceRoom
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID_3
import io.element.android.libraries.matrix.test.A_SPACE_ID
import io.element.android.libraries.matrix.test.A_SPACE_NAME
import io.element.android.libraries.matrix.test.spaces.FakeLeaveSpaceHandle
import io.element.android.libraries.previewutils.room.aSpaceRoom
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LeaveSpacePresenterTest {
    private val aSpace = aSpaceRoom(
        roomId = A_SPACE_ID,
        displayName = A_SPACE_NAME,
    )

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createLeaveSpacePresenter(
            leaveSpaceHandle = FakeLeaveSpaceHandle(
                roomsResult = { Result.success(emptyList()) },
            ),
        )
        presenter.test {
            val state = awaitItem()
            assertThat(state.spaceName).isNull()
            assertThat(state.isLastAdmin).isFalse()
            assertThat(state.selectableSpaceRooms.isLoading()).isTrue()
            assertThat(state.leaveSpaceAction).isEqualTo(AsyncAction.Uninitialized)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - fail to load rooms`() = runTest {
        val presenter = createLeaveSpacePresenter(
            leaveSpaceHandle = FakeLeaveSpaceHandle(
                roomsResult = { Result.failure(AN_EXCEPTION) },
            )
        )
        presenter.test {
            val state = awaitItem()
            assertThat(state.selectableSpaceRooms.isLoading()).isTrue()
            assertThat(state.leaveSpaceAction).isEqualTo(AsyncAction.Uninitialized)
            skipItems(2)
            val stateError = awaitItem()
            assertThat(stateError.selectableSpaceRooms.isFailure()).isTrue()
            // Retry
            stateError.eventSink(LeaveSpaceEvents.Retry)
            skipItems(1)
            val stateLoadingAgain = awaitItem()
            assertThat(stateLoadingAgain.selectableSpaceRooms.isLoading()).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - current space name and is last admin`() = runTest {
        val presenter = createLeaveSpacePresenter(
            leaveSpaceHandle = FakeLeaveSpaceHandle(
                roomsResult = { Result.success(listOf(aLeaveSpaceRoom(spaceRoom = aSpace, isLastAdmin = true))) },
            )
        )
        presenter.test {
            val state = awaitItem()
            assertThat(state.spaceName).isNull()
            skipItems(2)
            val finalState = awaitItem()
            assertThat(finalState.spaceName).isEqualTo(A_SPACE_NAME)
            assertThat(finalState.isLastAdmin).isTrue()
            // The current state is not in the sub room list
            assertThat(finalState.selectableSpaceRooms.dataOrNull()!!).isEmpty()
        }
    }

    @Test
    fun `present - direct rooms are filtered out`() = runTest {
        val leaveResult = lambdaRecorder<List<RoomId>, Result<Unit>> { Result.success(Unit) }
        val presenter = createLeaveSpacePresenter(
            leaveSpaceHandle = FakeLeaveSpaceHandle(
                roomsResult = {
                    Result.success(
                        listOf(
                            aLeaveSpaceRoom(spaceRoom = aSpace),
                            aLeaveSpaceRoom(
                                spaceRoom = aSpaceRoom(roomId = A_ROOM_ID, isDirect = false)
                            ),
                            aLeaveSpaceRoom(
                                spaceRoom = aSpaceRoom(roomId = A_ROOM_ID_2, isDirect = true)
                            ),
                            aLeaveSpaceRoom(
                                spaceRoom = aSpaceRoom(roomId = A_ROOM_ID_3, isDirect = null)
                            ),
                        )
                    )
                },
                leaveResult = leaveResult,
            )
        )
        presenter.test {
            val state = awaitItem()
            assertThat(state.spaceName).isNull()
            skipItems(2)
            val finalState = awaitItem()
            // The current state is not in the sub room list
            assertThat(finalState.selectableSpaceRooms.dataOrNull()!!.map { it.spaceRoom.roomId }).containsExactly(A_ROOM_ID, A_ROOM_ID_3)
            assertThat(finalState.selectedRoomsCount).isEqualTo(2)
            // Leaving the space will not include the DM
            finalState.eventSink(LeaveSpaceEvents.LeaveSpace)
            val stateLeaving = awaitItem()
            assertThat(stateLeaving.leaveSpaceAction).isEqualTo(AsyncAction.Loading)
            val stateLeft = awaitItem()
            assertThat(stateLeft.leaveSpaceAction.isSuccess()).isTrue()
            leaveResult.assertions().isCalledOnce().with(
                value(listOf(A_ROOM_ID, A_ROOM_ID_3))
            )
        }
    }

    @Test
    fun `present - leave space and sub rooms`() = runTest {
        val leaveResult = lambdaRecorder<List<RoomId>, Result<Unit>> { Result.success(Unit) }
        val presenter = createLeaveSpacePresenter(
            leaveSpaceHandle = FakeLeaveSpaceHandle(
                roomsResult = {
                    Result.success(
                        listOf(
                            LeaveSpaceRoom(aSpaceRoom(roomId = A_ROOM_ID), isLastAdmin = false),
                            LeaveSpaceRoom(aSpaceRoom(roomId = A_ROOM_ID_2), isLastAdmin = true),
                        )
                    )
                },
                leaveResult = leaveResult,
            )
        )
        presenter.test {
            skipItems(3)
            val state = awaitItem()
            assertThat(state.spaceName).isNull()
            assertThat(state.isLastAdmin).isFalse()
            val data = state.selectableSpaceRooms.dataOrNull()!!
            assertThat(data.size).isEqualTo(2)
            // Only one room is selectable as the user is the last admin in the other one
            val room1 = data[0]
            assertThat(room1.spaceRoom.roomId).isEqualTo(A_ROOM_ID)
            assertThat(room1.isSelected).isTrue()
            assertThat(room1.isLastAdmin).isFalse()
            val room2 = data[1]
            assertThat(room2.spaceRoom.roomId).isEqualTo(A_ROOM_ID_2)
            assertThat(room2.isSelected).isFalse()
            assertThat(room2.isLastAdmin).isTrue()
            // Deselect all
            state.eventSink(LeaveSpaceEvents.DeselectAllRooms)
            skipItems(1)
            val stateAllDeselected = awaitItem()
            val dataAllDeselected = stateAllDeselected.selectableSpaceRooms.dataOrNull()!!
            assertThat(dataAllDeselected.any { it.isSelected }).isFalse()
            // Select all
            stateAllDeselected.eventSink(LeaveSpaceEvents.SelectAllRooms)
            skipItems(1)
            val stateAllSelected = awaitItem()
            val dataAllSelected = stateAllSelected.selectableSpaceRooms.dataOrNull()!!
            // The last admin room should not be selected
            assertThat(dataAllSelected.count { it.isSelected }).isEqualTo(1)
            // Toggle selection of the first room
            stateAllSelected.eventSink(LeaveSpaceEvents.ToggleRoomSelection(A_ROOM_ID))
            skipItems(1)
            val stateOneDeselected = awaitItem()
            val dataOneDeselected = stateOneDeselected.selectableSpaceRooms.dataOrNull()!!
            assertThat(dataOneDeselected[0].isSelected).isFalse()
            // Toggle selection of the first room
            stateOneDeselected.eventSink(LeaveSpaceEvents.ToggleRoomSelection(A_ROOM_ID))
            skipItems(1)
            val stateOneSelected = awaitItem()
            val dataOneSelected = stateOneSelected.selectableSpaceRooms.dataOrNull()!!
            assertThat(dataOneSelected[0].isSelected).isTrue()
            // Leave space
            stateOneSelected.eventSink(LeaveSpaceEvents.LeaveSpace)
            val stateLeaving = awaitItem()
            assertThat(stateLeaving.leaveSpaceAction).isEqualTo(AsyncAction.Loading)
            val stateLeft = awaitItem()
            assertThat(stateLeft.leaveSpaceAction.isSuccess()).isTrue()
            leaveResult.assertions().isCalledOnce().with(
                value(listOf(A_ROOM_ID))
            )
        }
    }

    @Test
    fun `present - leave space error and close`() = runTest {
        val leaveResult = lambdaRecorder<List<RoomId>, Result<Unit>> {
            Result.failure(AN_EXCEPTION)
        }
        val presenter = createLeaveSpacePresenter(
            leaveSpaceHandle = FakeLeaveSpaceHandle(
                roomsResult = { Result.success(emptyList()) },
                leaveResult = leaveResult,
            )
        )
        presenter.test {
            skipItems(3)
            val state = awaitItem()
            state.eventSink(LeaveSpaceEvents.LeaveSpace)
            val stateLeaving = awaitItem()
            assertThat(stateLeaving.leaveSpaceAction).isEqualTo(AsyncAction.Loading)
            val stateError = awaitItem()
            assertThat(stateError.leaveSpaceAction.isFailure()).isTrue()
            // Close error
            stateError.eventSink(LeaveSpaceEvents.CloseError)
            val stateErrorClosed = awaitItem()
            assertThat(stateErrorClosed.leaveSpaceAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    private fun createLeaveSpacePresenter(
        leaveSpaceHandle: LeaveSpaceHandle = FakeLeaveSpaceHandle(),
    ): LeaveSpacePresenter {
        return LeaveSpacePresenter(
            leaveSpaceHandle = leaveSpaceHandle,
        )
    }
}

private fun aLeaveSpaceRoom(
    spaceRoom: SpaceRoom = aSpaceRoom(
        roomId = A_SPACE_ID,
        displayName = A_SPACE_NAME,
    ),
    isLastAdmin: Boolean = false,
) = LeaveSpaceRoom(
    spaceRoom = spaceRoom,
    isLastAdmin = isLastAdmin,
)
