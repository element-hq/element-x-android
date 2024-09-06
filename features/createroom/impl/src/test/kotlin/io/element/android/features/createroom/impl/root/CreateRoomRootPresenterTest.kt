/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.root

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.createroom.api.StartDMAction
import io.element.android.features.createroom.impl.userlist.FakeUserListPresenter
import io.element.android.features.createroom.impl.userlist.FakeUserListPresenterFactory
import io.element.android.features.createroom.impl.userlist.UserListDataStore
import io.element.android.features.createroom.test.FakeStartDMAction
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.usersearch.test.FakeUserRepository
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CreateRoomRootPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - start DM action complete scenario`() = runTest {
        val startDMAction = FakeStartDMAction()
        val presenter = createCreateRoomRootPresenter(startDMAction)
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
            val startDMSuccessResult = AsyncAction.Success(A_ROOM_ID)
            val startDMFailureResult = AsyncAction.Failure(A_THROWABLE)

            // Failure
            startDMAction.givenExecuteResult(startDMFailureResult)
            initialState.eventSink(CreateRoomRootEvents.StartDM(matrixUser))
            assertThat(awaitItem().startDmAction).isInstanceOf(AsyncAction.Loading::class.java)
            awaitItem().also { state ->
                assertThat(state.startDmAction).isEqualTo(startDMFailureResult)
                state.eventSink(CreateRoomRootEvents.CancelStartDM)
            }

            // Success
            startDMAction.givenExecuteResult(startDMSuccessResult)
            awaitItem().also { state ->
                assertThat(state.startDmAction).isEqualTo(AsyncAction.Uninitialized)
                state.eventSink(CreateRoomRootEvents.StartDM(matrixUser))
            }
            assertThat(awaitItem().startDmAction).isInstanceOf(AsyncAction.Loading::class.java)
            awaitItem().also { state ->
                assertThat(state.startDmAction).isEqualTo(startDMSuccessResult)
            }
        }
    }

    private fun createCreateRoomRootPresenter(
        startDMAction: StartDMAction = FakeStartDMAction(),
    ): CreateRoomRootPresenter {
        return CreateRoomRootPresenter(
            presenterFactory = FakeUserListPresenterFactory(FakeUserListPresenter()),
            userRepository = FakeUserRepository(),
            userListDataStore = UserListDataStore(),
            startDMAction = startDMAction,
            buildMeta = aBuildMeta(),
        )
    }
}
