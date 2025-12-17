/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.enter

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyUserStory
import io.element.android.features.securebackup.impl.setup.views.RecoveryKeyViewState
import io.element.android.features.securebackup.impl.tools.RecoveryKeyTools
import io.element.android.libraries.architecture.AsyncAction
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
            assertThat(initialState.submitAction).isEqualTo(AsyncAction.Uninitialized)
            assertThat(initialState.recoveryKeyViewState).isEqualTo(
                RecoveryKeyViewState(
                    recoveryKeyUserStory = RecoveryKeyUserStory.Enter,
                    formattedRecoveryKey = "",
                    displayTextFieldContents = false,
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
                    displayTextFieldContents = false,
                    inProgress = false,
                )
            )
            encryptionService.givenRecoverFailure(AN_EXCEPTION)
            withRecoveryKeyState.eventSink(SecureBackupEnterRecoveryKeyEvents.Submit)
            val loadingState = awaitItem()
            assertThat(loadingState.submitAction).isEqualTo(AsyncAction.Loading)
            assertThat(loadingState.isSubmitEnabled).isFalse()
            val errorState = awaitItem()
            assertThat(errorState.submitAction).isEqualTo(AsyncAction.Failure(AN_EXCEPTION))
            assertThat(errorState.isSubmitEnabled).isFalse()
            errorState.eventSink(SecureBackupEnterRecoveryKeyEvents.ClearDialog)
            val clearedState = awaitItem()
            assertThat(clearedState.submitAction).isEqualTo(AsyncAction.Uninitialized)
            assertThat(clearedState.isSubmitEnabled).isTrue()
            encryptionService.givenRecoverFailure(null)
            clearedState.eventSink(SecureBackupEnterRecoveryKeyEvents.Submit)
            val loadingState2 = awaitItem()
            assertThat(loadingState2.submitAction).isEqualTo(AsyncAction.Loading)
            assertThat(loadingState2.isSubmitEnabled).isFalse()
            val finalState = awaitItem()
            assertThat(finalState.submitAction).isEqualTo(AsyncAction.Success(Unit))
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
