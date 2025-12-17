/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl.acceptdecline

import com.google.common.truth.Truth.assertThat
import io.element.android.features.invite.api.InviteData
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteEvents
import io.element.android.features.invite.api.acceptdecline.ConfirmingDeclineInvite
import io.element.android.features.invite.impl.AcceptInvite
import io.element.android.features.invite.impl.DeclineInvite
import io.element.android.features.invite.impl.fake.FakeAcceptInvite
import io.element.android.features.invite.impl.fake.FakeDeclineInvite
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
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
        val presenter = createAcceptDeclineInvitePresenter()
        presenter.test {
            val inviteData = anInviteData()
            awaitItem().also { state ->
                state.eventSink(
                    AcceptDeclineInviteEvents.DeclineInvite(inviteData, blockUser = false, shouldConfirm = true)
                )
            }
            awaitItem().also { state ->
                assertThat(state.declineAction).isEqualTo(ConfirmingDeclineInvite(inviteData, false))
                state.eventSink(
                    InternalAcceptDeclineInviteEvents.ClearDeclineActionState
                )
            }
            awaitItem().also { state ->
                assertThat(state.declineAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            }
        }
    }

    @Test
    fun `present - declining invite error flow`() = runTest {
        val declineInviteFailure = lambdaRecorder<RoomId, Boolean, Boolean, String?, Result<RoomId>> { _, _, _, _ ->
            Result.failure(DeclineInvite.Exception.DeclineInviteFailed)
        }
        val presenter = createAcceptDeclineInvitePresenter(
            declineInvite = FakeDeclineInvite(lambda = declineInviteFailure)
        )
        presenter.test {
            val inviteData = anInviteData()
            awaitItem().also { state ->
                state.eventSink(
                    AcceptDeclineInviteEvents.DeclineInvite(inviteData, blockUser = false, shouldConfirm = true)
                )
            }
            awaitItem().also { state ->
                assertThat(state.declineAction).isEqualTo(ConfirmingDeclineInvite(inviteData, false))
                state.eventSink(
                    AcceptDeclineInviteEvents.DeclineInvite(inviteData, blockUser = false, shouldConfirm = false)
                )
            }
            assertThat(awaitItem().declineAction.isLoading()).isTrue()
            awaitItem().also { state ->
                assertThat(state.declineAction).isInstanceOf(AsyncAction.Failure::class.java)
                state.eventSink(
                    InternalAcceptDeclineInviteEvents.ClearDeclineActionState
                )
            }
            awaitItem().also { state ->
                assertThat(state.declineAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            }
            cancelAndConsumeRemainingEvents()
        }
        assert(declineInviteFailure)
            .isCalledOnce()
            .with(value(A_ROOM_ID), value(false), value(false), value(null))
    }

    @Test
    fun `present - declining invite success flow`() = runTest {
        val declineInviteSuccess = lambdaRecorder<RoomId, Boolean, Boolean, String?, Result<RoomId>> { roomId, _, _, _ -> Result.success(roomId) }
        val presenter = createAcceptDeclineInvitePresenter(
            declineInvite = FakeDeclineInvite(lambda = declineInviteSuccess)
        )
        presenter.test {
            val inviteData = anInviteData()
            awaitItem().also { state ->
                state.eventSink(
                    AcceptDeclineInviteEvents.DeclineInvite(inviteData, blockUser = false, shouldConfirm = true)
                )
            }
            awaitItem().also { state ->
                assertThat(state.declineAction).isEqualTo(ConfirmingDeclineInvite(inviteData, blockUser = false))
                state.eventSink(
                    AcceptDeclineInviteEvents.DeclineInvite(inviteData, blockUser = false, shouldConfirm = false)
                )
            }
            assertThat(awaitItem().declineAction.isLoading()).isTrue()
            awaitItem().also { state ->
                assertThat(state.declineAction).isInstanceOf(AsyncAction.Success::class.java)
            }
            cancelAndConsumeRemainingEvents()
        }
        assert(declineInviteSuccess)
            .isCalledOnce()
            .with(value(A_ROOM_ID), value(false), value(false), value(null))
    }

    @Test
    fun `present - accepting invite error flow`() = runTest {
        val acceptInviteFailure = lambdaRecorder<RoomId, Result<RoomId>> { roomId: RoomId ->
            Result.failure(RuntimeException("Failed to accept invite"))
        }
        val presenter = createAcceptDeclineInvitePresenter(
            acceptInvite = FakeAcceptInvite(lambda = acceptInviteFailure),
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
                    InternalAcceptDeclineInviteEvents.ClearAcceptActionState
                )
            }
            awaitItem().also { state ->
                assertThat(state.acceptAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            }
            cancelAndConsumeRemainingEvents()
        }
        assert(acceptInviteFailure)
            .isCalledOnce()
            .with(value(A_ROOM_ID))
    }

    @Test
    fun `present - accepting invite success flow`() = runTest {
        val acceptInviteSuccess = lambdaRecorder<RoomId, Result<RoomId>> { roomId: RoomId -> Result.success(roomId) }
        val presenter = createAcceptDeclineInvitePresenter(
            acceptInvite = FakeAcceptInvite(lambda = acceptInviteSuccess)
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
        acceptInviteSuccess.assertions()
            .isCalledOnce()
            .with(value(A_ROOM_ID))
    }

    private fun anInviteData(
        roomId: RoomId = A_ROOM_ID,
        name: String = A_ROOM_NAME,
        isDm: Boolean = false,
    ): InviteData {
        return InviteData(
            roomId = roomId,
            roomName = name,
            isDm = isDm,
        )
    }

    private fun createAcceptDeclineInvitePresenter(
        acceptInvite: AcceptInvite = FakeAcceptInvite(),
        declineInvite: DeclineInvite = FakeDeclineInvite(),
    ): AcceptDeclineInvitePresenter {
        return AcceptDeclineInvitePresenter(
            acceptInvite = acceptInvite,
            declineInvite = declineInvite,
        )
    }
}
