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

package io.element.android.features.roomdetails

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth
import io.element.android.features.roomdetails.impl.LeaveRoomWarning
import io.element.android.features.roomdetails.impl.RoomDetailsEvent
import io.element.android.features.roomdetails.impl.RoomDetailsPresenter
import io.element.android.features.roomdetails.impl.RoomDetailsType
import io.element.android.features.roomdetails.impl.members.aRoomMember
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsPresenter
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class RoomDetailsPresenterTests {

    private val roomMembershipObserver = RoomMembershipObserver()
    private val testCoroutineDispatchers = testCoroutineDispatchers()

    private fun aRoomDetailsPresenter(room: MatrixRoom): RoomDetailsPresenter {
        val roomMemberDetailsPresenterFactory = object : RoomMemberDetailsPresenter.Factory {
            override fun create(roomMemberId: UserId): RoomMemberDetailsPresenter {
                return RoomMemberDetailsPresenter(aMatrixClient(), room, roomMemberId)
            }
        }
        return RoomDetailsPresenter(room, roomMembershipObserver, testCoroutineDispatchers, roomMemberDetailsPresenterFactory)
    }

    @Test
    fun `present - initial state is created from room info`() = runTest {
        val room = aMatrixRoom()
        val presenter = aRoomDetailsPresenter(room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.roomId).isEqualTo(room.roomId.value)
            Truth.assertThat(initialState.roomName).isEqualTo(room.name)
            Truth.assertThat(initialState.roomAvatarUrl).isEqualTo(room.avatarUrl)
            Truth.assertThat(initialState.roomTopic).isEqualTo(room.topic)
            Truth.assertThat(initialState.memberCount).isEqualTo(Async.Uninitialized)
            Truth.assertThat(initialState.isEncrypted).isEqualTo(room.isEncrypted)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - room member count is calculated asynchronously`() = runTest {
        val error = RuntimeException()
        val room = aMatrixRoom()
        val roomMembers = listOf(
            aRoomMember(A_USER_ID),
            aRoomMember(A_USER_ID_2),
        )
        val presenter = aRoomDetailsPresenter(room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            room.givenRoomMembersState(MatrixRoomMembersState.Unknown)
            val initialState = awaitItem()
            Truth.assertThat(initialState.memberCount).isEqualTo(Async.Uninitialized)

            room.givenRoomMembersState(MatrixRoomMembersState.Pending(null))
            val loadingState = awaitItem()
            Truth.assertThat(loadingState.memberCount).isEqualTo(Async.Loading(null))

            room.givenRoomMembersState(MatrixRoomMembersState.Error(error))
            //skipItems(1)
            val failureState = awaitItem()
            Truth.assertThat(failureState.memberCount).isEqualTo(Async.Failure(error, null))

            room.givenRoomMembersState(MatrixRoomMembersState.Ready(roomMembers))
            //skipItems(1)
            val successState = awaitItem()
            Truth.assertThat(successState.memberCount).isEqualTo(Async.Success(roomMembers.size))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state with no room name`() = runTest {
        val room = aMatrixRoom(name = null)
        val presenter = aRoomDetailsPresenter(room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.roomName).isEqualTo(room.displayName)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - initial state with DM member sets custom DM roomType`() = runTest {
        val myRoomMember = aRoomMember(A_SESSION_ID)
        val otherRoomMember = aRoomMember(A_USER_ID_2)
        val room = aMatrixRoom(
            isEncrypted = true,
            isDirect = true,
        ).apply {
            val roomMembers = listOf(myRoomMember, otherRoomMember)
            givenRoomMembersState(MatrixRoomMembersState.Ready(roomMembers))
        }
        val presenter = aRoomDetailsPresenter(room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.roomType).isEqualTo(RoomDetailsType.Dm(otherRoomMember))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - Leave with confirmation on private room shows a specific warning`() = runTest {
        val room = aMatrixRoom(isPublic = false).apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(emptyList()))
        }
        val presenter = aRoomDetailsPresenter(room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomDetailsEvent.LeaveRoom(needsConfirmation = true))
            val confirmationState = awaitItem()
            Truth.assertThat(confirmationState.displayLeaveRoomWarning).isEqualTo(LeaveRoomWarning.PrivateRoom)
        }
    }

    @Test
    fun `present - Leave with confirmation on empty room shows a specific warning`() = runTest {
        val room = aMatrixRoom().apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(listOf(aRoomMember())))
        }
        val presenter = aRoomDetailsPresenter(room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomDetailsEvent.LeaveRoom(needsConfirmation = true))
            val confirmationState = awaitItem()
            Truth.assertThat(confirmationState.displayLeaveRoomWarning).isEqualTo(LeaveRoomWarning.LastUserInRoom)
        }
    }

    @Test
    fun `present - Leave with confirmation shows a generic warning`() = runTest {
        val room = aMatrixRoom().apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(emptyList()))
        }
        val presenter = aRoomDetailsPresenter(room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomDetailsEvent.LeaveRoom(needsConfirmation = true))
            val confirmationState = awaitItem()
            Truth.assertThat(confirmationState.displayLeaveRoomWarning).isEqualTo(LeaveRoomWarning.Generic)
        }
    }

    @Test
    fun `present - Leave without confirmation leaves the room`() = runTest {
        val room = aMatrixRoom().apply {
            givenRoomMembersState(MatrixRoomMembersState.Ready(emptyList()))
        }
        val presenter = aRoomDetailsPresenter(room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomDetailsEvent.LeaveRoom(needsConfirmation = false))

            cancelAndIgnoreRemainingEvents()
        }

        // Membership observer should receive a 'left room' change
        roomMembershipObserver.updates.take(1)
            .onEach { update -> Truth.assertThat(update.change).isEqualTo(MembershipChange.LEFT) }
            .collect()
    }

    @Test
    fun `present - ClearError removes any error present`() = runTest {
        val room = aMatrixRoom().apply {
            givenLeaveRoomError(Throwable())
        }
        val presenter = aRoomDetailsPresenter(room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomDetailsEvent.LeaveRoom(needsConfirmation = false))
            val errorState = awaitItem()
            Truth.assertThat(errorState.error).isNotNull()
            errorState.eventSink(RoomDetailsEvent.ClearError)
            Truth.assertThat(awaitItem().error).isNull()
        }
    }
}

fun aMatrixClient(
    sessionId: SessionId = A_SESSION_ID,
) = FakeMatrixClient()

fun aMatrixRoom(
    roomId: RoomId = A_ROOM_ID,
    name: String? = A_ROOM_NAME,
    displayName: String = "A fallback display name",
    topic: String? = "A topic",
    avatarUrl: String? = "https://matrix.org/avatar.jpg",
    isEncrypted: Boolean = true,
    isPublic: Boolean = true,
    isDirect: Boolean = false,
) = FakeMatrixRoom(
    roomId = roomId,
    name = name,
    displayName = displayName,
    topic = topic,
    avatarUrl = avatarUrl,
    isEncrypted = isEncrypted,
    isPublic = isPublic,
    isDirect = isDirect,
)

