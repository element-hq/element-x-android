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

package io.element.android.features.invite.impl.response

import com.google.common.truth.Truth.assertThat
import io.element.android.features.invite.api.response.AcceptDeclineInviteEvents
import io.element.android.features.invite.api.response.InviteData
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.push.api.notifications.NotificationDrawerManager
import io.element.android.libraries.push.test.notifications.FakeNotificationDrawerManager
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.util.Optional

class AcceptDeclineInvitePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createAcceptDeclineInvitePresenter()
        presenter.test {
            awaitItem().also { state ->
                assertThat(state.acceptAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
                assertThat(state.declineAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
                assertThat(state.invite).isEqualTo(Optional.empty<InviteData>())
            }
        }
    }

    @Test
    fun `present - declining invite cancel flow`() = runTest {
        val presenter = createAcceptDeclineInvitePresenter()
        presenter.test {
            val inviteData = anInviteData()
            awaitItem().also { state ->
                state.eventSink(
                    AcceptDeclineInviteEvents.DeclineInvite(inviteData)
                )
            }
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.invite).isEqualTo(Optional.of(inviteData))
                assertThat(state.declineAction).isInstanceOf(AsyncAction.Confirming::class.java)
                state.eventSink(
                    InternalAcceptDeclineInviteEvents.CancelDeclineInvite
                )
            }
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.invite).isEqualTo(Optional.empty<InviteData>())
                assertThat(state.declineAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            }
        }
    }

    @Test
    fun `present - declining invite error flow`() = runTest {
        val declineInviteFailure = lambdaRecorder { ->
            Result.failure<Unit>(RuntimeException("Failed to leave room"))
        }
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(
                roomId = A_ROOM_ID,
                result = FakeMatrixRoom().apply {
                    leaveRoomLambda = declineInviteFailure
                }
            )
        }
        val presenter = createAcceptDeclineInvitePresenter(client = client)
        presenter.test {
            val inviteData = anInviteData()
            awaitItem().also { state ->
                state.eventSink(
                    AcceptDeclineInviteEvents.DeclineInvite(inviteData)
                )
            }
            skipItems(1)
            awaitItem().also { state ->
                state.eventSink(
                    InternalAcceptDeclineInviteEvents.ConfirmDeclineInvite
                )
            }
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.declineAction).isInstanceOf(AsyncAction.Failure::class.java)
                state.eventSink(
                    InternalAcceptDeclineInviteEvents.DismissDeclineError
                )
            }
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.invite).isEqualTo(Optional.empty<InviteData>())
                assertThat(state.declineAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            }
            cancelAndConsumeRemainingEvents()
        }
        assert(declineInviteFailure).isCalledOnce()
    }

    @Test
    fun `present - declining invite success flow`() = runTest {
        val declineInviteSuccess = lambdaRecorder { ->
            Result.success(Unit)
        }
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(
                roomId = A_ROOM_ID,
                result = FakeMatrixRoom().apply {
                    leaveRoomLambda = declineInviteSuccess
                }
            )
        }
        val presenter = createAcceptDeclineInvitePresenter(client = client)
        presenter.test {
            val inviteData = anInviteData()
            awaitItem().also { state ->
                state.eventSink(
                    AcceptDeclineInviteEvents.DeclineInvite(inviteData)
                )
            }
            skipItems(1)
            awaitItem().also { state ->
                state.eventSink(
                    InternalAcceptDeclineInviteEvents.ConfirmDeclineInvite
                )
            }
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.declineAction).isInstanceOf(AsyncAction.Success::class.java)
            }
            cancelAndConsumeRemainingEvents()
        }
        assert(declineInviteSuccess).isCalledOnce()
    }

    @Test
    fun `present - accepting invite error flow`() = runTest {
        val joinRoomFailure = lambdaRecorder { roomId: RoomId ->
            Result.failure<RoomId>(RuntimeException("Failed to join room $roomId"))
        }
        val client = FakeMatrixClient().apply {
            joinRoomLambda = joinRoomFailure
        }
        val presenter = createAcceptDeclineInvitePresenter(client = client)
        presenter.test {
            val inviteData = anInviteData()
            awaitItem().also { state ->
                state.eventSink(
                    AcceptDeclineInviteEvents.AcceptInvite(inviteData)
                )
            }
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.invite).isEqualTo(Optional.of(inviteData))
                assertThat(state.acceptAction).isInstanceOf(AsyncAction.Failure::class.java)
                state.eventSink(
                    InternalAcceptDeclineInviteEvents.DismissAcceptError
                )
            }
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.invite).isEqualTo(Optional.empty<InviteData>())
                assertThat(state.acceptAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            }
            cancelAndConsumeRemainingEvents()
        }
        assert(joinRoomFailure).isCalledOnce()
    }

    @Test
    fun `present - accepting invite success flow`() = runTest {
        val joinRoomSuccess = lambdaRecorder { roomId: RoomId ->
            Result.success(roomId)
        }
        val client = FakeMatrixClient().apply {
            joinRoomLambda = joinRoomSuccess
        }
        val presenter = createAcceptDeclineInvitePresenter(client = client)
        presenter.test {
            val inviteData = anInviteData()
            awaitItem().also { state ->
                state.eventSink(
                    AcceptDeclineInviteEvents.AcceptInvite(inviteData)
                )
            }
            skipItems(1)
            awaitItem().also { state ->
                assertThat(state.invite).isEqualTo(Optional.of(inviteData))
                assertThat(state.acceptAction).isInstanceOf(AsyncAction.Success::class.java)
            }
            cancelAndConsumeRemainingEvents()
        }
        assert(joinRoomSuccess).isCalledOnce()
    }

    private fun anInviteData(
        roomId: RoomId = A_ROOM_ID,
        name: String = A_ROOM_NAME,
        isDirect: Boolean = false
    ): InviteData {
        return InviteData(
            roomId = roomId,
            roomName = name,
            isDirect = isDirect
        )
    }

    private fun createAcceptDeclineInvitePresenter(
        client: MatrixClient = FakeMatrixClient(),
        analyticsService: AnalyticsService = FakeAnalyticsService(),
        notificationDrawerManager: NotificationDrawerManager = FakeNotificationDrawerManager(),
    ): AcceptDeclineInvitePresenter {
        return AcceptDeclineInvitePresenter(
            client = client,
            analyticsService = analyticsService,
            notificationDrawerManager = notificationDrawerManager,
        )
    }
}
