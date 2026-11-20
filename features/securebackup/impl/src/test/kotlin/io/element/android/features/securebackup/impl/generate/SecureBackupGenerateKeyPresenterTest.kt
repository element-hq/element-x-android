/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.generate

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.androidutils.clipboard.FakeClipboardHelper
import io.element.android.libraries.androidutils.toast.FakeToastHelper
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_RECOVERY_KEY
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SecureBackupGenerateKeyPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - the key is generated up front but not activated`() = runTest {
        val activateRecoveryKeyLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val presenter = createPresenter(
            encryptionService = FakeEncryptionService(
                generateRecoveryKeyLambda = { A_RECOVERY_KEY },
                activateRecoveryKeyLambda = activateRecoveryKeyLambda,
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.recoveryKey).isEqualTo(A_RECOVERY_KEY)
            assertThat(initialState.isReplacingExistingKey).isTrue()
            assertThat(initialState.showSaveConfirmationDialog).isFalse()
            assertThat(initialState.activateAction).isEqualTo(AsyncAction.Uninitialized)
            assertThat(initialState.isCompleted).isFalse()
            activateRecoveryKeyLambda.assertions().isNeverCalled()
        }
    }

    @Test
    fun `present - the first key is not replacing an existing one`() = runTest {
        val presenter = createPresenter(isReplacingExistingKey = false)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().isReplacingExistingKey).isFalse()
        }
    }

    @Test
    fun `present - continuing asks to confirm that the key is saved, and it can be dismissed`() = runTest {
        val activateRecoveryKeyLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val presenter = createPresenter(
            encryptionService = FakeEncryptionService(activateRecoveryKeyLambda = activateRecoveryKeyLambda),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(SecureBackupGenerateKeyEvent.Continue)
            val confirmingState = awaitItem()
            assertThat(confirmingState.showSaveConfirmationDialog).isTrue()
            confirmingState.eventSink(SecureBackupGenerateKeyEvent.DismissConfirmation)
            assertThat(awaitItem().showSaveConfirmationDialog).isFalse()
            activateRecoveryKeyLambda.assertions().isNeverCalled()
        }
    }

    @Test
    fun `present - confirming that the key is saved activates it`() = runTest {
        val activateRecoveryKeyLambda = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val presenter = createPresenter(
            encryptionService = FakeEncryptionService(activateRecoveryKeyLambda = activateRecoveryKeyLambda),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(SecureBackupGenerateKeyEvent.Continue)
            val confirmingState = awaitItem()
            confirmingState.eventSink(SecureBackupGenerateKeyEvent.ConfirmSaved)
            assertThat(awaitItem().showSaveConfirmationDialog).isFalse()
            assertThat(awaitItem().activateAction).isEqualTo(AsyncAction.Loading)
            val completedState = awaitItem()
            assertThat(completedState.activateAction).isEqualTo(AsyncAction.Success(Unit))
            assertThat(completedState.isCompleted).isTrue()
            activateRecoveryKeyLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - a failure to activate the key can be dismissed and retried`() = runTest {
        var result: Result<Unit> = Result.failure(AN_EXCEPTION)
        val presenter = createPresenter(
            encryptionService = FakeEncryptionService(activateRecoveryKeyLambda = { result }),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(SecureBackupGenerateKeyEvent.ConfirmSaved)
            assertThat(awaitItem().activateAction).isEqualTo(AsyncAction.Loading)
            val failureState = awaitItem()
            assertThat(failureState.activateAction).isEqualTo(AsyncAction.Failure(AN_EXCEPTION))
            assertThat(failureState.isCompleted).isFalse()
            failureState.eventSink(SecureBackupGenerateKeyEvent.DismissError)
            val dismissedState = awaitItem()
            assertThat(dismissedState.activateAction).isEqualTo(AsyncAction.Uninitialized)
            // The same key is still shown, and can be activated this time.
            assertThat(dismissedState.recoveryKey).isEqualTo(initialState.recoveryKey)
            result = Result.success(Unit)
            dismissedState.eventSink(SecureBackupGenerateKeyEvent.ConfirmSaved)
            assertThat(awaitItem().activateAction).isEqualTo(AsyncAction.Loading)
            assertThat(awaitItem().isCompleted).isTrue()
        }
    }

    @Test
    fun `present - copying the key puts it on the clipboard as a sensitive text`() = runTest {
        val clipboardHelper = FakeClipboardHelper()
        val presenter = createPresenter(
            encryptionService = FakeEncryptionService(generateRecoveryKeyLambda = { A_RECOVERY_KEY }),
            clipboardHelper = clipboardHelper,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(SecureBackupGenerateKeyEvent.CopyRecoveryKey)
            assertThat(clipboardHelper.clipboardContents).isEqualTo(A_RECOVERY_KEY)
            assertThat(clipboardHelper.isSensitive).isTrue()
        }
    }

    private fun createPresenter(
        isReplacingExistingKey: Boolean = true,
        encryptionService: EncryptionService = FakeEncryptionService(),
        clipboardHelper: FakeClipboardHelper = FakeClipboardHelper(),
    ) = SecureBackupGenerateKeyPresenter(
        isReplacingExistingKey = isReplacingExistingKey,
        encryptionService = encryptionService,
        clipboardHelper = clipboardHelper,
        toastHelper = FakeToastHelper(),
    )
}
