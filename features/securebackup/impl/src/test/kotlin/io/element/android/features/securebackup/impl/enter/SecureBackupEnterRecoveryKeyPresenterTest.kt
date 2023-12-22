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

package io.element.android.features.securebackup.impl.enter

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyUserStory
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyViewState
import io.element.android.features.securebackup.impl.tools.RecoveryKeyTools
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SecureBackupEnterRecoveryKeyPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.isSubmitEnabled).isFalse()
            assertThat(initialState.submitAction).isEqualTo(Async.Uninitialized)
            assertThat(initialState.recoveryKeyViewState).isEqualTo(
                RecoveryKeyViewState(
                    recoveryKeyUserStory = RecoveryKeyUserStory.Enter,
                    formattedRecoveryKey = "",
                    inProgress = false,
                )
            )
        }
    }

    @Test
    fun `present - enter recovery key`() = runTest {
        val encryptionService = FakeEncryptionService()
        val presenter = createPresenter(encryptionService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(SecureBackupEnterRecoveryKeyEvents.OnRecoveryKeyChange("1234"))
            val withRecoveryKeyState = awaitItem()
            assertThat(withRecoveryKeyState.isSubmitEnabled).isTrue()
            assertThat(withRecoveryKeyState.recoveryKeyViewState).isEqualTo(
                RecoveryKeyViewState(
                    recoveryKeyUserStory = RecoveryKeyUserStory.Enter,
                    formattedRecoveryKey = "1234",
                    inProgress = false,
                )
            )
            encryptionService.givenRecoverFailure(AN_EXCEPTION)
            withRecoveryKeyState.eventSink(SecureBackupEnterRecoveryKeyEvents.Submit)
            val loadingState = awaitItem()
            assertThat(loadingState.submitAction).isEqualTo(Async.Loading<Unit>())
            assertThat(loadingState.isSubmitEnabled).isFalse()
            val errorState = awaitItem()
            assertThat(errorState.submitAction).isEqualTo(Async.Failure<Unit>(AN_EXCEPTION))
            assertThat(errorState.isSubmitEnabled).isFalse()
            errorState.eventSink(SecureBackupEnterRecoveryKeyEvents.ClearDialog)
            val clearedState = awaitItem()
            assertThat(clearedState.submitAction).isEqualTo(Async.Uninitialized)
            assertThat(clearedState.isSubmitEnabled).isTrue()
            encryptionService.givenRecoverFailure(null)
            clearedState.eventSink(SecureBackupEnterRecoveryKeyEvents.Submit)
            val loadingState2 = awaitItem()
            assertThat(loadingState2.submitAction).isEqualTo(Async.Loading<Unit>())
            assertThat(loadingState2.isSubmitEnabled).isFalse()
            val finalState = awaitItem()
            assertThat(finalState.submitAction).isEqualTo(Async.Success(Unit))
            assertThat(finalState.isSubmitEnabled).isFalse()
        }
    }

    private fun createPresenter(
        encryptionService: EncryptionService = FakeEncryptionService(),
    ) = SecureBackupEnterRecoveryKeyPresenter(
        encryptionService = encryptionService,
        recoveryKeyTools = RecoveryKeyTools(),
    )
}
