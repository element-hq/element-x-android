/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
