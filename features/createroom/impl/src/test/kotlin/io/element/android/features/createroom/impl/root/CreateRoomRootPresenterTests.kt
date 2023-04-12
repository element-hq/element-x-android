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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.createroom.impl.root

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.userlist.api.aUserListState
import io.element.android.features.userlist.test.FakeUserListDataSource
import io.element.android.features.userlist.test.FakeUserListPresenter
import io.element.android.features.userlist.test.FakeUserListPresenterFactory
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.ui.model.MatrixUser
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CreateRoomRootPresenterTests {

    private lateinit var userListDataSource: FakeUserListDataSource
    private lateinit var presenter: CreateRoomRootPresenter
    private lateinit var fakeUserListPresenter: FakeUserListPresenter
    private lateinit var fakeMatrixClient: FakeMatrixClient

    @Before
    fun setup() {
        fakeUserListPresenter = FakeUserListPresenter()
        fakeMatrixClient = FakeMatrixClient()
        userListDataSource = FakeUserListDataSource()
        presenter = CreateRoomRootPresenter(FakeUserListPresenterFactory(fakeUserListPresenter), userListDataSource, fakeMatrixClient)
    }

    @Test
    fun `present - initial state`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState)
        }
    }

    @Test
    fun `present - trigger action buttons`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(CreateRoomRootEvents.InvitePeople) // Not implemented yet
        }
    }

    @Test
    fun `present - trigger create DM action`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val matrixUser = MatrixUser(UserId("@name:matrix.org"))
            val createDmResult = Result.success(RoomId("!createDmResult"))

            fakeMatrixClient.givenFindDmResult(null)
            fakeMatrixClient.givenCreateDmResult(createDmResult)

            initialState.eventSink(CreateRoomRootEvents.StartDM(matrixUser))
            assertThat(awaitItem().startDmAction).isInstanceOf(Async.Loading::class.java)
            val stateAfterStartDM = awaitItem()
            assertThat(stateAfterStartDM.startDmAction).isInstanceOf(Async.Success::class.java)
            assertThat(stateAfterStartDM.startDmAction.dataOrNull()).isEqualTo(createDmResult.getOrNull())
        }
    }

    @Test
    fun `present - trigger retrieve DM action`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val matrixUser = MatrixUser(UserId("@name:matrix.org"))
            val fakeDmResult = FakeMatrixRoom(RoomId("!fakeDmResult"))

            fakeMatrixClient.givenFindDmResult(fakeDmResult)

            initialState.eventSink(CreateRoomRootEvents.StartDM(matrixUser))
            val stateAfterStartDM = awaitItem()
            assertThat(stateAfterStartDM.startDmAction).isInstanceOf(Async.Success::class.java)
            assertThat(stateAfterStartDM.startDmAction.dataOrNull()).isEqualTo(fakeDmResult.roomId)
        }
    }

    @Test
    fun `present - trigger retry create DM action`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val matrixUser = MatrixUser(UserId("@name:matrix.org"))
            val createDmResult = Result.success(RoomId("!createDmResult"))
            fakeUserListPresenter.givenState(aUserListState().copy(selectedUsers = persistentListOf(matrixUser)))

            fakeMatrixClient.givenFindDmResult(null)
            fakeMatrixClient.givenCreateDmError(A_THROWABLE)
            fakeMatrixClient.givenCreateDmResult(createDmResult)

            // Failure
            initialState.eventSink(CreateRoomRootEvents.StartDM(matrixUser))
            assertThat(awaitItem().startDmAction).isInstanceOf(Async.Loading::class.java)
            val stateAfterStartDM = awaitItem()
            assertThat(stateAfterStartDM.startDmAction).isInstanceOf(Async.Failure::class.java)

            // Cancel
            stateAfterStartDM.eventSink(CreateRoomRootEvents.CancelStartDM)
            val stateAfterCancel = awaitItem()
            assertThat(stateAfterCancel.startDmAction).isInstanceOf(Async.Uninitialized::class.java)

            // Failure
            stateAfterCancel.eventSink(CreateRoomRootEvents.StartDM(matrixUser))
            assertThat(awaitItem().startDmAction).isInstanceOf(Async.Loading::class.java)
            val stateAfterSecondAttempt = awaitItem()
            assertThat(stateAfterSecondAttempt.startDmAction).isInstanceOf(Async.Failure::class.java)

            // Retry with success
            fakeMatrixClient.givenCreateDmError(null)
            stateAfterSecondAttempt.eventSink(CreateRoomRootEvents.StartDM(matrixUser))
            assertThat(awaitItem().startDmAction).isInstanceOf(Async.Uninitialized::class.java)
            assertThat(awaitItem().startDmAction).isInstanceOf(Async.Loading::class.java)
            val stateAfterRetryStartDM = awaitItem()
            assertThat(stateAfterRetryStartDM.startDmAction).isInstanceOf(Async.Success::class.java)
            assertThat(stateAfterRetryStartDM.startDmAction.dataOrNull()).isEqualTo(createDmResult.getOrNull())
        }
    }
}
