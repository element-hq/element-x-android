/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl.declineandblock

import com.google.common.truth.Truth.assertThat
import io.element.android.features.invite.api.InviteData
import io.element.android.features.invite.impl.DeclineInvite
import io.element.android.features.invite.impl.fake.FakeDeclineInvite
import io.element.android.features.invite.test.anInviteData
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.assert
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DeclineAndBlockPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createDeclineAndBlockPresenter()
        presenter.test {
            awaitItem().also { state ->
                assertThat(state.blockUser).isTrue()
                assertThat(state.reportRoom).isFalse()
                assertThat(state.reportReason).isEmpty()
                assertThat(state.declineAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
                assertThat(state.canDecline).isTrue()
            }
        }
    }

    @Test
    fun `present - update form values`() = runTest {
        val presenter = createDeclineAndBlockPresenter()
        presenter.test {
            awaitItem().also { state ->
                assertThat(state.reportRoom).isFalse()
                assertThat(state.blockUser).isTrue()
                assertThat(state.reportReason).isEmpty()
                assertThat(state.canDecline).isTrue()
                state.eventSink(DeclineAndBlockEvents.ToggleBlockUser)
            }
            awaitItem().also { state ->
                assertThat(state.reportRoom).isFalse()
                assertThat(state.blockUser).isFalse()
                assertThat(state.reportReason).isEmpty()
                assertThat(state.canDecline).isFalse()
                state.eventSink(DeclineAndBlockEvents.ToggleReportRoom)
            }
            awaitItem().also { state ->
                assertThat(state.reportRoom).isTrue()
                assertThat(state.blockUser).isFalse()
                assertThat(state.reportReason).isEmpty()
                assertThat(state.canDecline).isFalse()
                state.eventSink(DeclineAndBlockEvents.UpdateReportReason("Spam"))
            }
            awaitItem().also { state ->
                assertThat(state.reportRoom).isTrue()
                assertThat(state.blockUser).isFalse()
                assertThat(state.reportReason).isEqualTo("Spam")
                assertThat(state.canDecline).isTrue()
            }
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `present - declining invite success flow`() = runTest {
        val declineInviteSuccess = lambdaRecorder<RoomId, Boolean, Boolean, String?, Result<RoomId>> { roomId, _, _, _ -> Result.success(roomId) }
        val presenter = createDeclineAndBlockPresenter(
            declineInvite = FakeDeclineInvite(lambda = declineInviteSuccess)
        )
        presenter.test {
            awaitItem().also { state ->
                state.eventSink(DeclineAndBlockEvents.Decline)
            }
            assertThat(awaitItem().declineAction.isLoading()).isTrue()
            awaitItem().also { state ->
                assertThat(state.declineAction).isInstanceOf(AsyncAction.Success::class.java)
            }
            cancelAndConsumeRemainingEvents()
        }
        assert(declineInviteSuccess)
            .isCalledOnce()
            .with(value(A_ROOM_ID), value(true), value(false), value(""))
    }

    @Test
    fun `present - declining invite error flow`() = runTest {
        val declineInviteFailure = lambdaRecorder<RoomId, Boolean, Boolean, String?, Result<RoomId>> { _, _, _, _ ->
            Result.failure(DeclineInvite.Exception.DeclineInviteFailed)
        }
        val presenter = createDeclineAndBlockPresenter(
            declineInvite = FakeDeclineInvite(lambda = declineInviteFailure)
        )
        presenter.test {
            awaitItem().also { state ->
                state.eventSink(DeclineAndBlockEvents.Decline)
            }
            assertThat(awaitItem().declineAction.isLoading()).isTrue()
            awaitItem().also { state ->
                assertThat(state.declineAction).isInstanceOf(AsyncAction.Failure::class.java)
                state.eventSink(DeclineAndBlockEvents.ClearDeclineAction)
            }
            awaitItem().also { state ->
                assertThat(state.declineAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            }
            cancelAndConsumeRemainingEvents()
        }
        assert(declineInviteFailure)
            .isCalledOnce()
            .with(value(A_ROOM_ID), value(true), value(false), value(""))
    }

    @Test
    fun `present - block user error flow`() = runTest {
        val declineInviteFailure = lambdaRecorder<RoomId, Boolean, Boolean, String?, Result<RoomId>> { _, _, _, _ ->
            Result.failure(DeclineInvite.Exception.BlockUserFailed)
        }
        val presenter = createDeclineAndBlockPresenter(
            declineInvite = FakeDeclineInvite(lambda = declineInviteFailure)
        )
        presenter.test {
            awaitItem().also { state ->
                state.eventSink(DeclineAndBlockEvents.Decline)
            }
            assertThat(awaitItem().declineAction.isLoading()).isTrue()
            awaitItem().also { state ->
                assertThat(state.declineAction).isInstanceOf(AsyncAction.Uninitialized::class.java)
            }
            cancelAndConsumeRemainingEvents()
        }
        assert(declineInviteFailure)
            .isCalledOnce()
            .with(value(A_ROOM_ID), value(true), value(false), value(""))
    }
}

internal fun createDeclineAndBlockPresenter(
    inviteData: InviteData = anInviteData(),
    declineInvite: DeclineInvite = FakeDeclineInvite(),
    snackbarDispatcher: SnackbarDispatcher = SnackbarDispatcher(),
): DeclineAndBlockPresenter {
    return DeclineAndBlockPresenter(
        inviteData = inviteData,
        declineInvite = declineInvite,
        snackbarDispatcher = snackbarDispatcher,
    )
}
