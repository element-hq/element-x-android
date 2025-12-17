/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.reset.password

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.encryption.FakeIdentityPasswordResetHandle
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ResetIdentityPasswordPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.resetAction.isUninitialized()).isTrue()
        }
    }

    @Test
    fun `present - Reset event succeeds`() = runTest {
        val resetLambda = lambdaRecorder<String, Result<Unit>> { _ -> Result.success(Unit) }
        val resetHandle = FakeIdentityPasswordResetHandle(resetPasswordLambda = resetLambda)
        val presenter = createPresenter(identityResetHandle = resetHandle)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ResetIdentityPasswordEvent.Reset("password"))
            assertThat(awaitItem().resetAction.isLoading()).isTrue()
            assertThat(awaitItem().resetAction.isSuccess()).isTrue()
        }
    }

    @Test
    fun `present - Reset event can fail gracefully`() = runTest {
        val resetLambda = lambdaRecorder<String, Result<Unit>> { _ -> Result.failure(IllegalStateException("Failed")) }
        val resetHandle = FakeIdentityPasswordResetHandle(resetPasswordLambda = resetLambda)
        val presenter = createPresenter(identityResetHandle = resetHandle)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ResetIdentityPasswordEvent.Reset("password"))
            assertThat(awaitItem().resetAction.isLoading()).isTrue()
            assertThat(awaitItem().resetAction.isFailure()).isTrue()
        }
    }

    @Test
    fun `present - DismissError event resets the state`() = runTest {
        val resetLambda = lambdaRecorder<String, Result<Unit>> { _ -> Result.failure(IllegalStateException("Failed")) }
        val resetHandle = FakeIdentityPasswordResetHandle(resetPasswordLambda = resetLambda)
        val presenter = createPresenter(identityResetHandle = resetHandle)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(ResetIdentityPasswordEvent.Reset("password"))
            assertThat(awaitItem().resetAction.isLoading()).isTrue()
            assertThat(awaitItem().resetAction.isFailure()).isTrue()

            initialState.eventSink(ResetIdentityPasswordEvent.DismissError)
            assertThat(awaitItem().resetAction.isUninitialized()).isTrue()
        }
    }

    private fun TestScope.createPresenter(
        identityResetHandle: FakeIdentityPasswordResetHandle = FakeIdentityPasswordResetHandle(),
    ) = ResetIdentityPasswordPresenter(
        identityPasswordResetHandle = identityResetHandle,
        dispatchers = testCoroutineDispatchers(),
    )
}
