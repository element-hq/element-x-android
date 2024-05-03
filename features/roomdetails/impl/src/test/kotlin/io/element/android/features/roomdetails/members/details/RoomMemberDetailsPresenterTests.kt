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

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.createroom.api.StartDMAction
import io.element.android.features.createroom.test.FakeStartDMAction
import io.element.android.features.roomdetails.aMatrixRoom
import io.element.android.features.roomdetails.impl.members.aRoomMember
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsPresenter
import io.element.android.features.userprofile.shared.UserProfileEvents
import io.element.android.features.userprofile.shared.UserProfileState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class RoomMemberDetailsPresenterTests {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - returns the room member's data, then updates it if needed`() = runTest {
        val roomMember = aRoomMember(displayName = "Alice")
        val room = aMatrixRoom().apply {
            givenUserDisplayNameResult(Result.success("A custom name"))
            givenUserAvatarUrlResult(Result.success("A custom avatar"))
            givenRoomMembersState(MatrixRoomMembersState.Ready(persistentListOf(roomMember)))
        }
        val presenter = createRoomMemberDetailsPresenter(
            room = room,
            roomMemberId = roomMember.userId
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.userId).isEqualTo(roomMember.userId)
            assertThat(initialState.userName).isEqualTo(roomMember.displayName)
            assertThat(initialState.avatarUrl).isEqualTo(roomMember.avatarUrl)
            assertThat(initialState.isBlocked).isEqualTo(AsyncData.Success(roomMember.isIgnored))
            skipItems(1)
            val loadedState = awaitItem()
            assertThat(loadedState.userName).isEqualTo("A custom name")
            assertThat(loadedState.avatarUrl).isEqualTo("A custom avatar")
        }
    }

    @Test
    fun `present - will recover when retrieving room member details fails`() = runTest {
        val roomMember = aRoomMember(displayName = "Alice")
        val room = aMatrixRoom().apply {
            givenUserDisplayNameResult(Result.failure(Throwable()))
            givenUserAvatarUrlResult(Result.failure(Throwable()))
            givenRoomMembersState(MatrixRoomMembersState.Ready(persistentListOf(roomMember)))
        }
        val presenter = createRoomMemberDetailsPresenter(
            room = room,
            roomMemberId = roomMember.userId
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.userName).isEqualTo(roomMember.displayName)
            assertThat(initialState.avatarUrl).isEqualTo(roomMember.avatarUrl)

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - will fallback to original data if the updated data is null`() = runTest {
        val roomMember = aRoomMember(displayName = "Alice")
        val room = aMatrixRoom().apply {
            givenUserDisplayNameResult(Result.success(null))
            givenUserAvatarUrlResult(Result.success(null))
            givenRoomMembersState(MatrixRoomMembersState.Ready(persistentListOf(roomMember)))
        }
        val presenter = createRoomMemberDetailsPresenter(
            room = room,
            roomMemberId = roomMember.userId
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.userName).isEqualTo(roomMember.displayName)
            assertThat(initialState.avatarUrl).isEqualTo(roomMember.avatarUrl)

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - will fallback to user profile if user is not a member of the room`() = runTest {
        val bobProfile = aMatrixUser("@bob:server.org", "Bob", avatarUrl = "anAvatarUrl")
        val room = aMatrixRoom().apply {
            givenUserDisplayNameResult(Result.failure(Exception("Not a member!")))
            givenUserAvatarUrlResult(Result.failure(Exception("Not a member!")))
        }
        val client = FakeMatrixClient().apply {
            givenGetProfileResult(bobProfile.userId, Result.success(bobProfile))
        }
        val presenter = createRoomMemberDetailsPresenter(
            client = client,
            room = room,
            roomMemberId = UserId("@bob:server.org")
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(2)
            val initialState = awaitFirstItem()
            assertThat(initialState.userName).isEqualTo("Bob")
            assertThat(initialState.avatarUrl).isEqualTo("anAvatarUrl")

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - BlockUser needing confirmation displays confirmation dialog`() = runTest {
        val presenter = createRoomMemberDetailsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(UserProfileEvents.BlockUser(needsConfirmation = true))

            val dialogState = awaitItem()
            assertThat(dialogState.displayConfirmationDialog).isEqualTo(UserProfileState.ConfirmationDialog.Block)

            dialogState.eventSink(UserProfileEvents.ClearConfirmationDialog)
            assertThat(awaitItem().displayConfirmationDialog).isNull()

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - BlockUser and UnblockUser without confirmation change the 'blocked' state`() = runTest {
        val client = FakeMatrixClient()
        val roomMember = aRoomMember()
        val presenter = createRoomMemberDetailsPresenter(
            client = client,
            roomMemberId = roomMember.userId
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(UserProfileEvents.BlockUser(needsConfirmation = false))
            assertThat(awaitItem().isBlocked.isLoading()).isTrue()
            client.emitIgnoreUserList(listOf(roomMember.userId))
            assertThat(awaitItem().isBlocked.dataOrNull()).isTrue()

            initialState.eventSink(UserProfileEvents.UnblockUser(needsConfirmation = false))
            assertThat(awaitItem().isBlocked.isLoading()).isTrue()
            client.emitIgnoreUserList(listOf())
            assertThat(awaitItem().isBlocked.dataOrNull()).isFalse()
        }
    }

    @Test
    fun `present - BlockUser with error`() = runTest {
        val matrixClient = FakeMatrixClient()
        matrixClient.givenIgnoreUserResult(Result.failure(A_THROWABLE))
        val presenter = createRoomMemberDetailsPresenter(client = matrixClient)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(UserProfileEvents.BlockUser(needsConfirmation = false))
            assertThat(awaitItem().isBlocked.isLoading()).isTrue()
            val errorState = awaitItem()
            assertThat(errorState.isBlocked.errorOrNull()).isEqualTo(A_THROWABLE)
            // Clear error
            initialState.eventSink(UserProfileEvents.ClearBlockUserError)
            assertThat(awaitItem().isBlocked).isEqualTo(AsyncData.Success(false))
        }
    }

    @Test
    fun `present - UnblockUser with error`() = runTest {
        val matrixClient = FakeMatrixClient()
        matrixClient.givenUnignoreUserResult(Result.failure(A_THROWABLE))
        val presenter = createRoomMemberDetailsPresenter(client = matrixClient)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(UserProfileEvents.UnblockUser(needsConfirmation = false))
            assertThat(awaitItem().isBlocked.isLoading()).isTrue()
            val errorState = awaitItem()
            assertThat(errorState.isBlocked.errorOrNull()).isEqualTo(A_THROWABLE)
            // Clear error
            initialState.eventSink(UserProfileEvents.ClearBlockUserError)
            assertThat(awaitItem().isBlocked).isEqualTo(AsyncData.Success(true))
        }
    }

    @Test
    fun `present - UnblockUser needing confirmation displays confirmation dialog`() = runTest {
        val presenter = createRoomMemberDetailsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(UserProfileEvents.UnblockUser(needsConfirmation = true))

            val dialogState = awaitItem()
            assertThat(dialogState.displayConfirmationDialog).isEqualTo(UserProfileState.ConfirmationDialog.Unblock)

            dialogState.eventSink(UserProfileEvents.ClearConfirmationDialog)
            assertThat(awaitItem().displayConfirmationDialog).isNull()

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - start DM action complete scenario`() = runTest {
        val startDMAction = FakeStartDMAction()
        val presenter = createRoomMemberDetailsPresenter(startDMAction = startDMAction)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.startDmActionState).isInstanceOf(AsyncAction.Uninitialized::class.java)
            val startDMSuccessResult = AsyncAction.Success(A_ROOM_ID)
            val startDMFailureResult = AsyncAction.Failure(A_THROWABLE)

            // Failure
            startDMAction.givenExecuteResult(startDMFailureResult)
            initialState.eventSink(UserProfileEvents.StartDM)
            assertThat(awaitItem().startDmActionState).isInstanceOf(AsyncAction.Loading::class.java)
            awaitItem().also { state ->
                assertThat(state.startDmActionState).isEqualTo(startDMFailureResult)
                state.eventSink(UserProfileEvents.ClearStartDMState)
            }

            // Success
            startDMAction.givenExecuteResult(startDMSuccessResult)
            awaitItem().also { state ->
                assertThat(state.startDmActionState).isEqualTo(AsyncAction.Uninitialized)
                state.eventSink(UserProfileEvents.StartDM)
            }
            assertThat(awaitItem().startDmActionState).isInstanceOf(AsyncAction.Loading::class.java)
            awaitItem().also { state ->
                assertThat(state.startDmActionState).isEqualTo(startDMSuccessResult)
            }
        }
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitFirstItem(): T {
        skipItems(1)
        return awaitItem()
    }

    private fun createRoomMemberDetailsPresenter(
        client: MatrixClient = FakeMatrixClient(),
        room: MatrixRoom = aMatrixRoom(),
        roomMemberId: UserId = UserId("@alice:server.org"),
        startDMAction: StartDMAction = FakeStartDMAction()
    ): RoomMemberDetailsPresenter {
        return RoomMemberDetailsPresenter(
            roomMemberId = roomMemberId,
            client = client,
            room = room,
            startDMAction = startDMAction
        )
    }
}
