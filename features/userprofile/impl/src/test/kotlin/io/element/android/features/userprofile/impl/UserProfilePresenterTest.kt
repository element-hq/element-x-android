/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.userprofile.impl

import app.cash.turbine.ReceiveTurbine
import com.google.common.truth.Truth.assertThat
import io.element.android.features.createroom.api.StartDMAction
import io.element.android.features.createroom.test.FakeStartDMAction
import io.element.android.features.userprofile.api.UserProfileEvents
import io.element.android.features.userprofile.api.UserProfileState
import io.element.android.features.userprofile.impl.root.UserProfilePresenter
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.awaitLastSequentialItem
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class UserProfilePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - returns the user profile data`() = runTest {
        val matrixUser = aMatrixUser(A_USER_ID.value, "Alice", "anAvatarUrl")
        val client = createFakeMatrixClient().apply {
            givenGetProfileResult(A_USER_ID, Result.success(matrixUser))
        }
        val presenter = createUserProfilePresenter(
            client = client,
        )
        presenter.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.userId).isEqualTo(matrixUser.userId)
            assertThat(initialState.userName).isEqualTo(matrixUser.displayName)
            assertThat(initialState.avatarUrl).isEqualTo(matrixUser.avatarUrl)
            assertThat(initialState.isBlocked).isEqualTo(AsyncData.Success(false))
            assertThat(initialState.isVerified.dataOrNull()).isFalse()
            assertThat(initialState.dmRoomId).isEqualTo(A_ROOM_ID)
            assertThat(initialState.canCall).isFalse()
        }
    }

    @Test
    fun `present - canCall is true when all the conditions are met`() {
        testCanCall(
            expectedResult = true,
        )
    }

    @Test
    fun `present - canCall is false when canUserJoinCall returns false`() {
        testCanCall(
            canUserJoinCallResult = Result.success(false),
            expectedResult = false,
        )
    }

    @Test
    fun `present - canCall is false when canUserJoinCall fails`() {
        testCanCall(
            canUserJoinCallResult = Result.failure(AN_EXCEPTION),
            expectedResult = false,
        )
    }

    @Test
    fun `present - canCall is false when there is no DM`() {
        testCanCall(
            dmRoom = null,
            expectedResult = false,
        )
    }

    @Test
    fun `present - canCall is false when room is not found`() {
        testCanCall(
            canFindRoom = false,
            expectedResult = false,
        )
    }

    private fun testCanCall(
        canUserJoinCallResult: Result<Boolean> = Result.success(true),
        dmRoom: RoomId? = A_ROOM_ID,
        canFindRoom: Boolean = true,
        expectedResult: Boolean,
    ) = runTest {
        val room = FakeMatrixRoom(
            canUserJoinCallResult = { canUserJoinCallResult },
        )
        val client = createFakeMatrixClient().apply {
            if (canFindRoom) {
                givenGetRoomResult(A_ROOM_ID, room)
            }
            givenFindDmResult(dmRoom)
        }
        val presenter = createUserProfilePresenter(
            userId = A_USER_ID_2,
            client = client,
        )
        presenter.test {
            val initialState = awaitLastSequentialItem()
            assertThat(initialState.canCall).isEqualTo(expectedResult)
        }
    }

    @Test
    fun `present - returns empty data in case of failure`() = runTest {
        val client = createFakeMatrixClient().apply {
            givenGetProfileResult(A_USER_ID, Result.failure(AN_EXCEPTION))
        }
        val presenter = createUserProfilePresenter(
            client = client,
        )
        presenter.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.userId).isEqualTo(A_USER_ID)
            assertThat(initialState.userName).isNull()
            assertThat(initialState.avatarUrl).isNull()
            assertThat(initialState.isBlocked).isEqualTo(AsyncData.Success(false))
        }
    }

    @Test
    fun `present - BlockUser needing confirmation displays confirmation dialog`() = runTest {
        val presenter = createUserProfilePresenter()
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(UserProfileEvents.BlockUser(needsConfirmation = true))

            val dialogState = awaitItem()
            assertThat(dialogState.displayConfirmationDialog).isEqualTo(UserProfileState.ConfirmationDialog.Block)

            dialogState.eventSink(UserProfileEvents.ClearConfirmationDialog)
            assertThat(awaitItem().displayConfirmationDialog).isNull()
        }
    }

    @Test
    fun `present - BlockUser and UnblockUser without confirmation change the 'blocked' state`() = runTest {
        val client = createFakeMatrixClient()
        val presenter = createUserProfilePresenter(
            client = client,
            userId = A_USER_ID
        )
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(UserProfileEvents.BlockUser(needsConfirmation = false))
            assertThat(awaitItem().isBlocked.isLoading()).isTrue()
            client.emitIgnoreUserList(listOf(A_USER_ID))
            assertThat(awaitItem().isBlocked.dataOrNull()).isTrue()

            initialState.eventSink(UserProfileEvents.UnblockUser(needsConfirmation = false))
            assertThat(awaitItem().isBlocked.isLoading()).isTrue()
            client.emitIgnoreUserList(listOf())
            assertThat(awaitItem().isBlocked.dataOrNull()).isFalse()
        }
    }

    @Test
    fun `present - BlockUser with error`() = runTest {
        val matrixClient = createFakeMatrixClient()
        matrixClient.givenIgnoreUserResult(Result.failure(A_THROWABLE))
        val presenter = createUserProfilePresenter(client = matrixClient)
        presenter.test {
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
        val matrixClient = createFakeMatrixClient()
        matrixClient.givenUnignoreUserResult(Result.failure(A_THROWABLE))
        val presenter = createUserProfilePresenter(client = matrixClient)
        presenter.test {
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
        val presenter = createUserProfilePresenter()
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(UserProfileEvents.UnblockUser(needsConfirmation = true))

            val dialogState = awaitItem()
            assertThat(dialogState.displayConfirmationDialog).isEqualTo(UserProfileState.ConfirmationDialog.Unblock)

            dialogState.eventSink(UserProfileEvents.ClearConfirmationDialog)
            assertThat(awaitItem().displayConfirmationDialog).isNull()
        }
    }

    @Test
    fun `present - start DM action complete scenario`() = runTest {
        val startDMAction = FakeStartDMAction()
        val presenter = createUserProfilePresenter(startDMAction = startDMAction)
        presenter.test {
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

    @Test
    fun `present - when user is verified, the value in the state is true`() = runTest {
        val client = createFakeMatrixClient(isUserVerified = true)
        val presenter = createUserProfilePresenter(
            client = client,
        )
        presenter.test {
            assertThat(awaitItem().isVerified.isUninitialized()).isTrue()
            assertThat(awaitItem().isVerified.isLoading()).isTrue()
            assertThat(awaitItem().isVerified.dataOrNull()).isTrue()
        }
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitFirstItem(): T {
        skipItems(2)
        return awaitItem()
    }

    private fun createFakeMatrixClient(
        isUserVerified: Boolean = false,
    ) = FakeMatrixClient(
        encryptionService = FakeEncryptionService(
            isUserVerifiedResult = { Result.success(isUserVerified) }
        ),
    )

    private fun createUserProfilePresenter(
        client: MatrixClient = createFakeMatrixClient(),
        userId: UserId = UserId("@alice:server.org"),
        startDMAction: StartDMAction = FakeStartDMAction()
    ): UserProfilePresenter {
        return UserProfilePresenter(
            userId = userId,
            client = client,
            startDMAction = startDMAction
        )
    }
}
