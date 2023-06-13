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

package io.element.android.features.roomdetails.members.details

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth
import io.element.android.features.roomdetails.aMatrixRoom
import io.element.android.features.roomdetails.impl.members.aRoomMember
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsEvents
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsPresenter
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsState
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.test.aFakeMatrixClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class RoomMemberDetailsPresenterTests {

    @Test
    fun `present - returns the room member's data, then updates it if needed`() = runTest {
        val roomMember = aRoomMember(displayName = "Alice")
        val room = aMatrixRoom().apply {
            givenUserDisplayNameResult(Result.success("A custom name"))
            givenUserAvatarUrlResult(Result.success("A custom avatar"))
            givenRoomMembersState(MatrixRoomMembersState.Ready(listOf(roomMember)))
        }
        val presenter = RoomMemberDetailsPresenter(aFakeMatrixClient(), room, roomMember.userId)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.userId).isEqualTo(roomMember.userId.value)
            Truth.assertThat(initialState.userName).isEqualTo(roomMember.displayName)
            Truth.assertThat(initialState.avatarUrl).isEqualTo(roomMember.avatarUrl)
            Truth.assertThat(initialState.isBlocked).isEqualTo(roomMember.isIgnored)

            val loadedState = awaitItem()
            Truth.assertThat(loadedState.userName).isEqualTo("A custom name")
            Truth.assertThat(loadedState.avatarUrl).isEqualTo("A custom avatar")
        }
    }

    @Test
    fun `present - will recover when retrieving room member details fails`() = runTest {
        val roomMember = aRoomMember(displayName = "Alice")
        val room = aMatrixRoom().apply {
            givenUserDisplayNameResult(Result.failure(Throwable()))
            givenUserAvatarUrlResult(Result.failure(Throwable()))
            givenRoomMembersState(MatrixRoomMembersState.Ready(listOf(roomMember)))
        }
        val presenter = RoomMemberDetailsPresenter(aFakeMatrixClient(), room, roomMember.userId)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.userName).isEqualTo(roomMember.displayName)
            Truth.assertThat(initialState.avatarUrl).isEqualTo(roomMember.avatarUrl)

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - will fallback to original data if the updated data is null`() = runTest {
        val roomMember = aRoomMember(displayName = "Alice")
        val room = aMatrixRoom().apply {
            givenUserDisplayNameResult(Result.success(null))
            givenUserAvatarUrlResult(Result.success(null))
            givenRoomMembersState(MatrixRoomMembersState.Ready(listOf(roomMember)))
        }
        val presenter = RoomMemberDetailsPresenter(aFakeMatrixClient(), room, roomMember.userId)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.userName).isEqualTo(roomMember.displayName)
            Truth.assertThat(initialState.avatarUrl).isEqualTo(roomMember.avatarUrl)

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - BlockUser needing confirmation displays confirmation dialog`() = runTest {
        val room = aMatrixRoom()
        val roomMember = aRoomMember()
        val presenter = RoomMemberDetailsPresenter(aFakeMatrixClient(), room, roomMember.userId)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomMemberDetailsEvents.BlockUser(needsConfirmation = true))

            val dialogState = awaitItem()
            Truth.assertThat(dialogState.displayConfirmationDialog).isEqualTo(RoomMemberDetailsState.ConfirmationDialog.Block)

            dialogState.eventSink(RoomMemberDetailsEvents.ClearConfirmationDialog)
            Truth.assertThat(awaitItem().displayConfirmationDialog).isNull()

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - BlockUser and UnblockUser without confirmation change the 'blocked' state`() = runTest {
        val room = aMatrixRoom()
        val roomMember = aRoomMember()
        val presenter = RoomMemberDetailsPresenter(aFakeMatrixClient(), room, roomMember.userId)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomMemberDetailsEvents.BlockUser(needsConfirmation = false))
            Truth.assertThat(awaitItem().isBlocked).isTrue()

            initialState.eventSink(RoomMemberDetailsEvents.UnblockUser(needsConfirmation = false))
            Truth.assertThat(awaitItem().isBlocked).isFalse()
        }
    }

    @Test
    fun `present - UnblockUser needing confirmation displays confirmation dialog`() = runTest {
        val room = aMatrixRoom()
        val roomMember = aRoomMember()
        val presenter = RoomMemberDetailsPresenter(aFakeMatrixClient(), room, roomMember.userId)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomMemberDetailsEvents.UnblockUser(needsConfirmation = true))

            val dialogState = awaitItem()
            Truth.assertThat(dialogState.displayConfirmationDialog).isEqualTo(RoomMemberDetailsState.ConfirmationDialog.Unblock)

            dialogState.eventSink(RoomMemberDetailsEvents.ClearConfirmationDialog)
            Truth.assertThat(awaitItem().displayConfirmationDialog).isNull()

            ensureAllEventsConsumed()
        }
    }
}
