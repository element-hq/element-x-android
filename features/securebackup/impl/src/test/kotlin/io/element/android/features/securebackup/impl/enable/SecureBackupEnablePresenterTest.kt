/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.enable

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SecureBackupEnablePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.enableAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - user enable backup`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(SecureBackupEnableEvents.EnableBackup)
            val loadingState = awaitItem()
            assertThat(loadingState.enableAction).isInstanceOf(AsyncAction.Loading::class.java)
            val finalState = awaitItem()
            assertThat(finalState.enableAction).isEqualTo(AsyncAction.Success(Unit))
        }
    }

    @Test
    fun `present - user enable backup with error`() = runTest {
        val encryptionService = FakeEncryptionService()
        encryptionService.givenEnableBackupsFailure(AN_EXCEPTION)
        val presenter = createPresenter(encryptionService = encryptionService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(SecureBackupEnableEvents.EnableBackup)
            val loadingState = awaitItem()
            assertThat(loadingState.enableAction).isInstanceOf(AsyncAction.Loading::class.java)
            val errorState = awaitItem()
            assertThat(errorState.enableAction).isEqualTo(AsyncAction.Failure(AN_EXCEPTION))
            errorState.eventSink(SecureBackupEnableEvents.DismissDialog)
            val finalState = awaitItem()
            assertThat(finalState.enableAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    private fun createPresenter(
        encryptionService: EncryptionService = FakeEncryptionService(),
    ) = SecureBackupEnablePresenter(
        encryptionService = encryptionService,
    )
}
