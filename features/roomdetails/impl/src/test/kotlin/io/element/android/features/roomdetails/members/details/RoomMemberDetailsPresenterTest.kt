/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class RoomMemberDetailsPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - returns the room member's data, then updates it if needed`() = runTest {
        val roomMember = aRoomMember(displayName = "Alice")
        val room = aMatrixRoom(
            userDisplayNameResult = { Result.success("A custom name") },
            userAvatarUrlResult = { Result.success("A custom avatar") },
            getUpdatedMemberResult = { Result.success(roomMember) },
        ).apply {
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
            assertThat(initialState.dmRoomId).isEqualTo(A_ROOM_ID)
            assertThat(initialState.canCall).isFalse()
            skipItems(1)
            val loadedState = awaitItem()
            assertThat(loadedState.userName).isEqualTo("A custom name")
            assertThat(loadedState.avatarUrl).isEqualTo("A custom avatar")
        }
    }

    @Test
    fun `present - will recover when retrieving room member details fails`() = runTest {
        val roomMember = aRoomMember(displayName = "Alice")
        val room = aMatrixRoom(
            userDisplayNameResult = { Result.failure(Throwable()) },
            userAvatarUrlResult = { Result.failure(Throwable()) },
            getUpdatedMemberResult = { Result.failure(AN_EXCEPTION) },
        ).apply {
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
        val room = aMatrixRoom(
            userDisplayNameResult = { Result.success(null) },
            userAvatarUrlResult = { Result.success(null) },
            getUpdatedMemberResult = { Result.success(roomMember) }
        ).apply {
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
        val room = aMatrixRoom(
            userDisplayNameResult = { Result.failure(Exception("Not a member!")) },
            userAvatarUrlResult = { Result.failure(Exception("Not a member!")) },
            getUpdatedMemberResult = { Result.failure(AN_EXCEPTION) },
        )
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
        val presenter = createRoomMemberDetailsPresenter(
            room = aMatrixRoom(
                getUpdatedMemberResult = { Result.failure(AN_EXCEPTION) },
                userDisplayNameResult = { Result.success("Alice") },
                userAvatarUrlResult = { Result.success("anAvatarUrl") },
            )
        )
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
            room = aMatrixRoom(
                getUpdatedMemberResult = { Result.failure(AN_EXCEPTION) },
                userDisplayNameResult = { Result.success("Alice") },
                userAvatarUrlResult = { Result.success("anAvatarUrl") },
            ),
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
        val presenter = createRoomMemberDetailsPresenter(
            client = matrixClient,
            room = aMatrixRoom(
                getUpdatedMemberResult = { Result.success(aRoomMember(displayName = "Alice")) },
                userDisplayNameResult = { Result.success("Alice") },
                userAvatarUrlResult = { Result.success("anAvatarUrl") },
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(UserProfileEvents.BlockUser(needsConfirmation = false))
            assertThat(awaitItem().isBlocked.isLoading()).isTrue()
            skipItems(2)
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
        val presenter = createRoomMemberDetailsPresenter(
            room = aMatrixRoom(
                getUpdatedMemberResult = { Result.success(aRoomMember(displayName = "Alice")) },
                userDisplayNameResult = { Result.success("Alice") },
                userAvatarUrlResult = { Result.success("anAvatarUrl") },
            ),
            client = matrixClient,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(UserProfileEvents.UnblockUser(needsConfirmation = false))
            assertThat(awaitItem().isBlocked.isLoading()).isTrue()
            skipItems(2)
            val errorState = awaitItem()
            assertThat(errorState.isBlocked.errorOrNull()).isEqualTo(A_THROWABLE)
            // Clear error
            initialState.eventSink(UserProfileEvents.ClearBlockUserError)
            assertThat(awaitItem().isBlocked).isEqualTo(AsyncData.Success(true))
        }
    }

    @Test
    fun `present - UnblockUser needing confirmation displays confirmation dialog`() = runTest {
        val presenter = createRoomMemberDetailsPresenter(
            room = aMatrixRoom(
                getUpdatedMemberResult = { Result.failure(AN_EXCEPTION) },
                userDisplayNameResult = { Result.success("Alice") },
                userAvatarUrlResult = { Result.success("anAvatarUrl") },
            ),
        )
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
        val presenter = createRoomMemberDetailsPresenter(
            room = aMatrixRoom(
                getUpdatedMemberResult = { Result.success(aRoomMember(displayName = "Alice")) },
                userDisplayNameResult = { Result.success("Alice") },
                userAvatarUrlResult = { Result.success("anAvatarUrl") },
            ),
            startDMAction = startDMAction,
        )
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
            skipItems(2)
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
        room: MatrixRoom,
        buildMeta: BuildMeta = aBuildMeta(),
        client: MatrixClient = FakeMatrixClient(),
        roomMemberId: UserId = UserId("@alice:server.org"),
        startDMAction: StartDMAction = FakeStartDMAction()
    ): RoomMemberDetailsPresenter {
        return RoomMemberDetailsPresenter(
            roomMemberId = roomMemberId,
            buildMeta = buildMeta,
            client = client,
            room = room,
            startDMAction = startDMAction
        )
    }
}
