/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomcall.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.api.CurrentCall
import io.element.android.features.call.api.CurrentCallService
import io.element.android.features.call.test.FakeCurrentCallService
import io.element.android.features.roomcall.api.RoomCallState
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.tests.testutils.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomCallStatePresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val room = FakeMatrixRoom(
            canUserJoinCallResult = { Result.success(false) },
        )
        val presenter = createRoomCallStatePresenter(matrixRoom = room)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(
                RoomCallState.StandBy(
                    canStartCall = false,
                )
            )
        }
    }

    @Test
    fun `present - initial state - user can join call`() = runTest {
        val room = FakeMatrixRoom(
            canUserJoinCallResult = { Result.success(true) },
        )
        val presenter = createRoomCallStatePresenter(matrixRoom = room)
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(
                RoomCallState.StandBy(
                    canStartCall = true,
                )
            )
        }
    }

    @Test
    fun `present - call is disabled if user cannot join it even if there is an ongoing call`() = runTest {
        val room = FakeMatrixRoom(
            canUserJoinCallResult = { Result.success(false) },
        ).apply {
            givenRoomInfo(aRoomInfo(hasRoomCall = true))
        }
        val presenter = createRoomCallStatePresenter(matrixRoom = room)
        presenter.test {
            skipItems(1)
            assertThat(awaitItem()).isEqualTo(
                RoomCallState.OnGoing(
                    canJoinCall = false,
                    isUserInTheCall = false,
                    isUserLocallyInTheCall = false,
                )
            )
        }
    }

    @Test
    fun `present - user has joined the call on another session`() = runTest {
        val room = FakeMatrixRoom(
            canUserJoinCallResult = { Result.success(true) },
        ).apply {
            givenRoomInfo(
                aRoomInfo(
                    hasRoomCall = true,
                    activeRoomCallParticipants = listOf(sessionId),
                )
            )
        }
        val presenter = createRoomCallStatePresenter(matrixRoom = room)
        presenter.test {
            skipItems(1)
            assertThat(awaitItem()).isEqualTo(
                RoomCallState.OnGoing(
                    canJoinCall = true,
                    isUserInTheCall = true,
                    isUserLocallyInTheCall = false,
                )
            )
        }
    }

    @Test
    fun `present - user has joined the call locally`() = runTest {
        val room = FakeMatrixRoom(
            canUserJoinCallResult = { Result.success(true) },
        ).apply {
            givenRoomInfo(
                aRoomInfo(
                    hasRoomCall = true,
                    activeRoomCallParticipants = listOf(sessionId),
                )
            )
        }
        val presenter = createRoomCallStatePresenter(
            matrixRoom = room,
            currentCallService = FakeCurrentCallService(MutableStateFlow(CurrentCall.RoomCall(room.roomId))),
        )
        presenter.test {
            skipItems(1)
            assertThat(awaitItem()).isEqualTo(
                RoomCallState.OnGoing(
                    canJoinCall = true,
                    isUserInTheCall = true,
                    isUserLocallyInTheCall = true,
                )
            )
        }
    }

    @Test
    fun `present - user leaves the call`() = runTest {
        val room = FakeMatrixRoom(
            canUserJoinCallResult = { Result.success(true) },
        ).apply {
            givenRoomInfo(
                aRoomInfo(
                    hasRoomCall = true,
                    activeRoomCallParticipants = listOf(sessionId),
                )
            )
        }
        val currentCall = MutableStateFlow<CurrentCall>(CurrentCall.RoomCall(room.roomId))
        val currentCallService = FakeCurrentCallService(currentCall = currentCall)
        val presenter = createRoomCallStatePresenter(
            matrixRoom = room,
            currentCallService = currentCallService
        )
        presenter.test {
            skipItems(1)
            assertThat(awaitItem()).isEqualTo(
                RoomCallState.OnGoing(
                    canJoinCall = true,
                    isUserInTheCall = true,
                    isUserLocallyInTheCall = true,
                )
            )
            currentCall.value = CurrentCall.None
            assertThat(awaitItem()).isEqualTo(
                RoomCallState.OnGoing(
                    canJoinCall = true,
                    isUserInTheCall = true,
                    isUserLocallyInTheCall = false,
                )
            )
            room.givenRoomInfo(
                aRoomInfo(
                    hasRoomCall = true,
                    activeRoomCallParticipants = emptyList(),
                )
            )
            assertThat(awaitItem()).isEqualTo(
                RoomCallState.OnGoing(
                    canJoinCall = true,
                    isUserInTheCall = false,
                    isUserLocallyInTheCall = false,
                )
            )
            room.givenRoomInfo(
                aRoomInfo(
                    hasRoomCall = false,
                    activeRoomCallParticipants = emptyList(),
                )
            )
            assertThat(awaitItem()).isEqualTo(
                RoomCallState.StandBy(
                    canStartCall = true,
                )
            )
        }
    }

    private fun createRoomCallStatePresenter(
        matrixRoom: MatrixRoom,
        currentCallService: CurrentCallService = FakeCurrentCallService(),
    ): RoomCallStatePresenter {
        return RoomCallStatePresenter(
            room = matrixRoom,
            currentCallService = currentCallService,
        )
    }
}
