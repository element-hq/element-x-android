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

package io.element.android.features.securebackup.impl.disable

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncData
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
            assertThat(initialState.disableAction).isEqualTo(AsyncData.Uninitialized)
            assertThat(initialState.showConfirmationDialog).isFalse()
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
            assertThat(initialState.showConfirmationDialog).isFalse()
            initialState.eventSink(SecureBackupDisableEvents.DisableBackup(force = false))
            val state = awaitItem()
            assertThat(state.showConfirmationDialog).isTrue()
            initialState.eventSink(SecureBackupDisableEvents.DismissDialogs)
            val finalState = awaitItem()
            assertThat(finalState.showConfirmationDialog).isFalse()
        }
    }

    @Test
    fun `present - user delete backup success`() = runTest {
        val presenter = createSecureBackupDisablePresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.showConfirmationDialog).isFalse()
            initialState.eventSink(SecureBackupDisableEvents.DisableBackup(force = false))
            val state = awaitItem()
            assertThat(state.showConfirmationDialog).isTrue()
            initialState.eventSink(SecureBackupDisableEvents.DisableBackup(force = true))
            skipItems(1)
            val loadingState = awaitItem()
            assertThat(loadingState.showConfirmationDialog).isFalse()
            assertThat(loadingState.disableAction).isInstanceOf(AsyncData.Loading::class.java)
            val finalState = awaitItem()
            assertThat(finalState.disableAction).isEqualTo(AsyncData.Success(Unit))
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
            assertThat(initialState.showConfirmationDialog).isFalse()
            initialState.eventSink(SecureBackupDisableEvents.DisableBackup(force = false))
            val state = awaitItem()
            assertThat(state.showConfirmationDialog).isTrue()
            initialState.eventSink(SecureBackupDisableEvents.DisableBackup(force = true))
            skipItems(1)
            val loadingState = awaitItem()
            assertThat(loadingState.showConfirmationDialog).isFalse()
            assertThat(loadingState.disableAction).isInstanceOf(AsyncData.Loading::class.java)
            val errorState = awaitItem()
            assertThat(errorState.disableAction).isInstanceOf(AsyncData.Failure::class.java)
            errorState.eventSink(SecureBackupDisableEvents.DismissDialogs)
            val finalState = awaitItem()
            assertThat(finalState.disableAction).isEqualTo(AsyncData.Uninitialized)
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
