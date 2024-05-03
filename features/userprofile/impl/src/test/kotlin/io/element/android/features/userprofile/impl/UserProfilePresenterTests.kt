/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.userprofile.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.createroom.api.StartDMAction
import io.element.android.features.createroom.test.FakeStartDMAction
import io.element.android.features.userprofile.impl.root.UserProfilePresenter
import io.element.android.features.userprofile.shared.UserProfileEvents
import io.element.android.features.userprofile.shared.UserProfileState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class UserProfilePresenterTests {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - returns the user profile data`() = runTest {
        val matrixUser = aMatrixUser(A_USER_ID.value, "Alice", "anAvatarUrl")
        val client = FakeMatrixClient().apply {
            givenGetProfileResult(A_USER_ID, Result.success(matrixUser))
        }
        val presenter = createUserProfilePresenter(
            client = client,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.userId).isEqualTo(matrixUser.userId)
            assertThat(initialState.userName).isEqualTo(matrixUser.displayName)
            assertThat(initialState.avatarUrl).isEqualTo(matrixUser.avatarUrl)
            assertThat(initialState.isBlocked).isEqualTo(AsyncData.Success(false))
        }
    }

    @Test
    fun `present - returns empty data in case of failure`() = runTest {
        val client = FakeMatrixClient().apply {
            givenGetProfileResult(A_USER_ID, Result.failure(AN_EXCEPTION))
        }
        val presenter = createUserProfilePresenter(
            client = client,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
        val presenter = createUserProfilePresenter(
            client = client,
            userId = A_USER_ID
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
        val matrixClient = FakeMatrixClient()
        matrixClient.givenIgnoreUserResult(Result.failure(A_THROWABLE))
        val presenter = createUserProfilePresenter(client = matrixClient)
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
        val presenter = createUserProfilePresenter(client = matrixClient)
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
        val presenter = createUserProfilePresenter()
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
        val presenter = createUserProfilePresenter(startDMAction = startDMAction)
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

    private fun createUserProfilePresenter(
        client: MatrixClient = FakeMatrixClient(),
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
