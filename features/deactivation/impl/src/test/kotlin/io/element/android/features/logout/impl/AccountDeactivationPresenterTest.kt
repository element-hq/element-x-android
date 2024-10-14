/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.logout.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class AccountDeactivationPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.accountDeactivationAction).isEqualTo(AsyncAction.Uninitialized)
            assertThat(initialState.deactivateFormState).isEqualTo(DeactivateFormState.Default)
        }
    }

    @Test
    fun `present - form update`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.deactivateFormState).isEqualTo(DeactivateFormState.Default)
            initialState.eventSink(AccountDeactivationEvents.SetEraseData(true))
            val updatedState = awaitItem()
            assertThat(updatedState.deactivateFormState).isEqualTo(DeactivateFormState.Default.copy(eraseData = true))
            assertThat(updatedState.submitEnabled).isFalse()
            updatedState.eventSink(AccountDeactivationEvents.SetPassword("password"))
            val updatedState2 = awaitItem()
            assertThat(updatedState2.deactivateFormState).isEqualTo(DeactivateFormState(password = "password", eraseData = true))
            assertThat(updatedState2.submitEnabled).isTrue()
        }
    }

    @Test
    fun `present - submit`() = runTest {
        val recorder = lambdaRecorder<String, Boolean, Result<Unit>> { _, _ ->
            Result.success(Unit)
        }
        val matrixClient = FakeMatrixClient(
            deactivateAccountResult = recorder
        )
        val presenter = createPresenter(matrixClient)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(AccountDeactivationEvents.SetPassword("password"))
            skipItems(1)
            initialState.eventSink(AccountDeactivationEvents.DeactivateAccount(isRetry = false))
            val updatedState = awaitItem()
            assertThat(updatedState.accountDeactivationAction).isEqualTo(AsyncAction.ConfirmingNoParams)
            updatedState.eventSink(AccountDeactivationEvents.DeactivateAccount(isRetry = false))
            val updatedState2 = awaitItem()
            assertThat(updatedState2.accountDeactivationAction).isEqualTo(AsyncAction.Loading)
            val finalState = awaitItem()
            assertThat(finalState.accountDeactivationAction).isEqualTo(AsyncAction.Success(Unit))
            recorder.assertions().isCalledOnce().with(value("password"), value(false))
        }
    }

    @Test
    fun `present - submit with error and retry`() = runTest {
        val recorder = lambdaRecorder<String, Boolean, Result<Unit>> { _, _ ->
            Result.failure(AN_EXCEPTION)
        }
        val matrixClient = FakeMatrixClient(
            deactivateAccountResult = recorder
        )
        val presenter = createPresenter(matrixClient)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(AccountDeactivationEvents.SetPassword("password"))
            initialState.eventSink(AccountDeactivationEvents.SetEraseData(true))
            skipItems(2)
            initialState.eventSink(AccountDeactivationEvents.DeactivateAccount(isRetry = false))
            val updatedState = awaitItem()
            assertThat(updatedState.accountDeactivationAction).isEqualTo(AsyncAction.ConfirmingNoParams)
            updatedState.eventSink(AccountDeactivationEvents.DeactivateAccount(isRetry = false))
            val updatedState2 = awaitItem()
            assertThat(updatedState2.accountDeactivationAction).isEqualTo(AsyncAction.Loading)
            val finalState = awaitItem()
            assertThat(finalState.accountDeactivationAction).isEqualTo(AsyncAction.Failure(AN_EXCEPTION))
            recorder.assertions().isCalledOnce().with(value("password"), value(true))
            // Retry
            finalState.eventSink(AccountDeactivationEvents.DeactivateAccount(isRetry = true))
            val finalState2 = awaitItem()
            assertThat(finalState2.accountDeactivationAction).isEqualTo(AsyncAction.Loading)
            assertThat(awaitItem().accountDeactivationAction).isEqualTo(AsyncAction.Failure(AN_EXCEPTION))
        }
    }

    @Test
    fun `present - submit with error and cancel`() = runTest {
        val recorder = lambdaRecorder<String, Boolean, Result<Unit>> { _, _ ->
            Result.failure(AN_EXCEPTION)
        }
        val matrixClient = FakeMatrixClient(
            deactivateAccountResult = recorder
        )
        val presenter = createPresenter(matrixClient)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(AccountDeactivationEvents.SetPassword("password"))
            initialState.eventSink(AccountDeactivationEvents.SetEraseData(true))
            skipItems(2)
            initialState.eventSink(AccountDeactivationEvents.DeactivateAccount(isRetry = false))
            val updatedState = awaitItem()
            assertThat(updatedState.accountDeactivationAction).isEqualTo(AsyncAction.ConfirmingNoParams)
            updatedState.eventSink(AccountDeactivationEvents.DeactivateAccount(isRetry = false))
            val updatedState2 = awaitItem()
            assertThat(updatedState2.accountDeactivationAction).isEqualTo(AsyncAction.Loading)
            val finalState = awaitItem()
            assertThat(finalState.accountDeactivationAction).isEqualTo(AsyncAction.Failure(AN_EXCEPTION))
            recorder.assertions().isCalledOnce().with(value("password"), value(true))
            // Cancel
            finalState.eventSink(AccountDeactivationEvents.CloseDialogs)
            val finalState2 = awaitItem()
            assertThat(finalState2.accountDeactivationAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    private fun createPresenter(
        matrixClient: MatrixClient = FakeMatrixClient(),
    ) = AccountDeactivationPresenter(
        matrixClient = matrixClient,
    )
}
