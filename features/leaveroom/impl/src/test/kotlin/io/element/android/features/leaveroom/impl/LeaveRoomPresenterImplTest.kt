/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.leaveroom.impl

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.leaveroom.api.LeaveRoomEvent
import io.element.android.features.leaveroom.api.LeaveRoomPresenter
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.aFakeMatrixClient
import io.element.android.libraries.matrix.test.room.aFakeMatrixRoom
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LeaveRoomPresenterImplTest {

    @Test
    fun `present - initial state hides all dialogs`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.confirmation).isEqualTo(LeaveRoomState.Confirmation.Hidden)
            assertThat(initialState.progress).isEqualTo(LeaveRoomState.Progress.Hidden)
            assertThat(initialState.error).isEqualTo(LeaveRoomState.Error.Hidden)
        }
    }

    @Test
    fun `present - show generic confirmation`() = runTest {
        val presenter = createPresenter(
            client = aFakeMatrixClient().apply {
                givenGetRoomResult(
                    roomId = A_ROOM_ID,
                    result = aFakeMatrixRoom()
                )
            }
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(LeaveRoomEvent.ShowConfirmation(A_ROOM_ID))
            val confirmationState = awaitItem()
            assertThat(confirmationState.confirmation).isEqualTo(LeaveRoomState.Confirmation.Generic(A_ROOM_ID))
        }
    }

    @Test
    fun `present - show private room confirmation`() = runTest {
        val presenter = createPresenter(
            client = aFakeMatrixClient().apply {
                givenGetRoomResult(
                    roomId = A_ROOM_ID,
                    result = aFakeMatrixRoom(isPublic = false),
                )
            }
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(LeaveRoomEvent.ShowConfirmation(A_ROOM_ID))
            val confirmationState = awaitItem()
            assertThat(confirmationState.confirmation).isEqualTo(LeaveRoomState.Confirmation.PrivateRoom(A_ROOM_ID))
        }
    }

    @Test
    fun `present - show last user in room confirmation`() = runTest {
        val presenter = createPresenter(
            client = aFakeMatrixClient().apply {
                givenGetRoomResult(
                    roomId = A_ROOM_ID,
                    result = aFakeMatrixRoom(joinedMemberCount = 1),
                )
            }
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(LeaveRoomEvent.ShowConfirmation(A_ROOM_ID))
            val confirmationState = awaitItem()
            assertThat(confirmationState.confirmation).isEqualTo(LeaveRoomState.Confirmation.LastUserInRoom(A_ROOM_ID))
        }
    }

    @Test
    fun `present - leaving a room leaves the room`() = runTest {
        val roomMembershipObserver = RoomMembershipObserver()
        val presenter = createPresenter(
            client = aFakeMatrixClient().apply {
                givenGetRoomResult(
                    roomId = A_ROOM_ID,
                    result = aFakeMatrixRoom(),
                )
            },
            roomMembershipObserver = roomMembershipObserver
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(LeaveRoomEvent.LeaveRoom(A_ROOM_ID))
            // Membership observer should receive a 'left room' change
            assertThat(roomMembershipObserver.updates.first().change).isEqualTo(MembershipChange.LEFT)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - show error if leave room fails`() = runTest {
        val presenter = createPresenter(
            client = aFakeMatrixClient().apply {
                givenGetRoomResult(
                    roomId = A_ROOM_ID,
                    result = aFakeMatrixRoom().apply {
                        givenLeaveRoomError(RuntimeException("Blimey!"))
                    },
                )
            }
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(LeaveRoomEvent.LeaveRoom(A_ROOM_ID))
            skipItems(1) // Skip show progress state
            val errorState = awaitItem()
            assertThat(errorState.error).isEqualTo(LeaveRoomState.Error.Shown)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - show progress indicator while leaving a room`() = runTest {
        val presenter = createPresenter(
            client = aFakeMatrixClient().apply {
                givenGetRoomResult(
                    roomId = A_ROOM_ID,
                    result = aFakeMatrixRoom(),
                )
            }
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(LeaveRoomEvent.LeaveRoom(A_ROOM_ID))
            val progressState = awaitItem()
            assertThat(progressState.progress).isEqualTo(LeaveRoomState.Progress.Shown)
            val finalState = awaitItem()
            assertThat(finalState.progress).isEqualTo(LeaveRoomState.Progress.Hidden)
        }
    }

    @Test
    fun `present - hide error hides the error`() = runTest {
        val presenter = createPresenter(
            client = aFakeMatrixClient().apply {
                givenGetRoomResult(
                    roomId = A_ROOM_ID,
                    result = aFakeMatrixRoom().apply {
                        givenLeaveRoomError(RuntimeException("Blimey!"))
                    },
                )
            }
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(LeaveRoomEvent.LeaveRoom(A_ROOM_ID))
            skipItems(1) // Skip show progress state
            val errorState = awaitItem()
            assertThat(errorState.error).isEqualTo(LeaveRoomState.Error.Shown)
            skipItems(1) // Skip hide progress state
            errorState.eventSink(LeaveRoomEvent.HideError)
            val hiddenErrorState = awaitItem()
            assertThat(hiddenErrorState.error).isEqualTo(LeaveRoomState.Error.Hidden)
        }
    }
}

private fun TestScope.createPresenter(
    client: MatrixClient = aFakeMatrixClient(),
    roomMembershipObserver: RoomMembershipObserver = RoomMembershipObserver(),
): LeaveRoomPresenter = LeaveRoomPresenterImpl(
    client = client,
    roomMembershipObserver = roomMembershipObserver,
    dispatchers = testCoroutineDispatchers(false),
)
