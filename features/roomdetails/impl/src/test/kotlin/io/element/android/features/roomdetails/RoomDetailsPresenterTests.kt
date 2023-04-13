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
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class RoomDetailsPresenterTests {

    private val roomMembershipObserver = RoomMembershipObserver(A_SESSION_ID)

    @Test
    fun `present - initial state is created from room info`() = runTest {
        val room = aMatrixRoom()
        val presenter = RoomDetailsPresenter(room, roomMembershipObserver)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.roomId).isEqualTo(room.roomId.value)
            Truth.assertThat(initialState.roomName).isEqualTo(room.name)
            Truth.assertThat(initialState.roomAvatarUrl).isEqualTo(room.avatarUrl)
            Truth.assertThat(initialState.roomTopic).isEqualTo(room.topic)
            Truth.assertThat(initialState.memberCount).isEqualTo(Async.Loading(null))
            Truth.assertThat(initialState.isEncrypted).isEqualTo(room.isEncrypted)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - room member count is calculated asynchronously`() = runTest {
        val room = aMatrixRoom()
        val presenter = RoomDetailsPresenter(room, roomMembershipObserver)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.memberCount).isEqualTo(Async.Loading(null))

            val finalState = awaitItem()
            Truth.assertThat(finalState.memberCount).isEqualTo(Async.Success(0))
        }
    }

    @Test
    fun `present - initial state with no room name`() = runTest {
        val room = aMatrixRoom(name = null)
        val presenter = RoomDetailsPresenter(room, roomMembershipObserver)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.roomName).isEqualTo(room.displayName)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - can handle error while fetching member count`() = runTest {
        val room = aMatrixRoom(name = null).apply {
            givenFetchMemberResult(Result.failure(Throwable()))
        }
        val presenter = RoomDetailsPresenter(room, roomMembershipObserver)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            Truth.assertThat(awaitItem().memberCount).isInstanceOf(Async.Failure::class.java)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - Leave with confirmation on private room shows a specific warning`() = runTest {
        val room = aMatrixRoom(isPublic = false)
        val presenter = RoomDetailsPresenter(room, roomMembershipObserver)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            // Allow room member count to load
            skipItems(1)

            initialState.eventSink(RoomDetailsEvent.LeaveRoom(needsConfirmation = true))
            val confirmationState = awaitItem()
            Truth.assertThat(confirmationState.displayLeaveRoomWarning).isEqualTo(LeaveRoomWarning.PrivateRoom)
        }
    }

    @Test
    fun `present - Leave with confirmation on empty room shows a specific warning`() = runTest {
        val room = aMatrixRoom(members = listOf(aRoomMember()))
        val presenter = RoomDetailsPresenter(room, roomMembershipObserver)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            // Allow room member count to load
            skipItems(1)

            initialState.eventSink(RoomDetailsEvent.LeaveRoom(needsConfirmation = true))
            val confirmationState = awaitItem()
            Truth.assertThat(confirmationState.displayLeaveRoomWarning).isEqualTo(LeaveRoomWarning.LastUserInRoom)
        }
    }

    @Test
    fun `present - Leave with confirmation shows a generic warning`() = runTest {
        val room = aMatrixRoom()
        val presenter = RoomDetailsPresenter(room, roomMembershipObserver)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            // Allow room member count to load
            skipItems(1)

            initialState.eventSink(RoomDetailsEvent.LeaveRoom(needsConfirmation = true))
            val confirmationState = awaitItem()
            Truth.assertThat(confirmationState.displayLeaveRoomWarning).isEqualTo(LeaveRoomWarning.Generic)
        }
    }

    @Test
    fun `present - Leave without confirmation leaves the room`() = runTest {
        val room = aMatrixRoom()
        val presenter = RoomDetailsPresenter(room, roomMembershipObserver)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            // Allow room member count to load
            skipItems(1)

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
        val presenter = RoomDetailsPresenter(room, roomMembershipObserver)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            // Allow room member count to load
            skipItems(1)

            initialState.eventSink(RoomDetailsEvent.LeaveRoom(needsConfirmation = false))
            val errorState = awaitItem()
            Truth.assertThat(errorState.error).isNotNull()
            errorState.eventSink(RoomDetailsEvent.ClearError)
            Truth.assertThat(awaitItem().error).isNull()
        }
    }
}

fun aMatrixRoom(
    roomId: RoomId = A_ROOM_ID,
    name: String? = A_ROOM_NAME,
    displayName: String = "A fallback display name",
    topic: String? = "A topic",
    avatarUrl: String? = "https://matrix.org/avatar.jpg",
    members: List<RoomMember> = emptyList(),
    isEncrypted: Boolean = true,
    isPublic: Boolean = true,
) = FakeMatrixRoom(
    roomId = roomId,
    name = name,
    displayName = displayName,
    topic = topic,
    avatarUrl = avatarUrl,
    members = members,
    isEncrypted = isEncrypted,
    isPublic = isPublic,
)

fun aRoomMember(
    userId: UserId = A_USER_ID,
    displayName: String? = null,
    avatarUrl: String? = null,
    membership: RoomMembershipState = RoomMembershipState.JOIN,
    isNameAmbiguous: Boolean = false,
    powerLevel: Long = 0L,
    normalizedPowerLevel: Long = 0L,
    isIgnored: Boolean = false,
) = RoomMember(
    userId = userId.value,
    displayName = displayName,
    avatarUrl = avatarUrl,
    membership = membership,
    isNameAmbiguous = isNameAmbiguous,
    powerLevel = powerLevel,
    normalizedPowerLevel = normalizedPowerLevel,
    isIgnored = isIgnored,
)
