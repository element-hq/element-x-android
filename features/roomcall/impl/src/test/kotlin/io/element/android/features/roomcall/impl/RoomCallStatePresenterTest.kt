/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomcall.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.api.CurrentCall
import io.element.android.features.call.api.CurrentCallService
import io.element.android.features.call.test.FakeCurrentCallService
import io.element.android.features.enterprise.test.FakeEnterpriseService
import io.element.android.features.roomcall.api.RoomCallState
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.tests.testutils.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomCallStatePresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserJoinCallResult = { Result.success(false) },
            )
        )
        val presenter = createRoomCallStatePresenter(joinedRoom = room)
        presenter.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(
                RoomCallState.StandBy(
                    canStartCall = false,
                )
            )
        }
    }

    @Test
    fun `present - element call not available`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserJoinCallResult = { Result.success(false) },
            )
        )
        val presenter = createRoomCallStatePresenter(
            joinedRoom = room,
            isElementCallAvailable = false,
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState).isEqualTo(
                RoomCallState.Unavailable
            )
        }
    }

    @Test
    fun `present - initial state - user can join call`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserJoinCallResult = { Result.success(true) },
            )
        )
        val presenter = createRoomCallStatePresenter(joinedRoom = room)
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
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserJoinCallResult = { Result.success(false) },
                initialRoomInfo = aRoomInfo(hasRoomCall = true),
            )
        )
        val presenter = createRoomCallStatePresenter(joinedRoom = room)
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
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserJoinCallResult = { Result.success(true) },
            ).apply {
                givenRoomInfo(
                    aRoomInfo(
                        hasRoomCall = true,
                        activeRoomCallParticipants = listOf(sessionId),
                    )
                )
            }
        )
        val presenter = createRoomCallStatePresenter(joinedRoom = room)
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
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserJoinCallResult = { Result.success(true) },
            ).apply {
                givenRoomInfo(
                    aRoomInfo(
                        hasRoomCall = true,
                        activeRoomCallParticipants = listOf(sessionId),
                    )
                )
            }
        )
        val presenter = createRoomCallStatePresenter(
            joinedRoom = room,
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
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(
                canUserJoinCallResult = { Result.success(true) },
            ).apply {
                givenRoomInfo(
                    aRoomInfo(
                        hasRoomCall = true,
                        activeRoomCallParticipants = listOf(sessionId),
                    )
                )
            }
        )
        val currentCall = MutableStateFlow<CurrentCall>(CurrentCall.RoomCall(room.roomId))
        val currentCallService = FakeCurrentCallService(currentCall = currentCall)
        val presenter = createRoomCallStatePresenter(
            joinedRoom = room,
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
        joinedRoom: JoinedRoom,
        currentCallService: CurrentCallService = FakeCurrentCallService(),
        isElementCallAvailable: Boolean = true,
    ): RoomCallStatePresenter {
        return RoomCallStatePresenter(
            room = joinedRoom,
            currentCallService = currentCallService,
            enterpriseService = FakeEnterpriseService(
                isElementCallAvailableResult = { isElementCallAvailable },
            ),
        )
    }
}
