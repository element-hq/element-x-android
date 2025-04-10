/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl.response

import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.features.invite.api.response.AcceptDeclineInviteEvents
import io.element.android.features.invite.api.response.ConfirmingDeclineInvite
import io.element.android.features.invite.api.response.InviteData
import io.element.android.features.invite.test.InMemorySeenInvitesStore
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID_3
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeRoomPreview
import io.element.android.libraries.matrix.test.room.join.FakeJoinRoom
import io.element.android.libraries.push.api.notifications.NotificationCleaner
import io.element.android.libraries.push.test.notifications.FakeNotificationCleaner
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

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
            }
        }
    }

    @Test
    fun `present - declining invite cancel flow`() = runTest {
        val seenInvitesStore = InMemorySeenInvitesStore(setOf(A_ROOM_ID, A_ROOM_ID_2, A_ROOM_ID_3))
        val presenter = createAcceptDeclineInvitePresenter(
            seenInvitesStore = seenInvitesStore,
        )
        presenter.test {
            val inviteData = anInviteData()
            awaitItem().also { state ->
                state.eventSink(
                    AcceptDeclineInviteEvents.DeclineInvite(inviteData)
                )
            }
            awaitItem().also { state ->
                assertThat(state.declineAction).isEqualTo(ConfirmingDeclineInvite(inviteData, false))
                state.eventSink(
                    InternalAcceptDeclineInviteEvents.CancelDeclineInvite
                )
            }
            awaitItem().also { state ->
                assertThat(state.declineAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            }
        }
        assertThat(seenInvitesStore.seenRoomIds().first()).containsExactly(A_ROOM_ID, A_ROOM_ID_2, A_ROOM_ID_3)
    }

    @Test
    fun `present - declining invite error flow`() = runTest {
        val declineInviteFailure = lambdaRecorder { ->
            Result.failure<Unit>(RuntimeException("Failed to leave room"))
        }
        val client = FakeMatrixClient(
            getRoomPreviewResult = { _, _ ->
                Result.success(FakeRoomPreview(declineInviteResult = declineInviteFailure))
            }
        )
        val seenInvitesStore = InMemorySeenInvitesStore(setOf(A_ROOM_ID, A_ROOM_ID_2, A_ROOM_ID_3))
        val presenter = createAcceptDeclineInvitePresenter(
            client = client,
            seenInvitesStore = seenInvitesStore,
        )
        presenter.test {
            val inviteData = anInviteData()
            awaitItem().also { state ->
                state.eventSink(
                    AcceptDeclineInviteEvents.DeclineInvite(inviteData)
                )
            }
            awaitItem().also { state ->
                assertThat(state.declineAction).isEqualTo(ConfirmingDeclineInvite(inviteData, false))
                state.eventSink(
                    InternalAcceptDeclineInviteEvents.ConfirmDeclineInvite
                )
            }
            assertThat(awaitItem().declineAction.isLoading()).isTrue()
            awaitItem().also { state ->
                assertThat(state.declineAction).isInstanceOf(AsyncAction.Failure::class.java)
                state.eventSink(
                    InternalAcceptDeclineInviteEvents.DismissDeclineError
                )
            }
            awaitItem().also { state ->
                assertThat(state.declineAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            }
            cancelAndConsumeRemainingEvents()
        }
        assert(declineInviteFailure).isCalledOnce()
        assertThat(seenInvitesStore.seenRoomIds().first()).containsExactly(A_ROOM_ID, A_ROOM_ID_2, A_ROOM_ID_3)
    }

    @Test
    fun `present - declining invite success flow`() = runTest {
        val clearMembershipNotificationForRoomLambda = lambdaRecorder<SessionId, RoomId, Unit> { _, _ ->
            Result.success(Unit)
        }
        val fakeNotificationCleaner = FakeNotificationCleaner(
            clearMembershipNotificationForRoomLambda = clearMembershipNotificationForRoomLambda
        )
        val declineInviteSuccess = lambdaRecorder { ->
            Result.success(Unit)
        }
        val client = FakeMatrixClient(
            getRoomPreviewResult = { _, _ ->
                Result.success(FakeRoomPreview(declineInviteResult = declineInviteSuccess))
            }
        )
        val seenInvitesStore = InMemorySeenInvitesStore(setOf(A_ROOM_ID, A_ROOM_ID_2, A_ROOM_ID_3))
        val presenter = createAcceptDeclineInvitePresenter(
            client = client,
            notificationCleaner = fakeNotificationCleaner,
            seenInvitesStore = seenInvitesStore,
        )
        presenter.test {
            val inviteData = anInviteData()
            awaitItem().also { state ->
                state.eventSink(
                    AcceptDeclineInviteEvents.DeclineInvite(inviteData)
                )
            }
            awaitItem().also { state ->
                assertThat(state.declineAction).isEqualTo(ConfirmingDeclineInvite(inviteData, false))
                state.eventSink(
                    InternalAcceptDeclineInviteEvents.ConfirmDeclineInvite
                )
            }
            assertThat(awaitItem().declineAction.isLoading()).isTrue()
            awaitItem().also { state ->
                assertThat(state.declineAction).isInstanceOf(AsyncAction.Success::class.java)
            }
            cancelAndConsumeRemainingEvents()
        }
        declineInviteSuccess.assertions().isCalledOnce()
        clearMembershipNotificationForRoomLambda.assertions()
            .isCalledOnce()
            .with(value(A_SESSION_ID), value(A_ROOM_ID))
        assertThat(seenInvitesStore.seenRoomIds().first()).containsExactly(A_ROOM_ID_2, A_ROOM_ID_3)
    }

    @Test
    fun `present - declining invite with block success flow`() = runTest {
        val clearMembershipNotificationForRoomLambda = lambdaRecorder<SessionId, RoomId, Unit> { _, _ ->
            Result.success(Unit)
        }
        val fakeNotificationCleaner = FakeNotificationCleaner(
            clearMembershipNotificationForRoomLambda = clearMembershipNotificationForRoomLambda
        )
        val declineInviteSuccess = lambdaRecorder { -> Result.success(Unit) }
        val ignoreUserSuccess = lambdaRecorder { _: UserId -> Result.success(Unit) }
        val client = FakeMatrixClient(
            getRoomPreviewResult = { _, _ ->
                Result.success(FakeRoomPreview(declineInviteResult = declineInviteSuccess))
            },
            ignoreUserResult = ignoreUserSuccess
        )
        val seenInvitesStore = InMemorySeenInvitesStore(setOf(A_ROOM_ID, A_ROOM_ID_2, A_ROOM_ID_3))
        val presenter = createAcceptDeclineInvitePresenter(
            client = client,
            notificationCleaner = fakeNotificationCleaner,
            seenInvitesStore = seenInvitesStore,
        )
        presenter.test {
            val inviteData = anInviteData()
            awaitItem().also { state ->
                state.eventSink(
                    AcceptDeclineInviteEvents.DeclineInvite(inviteData, blockUser = true)
                )
            }
            awaitItem().also { state ->
                assertThat(state.declineAction).isEqualTo(ConfirmingDeclineInvite(inviteData, true))
                state.eventSink(
                    InternalAcceptDeclineInviteEvents.ConfirmDeclineInvite
                )
            }
            assertThat(awaitItem().declineAction.isLoading()).isTrue()
            awaitItem().also { state ->
                assertThat(state.declineAction).isInstanceOf(AsyncAction.Success::class.java)
            }
            cancelAndConsumeRemainingEvents()
        }
        declineInviteSuccess.assertions().isCalledOnce()
        ignoreUserSuccess.assertions().isCalledOnce().with(value(A_USER_ID))
        clearMembershipNotificationForRoomLambda.assertions()
            .isCalledOnce()
            .with(value(A_SESSION_ID), value(A_ROOM_ID))
        assertThat(seenInvitesStore.seenRoomIds().first()).containsExactly(A_ROOM_ID_2, A_ROOM_ID_3)
    }

    @Test
    fun `present - declining invite with block error flow`() = runTest {
        val declineInviteFailure = lambdaRecorder { ->
            Result.failure<Unit>(RuntimeException("Failed to leave room"))
        }
        val client = FakeMatrixClient(
            getRoomPreviewResult = { _, _ ->
                Result.success(FakeRoomPreview(declineInviteResult = declineInviteFailure))
            }
        )
        val seenInvitesStore = InMemorySeenInvitesStore(setOf(A_ROOM_ID, A_ROOM_ID_2, A_ROOM_ID_3))
        val presenter = createAcceptDeclineInvitePresenter(
            client = client,
            seenInvitesStore = seenInvitesStore,
        )
        presenter.test {
            val inviteData = anInviteData()
            awaitItem().also { state ->
                state.eventSink(
                    AcceptDeclineInviteEvents.DeclineInvite(inviteData, blockUser = true)
                )
            }
            awaitItem().also { state ->
                assertThat(state.declineAction).isEqualTo(ConfirmingDeclineInvite(inviteData, true))
                state.eventSink(
                    InternalAcceptDeclineInviteEvents.ConfirmDeclineInvite
                )
            }
            assertThat(awaitItem().declineAction.isLoading()).isTrue()
        }
        assertThat(seenInvitesStore.seenRoomIds().first()).containsExactly(A_ROOM_ID, A_ROOM_ID_2, A_ROOM_ID_3)
    }

    @Test
    fun `present - accepting invite error flow`() = runTest {
        val joinRoomFailure = lambdaRecorder { roomIdOrAlias: RoomIdOrAlias, _: List<String>, _: JoinedRoom.Trigger ->
            Result.failure<Unit>(RuntimeException("Failed to join room $roomIdOrAlias"))
        }
        val seenInvitesStore = InMemorySeenInvitesStore(setOf(A_ROOM_ID, A_ROOM_ID_2, A_ROOM_ID_3))
        val presenter = createAcceptDeclineInvitePresenter(
            joinRoomLambda = joinRoomFailure,
            seenInvitesStore = seenInvitesStore,
        )
        presenter.test {
            val inviteData = anInviteData()
            awaitItem().also { state ->
                state.eventSink(
                    AcceptDeclineInviteEvents.AcceptInvite(inviteData)
                )
            }
            awaitItem().also { state ->
                assertThat(state.acceptAction).isEqualTo(AsyncAction.Loading)
            }
            awaitItem().also { state ->
                assertThat(state.acceptAction).isInstanceOf(AsyncAction.Failure::class.java)
                state.eventSink(
                    InternalAcceptDeclineInviteEvents.DismissAcceptError
                )
            }
            awaitItem().also { state ->
                assertThat(state.acceptAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            }
            cancelAndConsumeRemainingEvents()
        }
        assert(joinRoomFailure)
            .isCalledOnce()
            .with(
                value(A_ROOM_ID.toRoomIdOrAlias()),
                value(emptyList<String>()),
                value(JoinedRoom.Trigger.Invite)
            )
        assertThat(seenInvitesStore.seenRoomIds().first()).containsExactly(A_ROOM_ID, A_ROOM_ID_2, A_ROOM_ID_3)
    }

    @Test
    fun `present - accepting invite success flow`() = runTest {
        val clearMembershipNotificationForRoomLambda = lambdaRecorder<SessionId, RoomId, Unit> { _, _ ->
            Result.success(Unit)
        }
        val fakeNotificationCleaner = FakeNotificationCleaner(
            clearMembershipNotificationForRoomLambda = clearMembershipNotificationForRoomLambda
        )
        val joinRoomSuccess = lambdaRecorder { _: RoomIdOrAlias, _: List<String>, _: JoinedRoom.Trigger ->
            Result.success(Unit)
        }
        val seenInvitesStore = InMemorySeenInvitesStore(setOf(A_ROOM_ID, A_ROOM_ID_2, A_ROOM_ID_3))
        val presenter = createAcceptDeclineInvitePresenter(
            joinRoomLambda = joinRoomSuccess,
            notificationCleaner = fakeNotificationCleaner,
            seenInvitesStore = seenInvitesStore,
        )
        presenter.test {
            val inviteData = anInviteData()
            awaitItem().also { state ->
                state.eventSink(
                    AcceptDeclineInviteEvents.AcceptInvite(inviteData)
                )
            }
            awaitItem().also { state ->
                assertThat(state.acceptAction).isEqualTo(AsyncAction.Loading)
            }
            awaitItem().also { state ->
                assertThat(state.acceptAction).isInstanceOf(AsyncAction.Success::class.java)
            }
            cancelAndConsumeRemainingEvents()
        }
        assert(joinRoomSuccess)
            .isCalledOnce()
            .with(
                value(A_ROOM_ID.toRoomIdOrAlias()),
                value(emptyList<String>()),
                value(JoinedRoom.Trigger.Invite)
            )
        clearMembershipNotificationForRoomLambda.assertions()
            .isCalledOnce()
            .with(value(A_SESSION_ID), value(A_ROOM_ID))
        assertThat(seenInvitesStore.seenRoomIds().first()).containsExactly(A_ROOM_ID_2, A_ROOM_ID_3)
    }

    private fun anInviteData(
        roomId: RoomId = A_ROOM_ID,
        name: String = A_ROOM_NAME,
        isDm: Boolean = false,
        senderId: UserId = A_USER_ID,
    ): InviteData {
        return InviteData(
            roomId = roomId,
            roomName = name,
            isDm = isDm,
            senderId = senderId,
        )
    }

    private fun createAcceptDeclineInvitePresenter(
        client: MatrixClient = FakeMatrixClient(),
        joinRoomLambda: (RoomIdOrAlias, List<String>, JoinedRoom.Trigger) -> Result<Unit> = { _, _, _ ->
            Result.success(Unit)
        },
        notificationCleaner: NotificationCleaner = FakeNotificationCleaner(),
        seenInvitesStore: SeenInvitesStore = InMemorySeenInvitesStore(),
    ): AcceptDeclineInvitePresenter {
        return AcceptDeclineInvitePresenter(
            client = client,
            joinRoom = FakeJoinRoom(joinRoomLambda),
            notificationCleaner = notificationCleaner,
            seenInvitesStore = seenInvitesStore,
        )
    }
}
