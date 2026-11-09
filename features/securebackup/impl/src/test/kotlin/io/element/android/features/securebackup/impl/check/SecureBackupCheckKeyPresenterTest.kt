/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.check

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyUserStory
import io.element.android.features.securebackup.impl.tools.RecoveryKeyTools
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.awaitLastSequentialItem
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SecureBackupCheckKeyPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.recoveryKeyViewState.recoveryKeyUserStory).isEqualTo(RecoveryKeyUserStory.Enter)
            assertThat(initialState.recoveryKeyViewState.formattedRecoveryKey).isEmpty()
            assertThat(initialState.recoveryKeyViewState.displayTextFieldContents).isFalse()
            assertThat(initialState.recoveryKeyViewState.inProgress).isFalse()
            assertThat(initialState.isSubmitEnabled).isFalse()
            assertThat(initialState.isKeyIncorrect).isFalse()
            assertThat(initialState.isCompleted).isFalse()
        }
    }

    @Test
    fun `present - entering a key enables submitting it`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(SecureBackupCheckKeyEvent.OnRecoveryKeyChange("1234"))
            val state = awaitItem()
            assertThat(state.recoveryKeyViewState.formattedRecoveryKey).isEqualTo("1234")
            assertThat(state.isSubmitEnabled).isTrue()
        }
    }

    @Test
    fun `present - a pasted recovery key loses its spaces, but typing a key keeps them`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(SecureBackupCheckKeyEvent.OnRecoveryKeyChange(A_FORMATTED_RECOVERY_KEY))
            val pastedState = awaitItem()
            assertThat(pastedState.recoveryKeyViewState.formattedRecoveryKey).isEqualTo(A_FORMATTED_RECOVERY_KEY.replace(" ", ""))
            pastedState.eventSink(SecureBackupCheckKeyEvent.OnRecoveryKeyChange("1234 5678"))
            assertThat(awaitItem().recoveryKeyViewState.formattedRecoveryKey).isEqualTo("1234 5678")
        }
    }

    @Test
    fun `present - the visibility of the key can be changed`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(SecureBackupCheckKeyEvent.ChangeRecoveryKeyFieldContentsVisibility(true))
            assertThat(awaitItem().recoveryKeyViewState.displayTextFieldContents).isTrue()
        }
    }

    @Test
    fun `present - a correct key completes the check`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(SecureBackupCheckKeyEvent.OnRecoveryKeyChange("1234"))
            awaitItem().eventSink(SecureBackupCheckKeyEvent.Submit)
            val loadingState = awaitItem()
            assertThat(loadingState.recoveryKeyViewState.inProgress).isTrue()
            assertThat(loadingState.isSubmitEnabled).isFalse()
            val completedState = awaitItem()
            assertThat(completedState.isCompleted).isTrue()
            assertThat(completedState.isKeyIncorrect).isFalse()
        }
    }

    @Test
    fun `present - an incorrect key is reported until the user types again`() = runTest {
        val encryptionService = FakeEncryptionService().apply { givenRecoverFailure(AN_EXCEPTION) }
        val presenter = createPresenter(encryptionService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(SecureBackupCheckKeyEvent.OnRecoveryKeyChange("1234"))
            awaitItem().eventSink(SecureBackupCheckKeyEvent.Submit)
            assertThat(awaitItem().recoveryKeyViewState.inProgress).isTrue()
            val incorrectState = awaitItem()
            assertThat(incorrectState.isKeyIncorrect).isTrue()
            assertThat(incorrectState.isCompleted).isFalse()
            assertThat(incorrectState.isSubmitEnabled).isFalse()
            incorrectState.eventSink(SecureBackupCheckKeyEvent.OnRecoveryKeyChange("12345"))
            val retryState = awaitLastSequentialItem()
            assertThat(retryState.isKeyIncorrect).isFalse()
            assertThat(retryState.isSubmitEnabled).isTrue()
        }
    }

    private companion object {
        const val A_FORMATTED_RECOVERY_KEY = "EsTM njec uHYA yHmh dQdW Nj4o bNRU 9jMN XGMc KUNM UFr5 R8GY"
    }

    private fun createPresenter(
        encryptionService: EncryptionService = FakeEncryptionService(),
    ) = SecureBackupCheckKeyPresenter(
        encryptionService = encryptionService,
        recoveryKeyTools = RecoveryKeyTools(),
    )
}
