/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.report

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ReportMessagePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `presenter - initial state`() = runTest {
        val presenter = createReportMessagePresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.reason).isEmpty()
            assertThat(initialState.blockUser).isFalse()
            assertThat(initialState.result).isInstanceOf(AsyncAction.Uninitialized::class.java)
        }
    }

    @Test
    fun `presenter - update reason`() = runTest {
        val presenter = createReportMessagePresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val reason = "This user is making the chat very toxic."
            initialState.eventSink(ReportMessageEvents.UpdateReason(reason))

            assertThat(awaitItem().reason).isEqualTo(reason)
        }
    }

    @Test
    fun `presenter - toggle block user`() = runTest {
        val presenter = createReportMessagePresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ReportMessageEvents.ToggleBlockUser)

            assertThat(awaitItem().blockUser).isTrue()

            initialState.eventSink(ReportMessageEvents.ToggleBlockUser)

            assertThat(awaitItem().blockUser).isFalse()
        }
    }

    @Test
    fun `presenter - handle successful report and block user`() = runTest {
        val reportContentResult = lambdaRecorder<EventId, String, UserId?, Result<Unit>> { _, _, _ ->
            Result.success(Unit)
        }
        val room = FakeJoinedRoom(
            reportContentResult = reportContentResult
        )
        val presenter = createReportMessagePresenter(joinedRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ReportMessageEvents.ToggleBlockUser)
            skipItems(1)
            initialState.eventSink(ReportMessageEvents.Report)
            assertThat(awaitItem().result).isInstanceOf(AsyncAction.Loading::class.java)
            assertThat(awaitItem().result).isInstanceOf(AsyncAction.Success::class.java)
            reportContentResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `presenter - handle successful report`() = runTest {
        val reportContentResult = lambdaRecorder<EventId, String, UserId?, Result<Unit>> { _, _, _ ->
            Result.success(Unit)
        }
        val room = FakeJoinedRoom(
            reportContentResult = reportContentResult
        )
        val presenter = createReportMessagePresenter(joinedRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ReportMessageEvents.Report)
            assertThat(awaitItem().result).isInstanceOf(AsyncAction.Loading::class.java)
            assertThat(awaitItem().result).isInstanceOf(AsyncAction.Success::class.java)
            reportContentResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `presenter - handle failed report`() = runTest {
        val reportContentResult = lambdaRecorder<EventId, String, UserId?, Result<Unit>> { _, _, _ ->
            Result.failure(Exception("Failed to report content"))
        }
        val room = FakeJoinedRoom(
            reportContentResult = reportContentResult
        )
        val presenter = createReportMessagePresenter(joinedRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ReportMessageEvents.Report)
            assertThat(awaitItem().result).isInstanceOf(AsyncAction.Loading::class.java)
            val resultState = awaitItem()
            assertThat(resultState.result).isInstanceOf(AsyncAction.Failure::class.java)
            reportContentResult.assertions().isCalledOnce()

            resultState.eventSink(ReportMessageEvents.ClearError)
            assertThat(awaitItem().result).isInstanceOf(AsyncAction.Uninitialized::class.java)
        }
    }

    private fun createReportMessagePresenter(
        inputs: ReportMessagePresenter.Inputs = ReportMessagePresenter.Inputs(AN_EVENT_ID, A_USER_ID),
        joinedRoom: JoinedRoom = FakeJoinedRoom(),
        snackbarDispatcher: SnackbarDispatcher = SnackbarDispatcher(),
    ) = ReportMessagePresenter(
        inputs = inputs,
        room = joinedRoom,
        snackbarDispatcher = snackbarDispatcher,
    )
}
