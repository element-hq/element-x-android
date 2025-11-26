/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.userprofile.impl

import androidx.compose.runtime.MutableState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.test.FakeSessionEnterpriseService
import io.element.android.features.invitepeople.test.FakeStartDMAction
import io.element.android.features.startchat.api.ConfirmingStartDmWithMatrixUser
import io.element.android.features.startchat.api.StartDMAction
import io.element.android.features.userprofile.api.UserProfileEvents
import io.element.android.features.userprofile.api.UserProfileState
import io.element.android.features.userprofile.api.UserProfileVerificationState
import io.element.android.features.userprofile.impl.root.UserProfilePresenter
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
            assertThat(initialState.verificationState).isEqualTo(UserProfileVerificationState.UNKNOWN)
            assertThat(initialState.dmRoomId).isEqualTo(A_ROOM_ID)
            assertThat(initialState.canCall).isFalse()
        }
    }

    @Test
    fun `present - canCall is true when all the conditions are met`() {
        testCanCall(
            expectedResult = true,
            skipItems = 3,
            checkThatRoomIsDestroyed = true,
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

    @Test
    fun `present - canCall is false when call is not available`() {
        testCanCall(
            isElementCallAvailable = false,
            expectedResult = false,
        )
    }

    private fun testCanCall(
        isElementCallAvailable: Boolean = true,
        canUserJoinCallResult: Result<Boolean> = Result.success(true),
        dmRoom: RoomId? = A_ROOM_ID,
        canFindRoom: Boolean = true,
        expectedResult: Boolean,
        skipItems: Int = 1,
        checkThatRoomIsDestroyed: Boolean = false,
    ) = runTest {
        val room = FakeBaseRoom(
            canUserJoinCallResult = { canUserJoinCallResult },
        )
        val client = createFakeMatrixClient().apply {
            if (canFindRoom) {
                givenGetRoomResult(A_ROOM_ID, room)
            }
            givenFindDmResult(Result.success(dmRoom))
        }
        val presenter = createUserProfilePresenter(
            userId = A_USER_ID_2,
            client = client,
            isElementCallAvailable = isElementCallAvailable,
        )
        presenter.test {
            val initialState = awaitFirstItem(skipItems)
            assertThat(initialState.canCall).isEqualTo(expectedResult)
        }
        if (checkThatRoomIsDestroyed) {
            room.assertDestroyed()
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
        val ignoredUsersFlow = MutableStateFlow(persistentListOf<UserId>())
        val client = createFakeMatrixClient(ignoredUsersFlow = ignoredUsersFlow)
        val presenter = createUserProfilePresenter(
            client = client,
            userId = A_USER_ID
        )
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(UserProfileEvents.BlockUser(needsConfirmation = false))
            assertThat(awaitItem().isBlocked.isLoading()).isTrue()
            ignoredUsersFlow.emit(persistentListOf(A_USER_ID))
            assertThat(awaitItem().isBlocked.dataOrNull()).isTrue()

            initialState.eventSink(UserProfileEvents.UnblockUser(needsConfirmation = false))
            assertThat(awaitItem().isBlocked.isLoading()).isTrue()
            ignoredUsersFlow.emit(persistentListOf())
            assertThat(awaitItem().isBlocked.dataOrNull()).isFalse()
        }
    }

    @Test
    fun `present - BlockUser with error`() = runTest {
        val matrixClient = createFakeMatrixClient(
            ignoreUserResult = { Result.failure(AN_EXCEPTION) }
        )
        val presenter = createUserProfilePresenter(client = matrixClient)
        presenter.test {
            val initialState = awaitFirstItem(count = 2)
            initialState.eventSink(UserProfileEvents.BlockUser(needsConfirmation = false))
            assertThat(awaitItem().isBlocked.isLoading()).isTrue()
            val errorState = awaitItem()
            assertThat(errorState.isBlocked.errorOrNull()).isEqualTo(AN_EXCEPTION)
            // Clear error
            initialState.eventSink(UserProfileEvents.ClearBlockUserError)
            assertThat(awaitItem().isBlocked).isEqualTo(AsyncData.Success(false))
        }
    }

    @Test
    fun `present - UnblockUser with error`() = runTest {
        val matrixClient = createFakeMatrixClient(
            unIgnoreUserResult = { Result.failure(AN_EXCEPTION) }
        )
        val presenter = createUserProfilePresenter(client = matrixClient)
        presenter.test {
            val initialState = awaitFirstItem(count = 2)
            initialState.eventSink(UserProfileEvents.UnblockUser(needsConfirmation = false))
            assertThat(awaitItem().isBlocked.isLoading()).isTrue()
            val errorState = awaitItem()
            assertThat(errorState.isBlocked.errorOrNull()).isEqualTo(AN_EXCEPTION)
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
    fun `present - start DM action failure scenario`() = runTest {
        val startDMFailureResult = AsyncAction.Failure(AN_EXCEPTION)
        val executeResult = lambdaRecorder<MatrixUser, Boolean, MutableState<AsyncAction<RoomId>>, Unit> { _, _, actionState ->
            actionState.value = startDMFailureResult
        }
        val startDMAction = FakeStartDMAction(executeResult = executeResult)
        val presenter = createUserProfilePresenter(startDMAction = startDMAction)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.startDmActionState).isInstanceOf(AsyncAction.Uninitialized::class.java)
            val matrixUser = MatrixUser(UserId("@alice:server.org"))
            initialState.eventSink(UserProfileEvents.StartDM)
            awaitItem().also { state ->
                assertThat(state.startDmActionState).isEqualTo(startDMFailureResult)
                executeResult.assertions().isCalledOnce().with(
                    value(matrixUser),
                    value(false),
                    any(),
                )
                state.eventSink(UserProfileEvents.ClearStartDMState)
            }
            awaitItem().also { state ->
                assertThat(state.startDmActionState.isUninitialized()).isTrue()
            }
        }
    }

    @Test
    fun `present - start DM action success scenario`() = runTest {
        val startDMSuccessResult = AsyncAction.Success(A_ROOM_ID)
        val executeResult = lambdaRecorder<MatrixUser, Boolean, MutableState<AsyncAction<RoomId>>, Unit> { _, _, actionState ->
            actionState.value = startDMSuccessResult
        }
        val startDMAction = FakeStartDMAction(executeResult = executeResult)
        val presenter = createUserProfilePresenter(startDMAction = startDMAction)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.startDmActionState).isInstanceOf(AsyncAction.Uninitialized::class.java)
            val matrixUser = MatrixUser(UserId("@alice:server.org"))
            initialState.eventSink(UserProfileEvents.StartDM)
            awaitItem().also { state ->
                assertThat(state.startDmActionState).isEqualTo(startDMSuccessResult)
                executeResult.assertions().isCalledOnce().with(
                    value(matrixUser),
                    value(false),
                    any(),
                )
            }
        }
    }

    @Test
    fun `present - start DM action confirmation scenario - cancel`() = runTest {
        val matrixUser = MatrixUser(UserId("@alice:server.org"))
        val startDMConfirmationResult = ConfirmingStartDmWithMatrixUser(matrixUser)
        val executeResult = lambdaRecorder<MatrixUser, Boolean, MutableState<AsyncAction<RoomId>>, Unit> { _, _, actionState ->
            actionState.value = startDMConfirmationResult
        }
        val startDMAction = FakeStartDMAction(executeResult = executeResult)
        val presenter = createUserProfilePresenter(startDMAction = startDMAction)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.startDmActionState).isInstanceOf(AsyncAction.Uninitialized::class.java)
            initialState.eventSink(UserProfileEvents.StartDM)
            val confirmingState = awaitItem()
            assertThat(confirmingState.startDmActionState).isEqualTo(startDMConfirmationResult)
            executeResult.assertions().isCalledOnce().with(
                value(matrixUser),
                value(false),
                any(),
            )
            // Cancelling should not create the DM
            confirmingState.eventSink(UserProfileEvents.ClearStartDMState)
            val finalState = awaitItem()
            assertThat(finalState.startDmActionState.isUninitialized()).isTrue()
            executeResult.assertions().isCalledExactly(1)
        }
    }

    @Test
    fun `present - start DM action confirmation scenario - confirm`() = runTest {
        val matrixUser = MatrixUser(UserId("@alice:server.org"))
        val startDMConfirmationResult = ConfirmingStartDmWithMatrixUser(matrixUser)
        val executeResult = lambdaRecorder<MatrixUser, Boolean, MutableState<AsyncAction<RoomId>>, Unit> { _, _, actionState ->
            actionState.value = startDMConfirmationResult
        }
        val startDMAction = FakeStartDMAction(executeResult = executeResult)
        val presenter = createUserProfilePresenter(startDMAction = startDMAction)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.startDmActionState).isInstanceOf(AsyncAction.Uninitialized::class.java)
            initialState.eventSink(UserProfileEvents.StartDM)
            val confirmingState = awaitItem()
            assertThat(confirmingState.startDmActionState).isEqualTo(startDMConfirmationResult)
            executeResult.assertions().isCalledOnce().with(
                value(matrixUser),
                value(false),
                any(),
            )
            // Start DM again should invoke the action with createIfDmDoesNotExist = true
            confirmingState.eventSink(UserProfileEvents.StartDM)
            executeResult.assertions().isCalledExactly(2).withSequence(
                listOf(value(matrixUser), value(false), any()),
                listOf(value(matrixUser), value(true), any()),
            )
        }
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitFirstItem(count: Int = 1): T {
        skipItems(count)
        return awaitItem()
    }

    private fun createFakeMatrixClient(
        userIdentityState: IdentityState? = null,
        ignoreUserResult: (UserId) -> Result<Unit> = { Result.success(Unit) },
        unIgnoreUserResult: (UserId) -> Result<Unit> = { Result.success(Unit) },
        ignoredUsersFlow: StateFlow<ImmutableList<UserId>> = MutableStateFlow(persistentListOf())
    ) = FakeMatrixClient(
        encryptionService = FakeEncryptionService(
            getUserIdentityResult = { Result.success(userIdentityState) }
        ),
        ignoreUserResult = ignoreUserResult,
        unIgnoreUserResult = unIgnoreUserResult,
        ignoredUsersFlow = ignoredUsersFlow,
    )

    private fun createUserProfilePresenter(
        client: MatrixClient = createFakeMatrixClient(),
        userId: UserId = UserId("@alice:server.org"),
        startDMAction: StartDMAction = FakeStartDMAction(),
        isElementCallAvailable: Boolean = true,
    ): UserProfilePresenter {
        return UserProfilePresenter(
            userId = userId,
            client = client,
            startDMAction = startDMAction,
            sessionEnterpriseService = FakeSessionEnterpriseService(
                isElementCallAvailableResult = { isElementCallAvailable },
            ),
        )
    }
}
