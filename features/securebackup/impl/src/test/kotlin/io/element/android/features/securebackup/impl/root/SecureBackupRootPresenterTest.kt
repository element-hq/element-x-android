/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
