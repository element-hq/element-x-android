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

package io.element.android.features.securebackup.impl.root

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SecureBackupRootPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createSecureBackupRootPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(2)
            val initialState = awaitItem()
            assertThat(initialState.backupState).isEqualTo(BackupState.UNKNOWN)
            assertThat(initialState.doesBackupExistOnServer.dataOrNull()).isTrue()
            assertThat(initialState.recoveryState).isEqualTo(RecoveryState.UNKNOWN)
            assertThat(initialState.appName).isEqualTo("Element")
            assertThat(initialState.snackbarMessage).isNull()
        }
    }

    @Test
    fun `present - Unknown state`() = runTest {
        val encryptionService = FakeEncryptionService()
        val presenter = createSecureBackupRootPresenter(
            encryptionService = encryptionService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            encryptionService.givenDoesBackupExistOnServerResult(Result.failure(AN_EXCEPTION))
            assertThat(initialState.backupState).isEqualTo(BackupState.UNKNOWN)
            assertThat(initialState.doesBackupExistOnServer).isEqualTo(AsyncData.Uninitialized)
            val loadingState1 = awaitItem()
            assertThat(loadingState1.doesBackupExistOnServer).isInstanceOf(AsyncData.Loading::class.java)
            val errorState = awaitItem()
            assertThat(errorState.doesBackupExistOnServer).isEqualTo(AsyncData.Failure<Boolean>(AN_EXCEPTION))
            encryptionService.givenDoesBackupExistOnServerResult(Result.success(false))
            errorState.eventSink.invoke(SecureBackupRootEvents.RetryKeyBackupState)
            val loadingState2 = awaitItem()
            assertThat(loadingState2.doesBackupExistOnServer).isInstanceOf(AsyncData.Loading::class.java)
            val finalState = awaitItem()
            assertThat(finalState.doesBackupExistOnServer.dataOrNull()).isFalse()
        }
    }

    private fun createSecureBackupRootPresenter(
        encryptionService: EncryptionService = FakeEncryptionService(),
        appName: String = "Element",
    ): SecureBackupRootPresenter {
        return SecureBackupRootPresenter(
            encryptionService = encryptionService,
            buildMeta = aBuildMeta(applicationName = appName),
            snackbarDispatcher = SnackbarDispatcher(),
        )
    }
}
