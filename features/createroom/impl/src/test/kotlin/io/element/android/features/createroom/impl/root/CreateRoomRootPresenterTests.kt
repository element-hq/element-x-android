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

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.CreatedRoom
import io.element.android.features.analytics.test.FakeAnalyticsService
import io.element.android.features.createroom.impl.userlist.FakeUserListPresenter
import io.element.android.features.createroom.impl.userlist.FakeUserListPresenterFactory
import io.element.android.features.createroom.impl.userlist.UserListDataStore
import io.element.android.features.createroom.impl.userlist.aUserListState
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.usersearch.test.FakeUserRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CreateRoomRootPresenterTests {

    private lateinit var userRepository: FakeUserRepository
    private lateinit var presenter: CreateRoomRootPresenter
    private lateinit var fakeUserListPresenter: FakeUserListPresenter
    private lateinit var fakeMatrixClient: FakeMatrixClient
    private lateinit var fakeAnalyticsService: FakeAnalyticsService

    @Before
    fun setup() {
        fakeUserListPresenter = FakeUserListPresenter()
        fakeMatrixClient = FakeMatrixClient()
        fakeAnalyticsService = FakeAnalyticsService()
        userRepository = FakeUserRepository()
        presenter = CreateRoomRootPresenter(
            presenterFactory = FakeUserListPresenterFactory(fakeUserListPresenter),
            userRepository = userRepository,
            userListDataStore = UserListDataStore(),
            matrixClient = fakeMatrixClient,
            analyticsService = fakeAnalyticsService,
            buildMeta = aBuildMeta(),
        )
    }

    @Test
    fun `present - initial state`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.startDmAction).isInstanceOf(Async.Uninitialized::class.java)
            assertThat(initialState.applicationName).isEqualTo(aBuildMeta().applicationName)
            assertThat(initialState.userListState.selectedUsers).isEmpty()
            assertThat(initialState.userListState.isSearchActive).isFalse()
            assertThat(initialState.userListState.isMultiSelectionEnabled).isFalse()
        }
    }

    @Test
    fun `present - trigger create DM action`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val matrixUser = MatrixUser(UserId("@name:domain"))
            val createDmResult = Result.success(RoomId("!createDmResult:domain"))

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
    fun `present - creating a DM records analytics event`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val matrixUser = MatrixUser(UserId("@name:domain"))
            val createDmResult = Result.success(RoomId("!createDmResult:domain"))

            fakeMatrixClient.givenFindDmResult(null)
            fakeMatrixClient.givenCreateDmResult(createDmResult)

            initialState.eventSink(CreateRoomRootEvents.StartDM(matrixUser))
            skipItems(2)

            val analyticsEvent = fakeAnalyticsService.capturedEvents.filterIsInstance<CreatedRoom>().firstOrNull()
            assertThat(analyticsEvent).isNotNull()
            assertThat(analyticsEvent?.isDM).isTrue()
        }
    }

    @Test
    fun `present - trigger retrieve DM action`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val matrixUser = MatrixUser(UserId("@name:domain"))
            val fakeDmResult = FakeMatrixRoom(roomId = RoomId("!fakeDmResult:domain"))

            fakeMatrixClient.givenFindDmResult(fakeDmResult)

            initialState.eventSink(CreateRoomRootEvents.StartDM(matrixUser))
            val stateAfterStartDM = awaitItem()
            assertThat(stateAfterStartDM.startDmAction).isInstanceOf(Async.Success::class.java)
            assertThat(stateAfterStartDM.startDmAction.dataOrNull()).isEqualTo(fakeDmResult.roomId)
            assertThat(fakeAnalyticsService.capturedEvents.filterIsInstance<CreatedRoom>()).isEmpty()
        }
    }

    @Test
    fun `present - trigger retry create DM action`() = runTest {
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val matrixUser = MatrixUser(UserId("@name:domain"))
            val createDmResult = Result.success(RoomId("!createDmResult:domain"))
            fakeUserListPresenter.givenState(aUserListState().copy(selectedUsers = persistentListOf(matrixUser)))

            fakeMatrixClient.givenFindDmResult(null)
            fakeMatrixClient.givenCreateDmError(A_THROWABLE)
            fakeMatrixClient.givenCreateDmResult(createDmResult)

            // Failure
            initialState.eventSink(CreateRoomRootEvents.StartDM(matrixUser))
            assertThat(awaitItem().startDmAction).isInstanceOf(Async.Loading::class.java)
            val stateAfterStartDM = awaitItem()
            assertThat(stateAfterStartDM.startDmAction).isInstanceOf(Async.Failure::class.java)
            assertThat(fakeAnalyticsService.capturedEvents.filterIsInstance<CreatedRoom>()).isEmpty()

            // Cancel
            stateAfterStartDM.eventSink(CreateRoomRootEvents.CancelStartDM)
            val stateAfterCancel = awaitItem()
            assertThat(stateAfterCancel.startDmAction).isInstanceOf(Async.Uninitialized::class.java)

            // Failure
            stateAfterCancel.eventSink(CreateRoomRootEvents.StartDM(matrixUser))
            assertThat(awaitItem().startDmAction).isInstanceOf(Async.Loading::class.java)
            val stateAfterSecondAttempt = awaitItem()
            assertThat(stateAfterSecondAttempt.startDmAction).isInstanceOf(Async.Failure::class.java)
            assertThat(fakeAnalyticsService.capturedEvents.filterIsInstance<CreatedRoom>()).isEmpty()

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

private fun aBuildMeta() =
    BuildMeta(
        buildType = BuildType.DEBUG,
        isDebuggable = true,
        applicationId = "",
        applicationName = "An Application",
        lowPrivacyLoggingEnabled = true,
        versionName = "",
        gitRevision = "",
        gitBranchName = "",
        gitRevisionDate = "",
        flavorDescription = "",
        flavorShortDescription = "",
    )
