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
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.core.RoomId
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

class CreateRoomRootPresenterTests {

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

            assertThat(initialState.startDmAction).isInstanceOf(Async.Uninitialized::class.java)
            assertThat(initialState.applicationName).isEqualTo(aBuildMeta().applicationName)
            assertThat(initialState.userListState.selectedUsers).isEmpty()
            assertThat(initialState.userListState.isSearchActive).isFalse()
            assertThat(initialState.userListState.isMultiSelectionEnabled).isFalse()

            val matrixUser = MatrixUser(UserId("@name:domain"))
            val startDMSuccessResult = Async.Success(A_ROOM_ID)
            val startDMFailureResult = Async.Failure<RoomId>(A_THROWABLE)

            // Failure
            startDMAction.givenExecuteResult(startDMFailureResult)
            initialState.eventSink(CreateRoomRootEvents.StartDM(matrixUser))
            assertThat(awaitItem().startDmAction).isInstanceOf(Async.Loading::class.java)
            awaitItem().also { state ->
                assertThat(state.startDmAction).isEqualTo(startDMFailureResult)
                state.eventSink(CreateRoomRootEvents.CancelStartDM)
            }

            // Success
            startDMAction.givenExecuteResult(startDMSuccessResult)
            awaitItem().also { state ->
                assertThat(state.startDmAction).isEqualTo(Async.Uninitialized)
                state.eventSink(CreateRoomRootEvents.StartDM(matrixUser))
            }
            assertThat(awaitItem().startDmAction).isInstanceOf(Async.Loading::class.java)
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
