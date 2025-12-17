/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.startchat.impl.root

import androidx.compose.runtime.MutableState
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.invitepeople.test.FakeStartDMAction
import io.element.android.features.startchat.api.ConfirmingStartDmWithMatrixUser
import io.element.android.features.startchat.api.StartDMAction
import io.element.android.features.startchat.impl.userlist.FakeUserListPresenter
import io.element.android.features.startchat.impl.userlist.FakeUserListPresenterFactory
import io.element.android.features.startchat.impl.userlist.UserListDataStore
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.usersearch.test.FakeUserRepository
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class StartChatPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - start DM action failure scenario`() = runTest {
        val startDMFailureResult = AsyncAction.Failure(AN_EXCEPTION)
        val executeResult = lambdaRecorder<MatrixUser, Boolean, MutableState<AsyncAction<RoomId>>, Unit> { _, _, actionState ->
            actionState.value = startDMFailureResult
        }
        val startDMAction = FakeStartDMAction(executeResult = executeResult)
        val presenter = createStartChatPresenter(startDMAction)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.startDmAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            assertThat(initialState.applicationName).isEqualTo(aBuildMeta().applicationName)
            assertThat(initialState.userListState.selectedUsers).isEmpty()
            assertThat(initialState.userListState.isSearchActive).isFalse()
            assertThat(initialState.userListState.isMultiSelectionEnabled).isFalse()
            val matrixUser = MatrixUser(UserId("@name:domain"))
            initialState.eventSink(StartChatEvents.StartDM(matrixUser))
            awaitItem().also { state ->
                assertThat(state.startDmAction).isEqualTo(startDMFailureResult)
                executeResult.assertions().isCalledOnce().with(
                    value(matrixUser),
                    value(false),
                    any(),
                )
                state.eventSink(StartChatEvents.CancelStartDM)
            }
            awaitItem().also { state ->
                assertThat(state.startDmAction.isUninitialized()).isTrue()
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
        val presenter = createStartChatPresenter(startDMAction)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.startDmAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            assertThat(initialState.applicationName).isEqualTo(aBuildMeta().applicationName)
            assertThat(initialState.userListState.selectedUsers).isEmpty()
            assertThat(initialState.userListState.isSearchActive).isFalse()
            assertThat(initialState.userListState.isMultiSelectionEnabled).isFalse()
            val matrixUser = MatrixUser(UserId("@name:domain"))
            initialState.eventSink(StartChatEvents.StartDM(matrixUser))
            awaitItem().also { state ->
                assertThat(state.startDmAction).isEqualTo(startDMSuccessResult)
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
        val matrixUser = MatrixUser(UserId("@name:domain"))
        val startDMConfirmationResult = ConfirmingStartDmWithMatrixUser(matrixUser)
        val executeResult = lambdaRecorder<MatrixUser, Boolean, MutableState<AsyncAction<RoomId>>, Unit> { _, _, actionState ->
            actionState.value = startDMConfirmationResult
        }
        val startDMAction = FakeStartDMAction(executeResult = executeResult)
        val presenter = createStartChatPresenter(startDMAction)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.startDmAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            initialState.eventSink(StartChatEvents.StartDM(matrixUser))
            val confirmingState = awaitItem()
            assertThat(confirmingState.startDmAction).isEqualTo(startDMConfirmationResult)
            executeResult.assertions().isCalledOnce().with(
                value(matrixUser),
                value(false),
                any(),
            )
            // Cancelling should not create the DM
            confirmingState.eventSink(StartChatEvents.CancelStartDM)
            val finalState = awaitItem()
            assertThat(finalState.startDmAction.isUninitialized()).isTrue()
            executeResult.assertions().isCalledExactly(1)
        }
    }

    @Test
    fun `present - start DM action confirmation scenario - confirm`() = runTest {
        val matrixUser = MatrixUser(UserId("@name:domain"))
        val startDMConfirmationResult = ConfirmingStartDmWithMatrixUser(matrixUser)
        val executeResult = lambdaRecorder<MatrixUser, Boolean, MutableState<AsyncAction<RoomId>>, Unit> { _, _, actionState ->
            actionState.value = startDMConfirmationResult
        }
        val startDMAction = FakeStartDMAction(executeResult = executeResult)
        val presenter = createStartChatPresenter(startDMAction)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.startDmAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            initialState.eventSink(StartChatEvents.StartDM(matrixUser))
            val confirmingState = awaitItem()
            assertThat(confirmingState.startDmAction).isEqualTo(startDMConfirmationResult)
            executeResult.assertions().isCalledOnce().with(
                value(matrixUser),
                value(false),
                any(),
            )
            // Start DM again should invoke the action with createIfDmDoesNotExist = true
            confirmingState.eventSink(StartChatEvents.StartDM(matrixUser))
            executeResult.assertions().isCalledExactly(2).withSequence(
                listOf(value(matrixUser), value(false), any()),
                listOf(value(matrixUser), value(true), any()),
            )
        }
    }

    @Test
    fun `present - room directory search`() = runTest {
        val presenter = createStartChatPresenter(isRoomDirectorySearchEnabled = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            awaitItem().let { state ->
                assertThat(state.isRoomDirectorySearchEnabled).isTrue()
            }
        }
    }
}

internal fun createStartChatPresenter(
    startDMAction: StartDMAction = FakeStartDMAction(),
    isRoomDirectorySearchEnabled: Boolean = false,
): StartChatPresenter {
    val featureFlagService = FakeFeatureFlagService(
        initialState = mapOf(
            FeatureFlags.RoomDirectorySearch.key to isRoomDirectorySearchEnabled,
        ),
    )
    return StartChatPresenter(
        presenterFactory = FakeUserListPresenterFactory(FakeUserListPresenter()),
        userRepository = FakeUserRepository(),
        userListDataStore = UserListDataStore(),
        startDMAction = startDMAction,
        featureFlagService = featureFlagService,
        buildMeta = aBuildMeta(),
    )
}
