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

package io.element.android.features.messages.impl.report

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ReportMessagePresenterTests {

    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `presenter - initial state`() = runTest {
        val presenter = aPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.reason).isEmpty()
            assertThat(initialState.blockUser).isFalse()
            assertThat(initialState.result).isInstanceOf(AsyncData.Uninitialized::class.java)
        }
    }

    @Test
    fun `presenter - update reason`() = runTest {
        val presenter = aPresenter()
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
        val presenter = aPresenter()
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
        val room = FakeMatrixRoom()
        val presenter = aPresenter(matrixRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ReportMessageEvents.ToggleBlockUser)
            skipItems(1)
            initialState.eventSink(ReportMessageEvents.Report)
            assertThat(awaitItem().result).isInstanceOf(AsyncData.Loading::class.java)
            assertThat(awaitItem().result).isInstanceOf(AsyncData.Success::class.java)
            assertThat(room.reportedContentCount).isEqualTo(1)
        }
    }

    @Test
    fun `presenter - handle successful report`() = runTest {
        val room = FakeMatrixRoom()
        val presenter = aPresenter(matrixRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ReportMessageEvents.Report)
            assertThat(awaitItem().result).isInstanceOf(AsyncData.Loading::class.java)
            assertThat(awaitItem().result).isInstanceOf(AsyncData.Success::class.java)
            assertThat(room.reportedContentCount).isEqualTo(1)
        }
    }

    @Test
    fun `presenter - handle failed report`() = runTest {
        val room = FakeMatrixRoom().apply {
            givenReportContentResult(Result.failure(Exception("Failed to report content")))
        }
        val presenter = aPresenter(matrixRoom = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ReportMessageEvents.Report)
            assertThat(awaitItem().result).isInstanceOf(AsyncData.Loading::class.java)
            val resultState = awaitItem()
            assertThat(resultState.result).isInstanceOf(AsyncData.Failure::class.java)
            assertThat(room.reportedContentCount).isEqualTo(1)

            resultState.eventSink(ReportMessageEvents.ClearError)
            assertThat(awaitItem().result).isInstanceOf(AsyncData.Uninitialized::class.java)
        }
    }

    private fun aPresenter(
        inputs: ReportMessagePresenter.Inputs = ReportMessagePresenter.Inputs(AN_EVENT_ID, A_USER_ID),
        matrixRoom: MatrixRoom = FakeMatrixRoom(),
        snackbarDispatcher: SnackbarDispatcher = SnackbarDispatcher(),
    ) = ReportMessagePresenter(
        inputs = inputs,
        room = matrixRoom,
        snackbarDispatcher = snackbarDispatcher,
    )
}
