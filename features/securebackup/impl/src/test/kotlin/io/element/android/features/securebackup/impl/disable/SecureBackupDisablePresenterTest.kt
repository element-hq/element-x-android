/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.disable

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SecureBackupDisablePresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createSecureBackupDisablePresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.backupState).isEqualTo(BackupState.UNKNOWN)
            assertThat(initialState.disableAction).isEqualTo(AsyncAction.Uninitialized)
            assertThat(initialState.appName).isEqualTo("Element")
        }
    }

    @Test
    fun `present - user delete backup and cancel`() = runTest {
        val presenter = createSecureBackupDisablePresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(SecureBackupDisableEvents.DisableBackup)
            val state = awaitItem()
            assertThat(state.disableAction).isEqualTo(AsyncAction.ConfirmingNoParams)
            initialState.eventSink(SecureBackupDisableEvents.DismissDialogs)
            val finalState = awaitItem()
            assertThat(finalState.disableAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - user delete backup success`() = runTest {
        val presenter = createSecureBackupDisablePresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.disableAction).isEqualTo(AsyncAction.Uninitialized)
            initialState.eventSink(SecureBackupDisableEvents.DisableBackup)
            val state = awaitItem()
            assertThat(state.disableAction).isEqualTo(AsyncAction.ConfirmingNoParams)
            initialState.eventSink(SecureBackupDisableEvents.DisableBackup)
            val loadingState = awaitItem()
            assertThat(loadingState.disableAction).isInstanceOf(AsyncAction.Loading::class.java)
            val finalState = awaitItem()
            assertThat(finalState.disableAction).isEqualTo(AsyncAction.Success(Unit))
        }
    }

    @Test
    fun `present - user delete backup error`() = runTest {
        val encryptionService = FakeEncryptionService().apply {
            givenDisableRecoveryFailure(Exception("failure"))
        }
        val presenter = createSecureBackupDisablePresenter(
            encryptionService = encryptionService
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.disableAction).isEqualTo(AsyncAction.Uninitialized)
            initialState.eventSink(SecureBackupDisableEvents.DisableBackup)
            val state = awaitItem()
            assertThat(state.disableAction).isEqualTo(AsyncAction.ConfirmingNoParams)
            initialState.eventSink(SecureBackupDisableEvents.DisableBackup)
            val loadingState = awaitItem()
            assertThat(loadingState.disableAction).isInstanceOf(AsyncAction.Loading::class.java)
            val errorState = awaitItem()
            assertThat(errorState.disableAction).isInstanceOf(AsyncAction.Failure::class.java)
            errorState.eventSink(SecureBackupDisableEvents.DismissDialogs)
            val finalState = awaitItem()
            assertThat(finalState.disableAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    private fun createSecureBackupDisablePresenter(
        encryptionService: EncryptionService = FakeEncryptionService(),
        appName: String = "Element",
    ): SecureBackupDisablePresenter {
        return SecureBackupDisablePresenter(
            encryptionService = encryptionService,
            buildMeta = aBuildMeta(
                applicationName = appName,
            )
        )
    }
}
