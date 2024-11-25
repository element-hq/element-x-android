/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.logout.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.BackupUploadState
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.test.A_THROWABLE
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class LogoutPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createLogoutPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.isLastDevice).isFalse()
            assertThat(initialState.backupState).isEqualTo(BackupState.UNKNOWN)
            assertThat(initialState.doesBackupExistOnServer).isTrue()
            assertThat(initialState.recoveryState).isEqualTo(RecoveryState.UNKNOWN)
            assertThat(initialState.backupUploadState).isEqualTo(BackupUploadState.Unknown)
            assertThat(initialState.logoutAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - initial state - last session`() = runTest {
        val presenter = createLogoutPresenter(
            encryptionService = FakeEncryptionService().apply {
                emitIsLastDevice(true)
            }
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(2)
            val initialState = awaitItem()
            assertThat(initialState.isLastDevice).isTrue()
            assertThat(initialState.backupUploadState).isEqualTo(BackupUploadState.Unknown)
            assertThat(initialState.logoutAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - initial state - backing up`() = runTest {
        val encryptionService = FakeEncryptionService()
        encryptionService.givenWaitForBackupUploadSteadyStateFlow(
            flow {
                emit(BackupUploadState.Waiting)
                delay(1)
                emit(BackupUploadState.Uploading(backedUpCount = 1, totalCount = 2))
                delay(1)
                emit(BackupUploadState.Done)
            }
        )
        val presenter = createLogoutPresenter(
            encryptionService = encryptionService
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.isLastDevice).isFalse()
            assertThat(initialState.backupUploadState).isEqualTo(BackupUploadState.Unknown)
            assertThat(initialState.logoutAction).isEqualTo(AsyncAction.Uninitialized)
            val waitingState = awaitItem()
            assertThat(waitingState.backupUploadState).isEqualTo(BackupUploadState.Waiting)
            skipItems(1)
            val uploadingState = awaitItem()
            assertThat(uploadingState.backupUploadState).isEqualTo(BackupUploadState.Uploading(backedUpCount = 1, totalCount = 2))
            val doneState = awaitItem()
            assertThat(doneState.backupUploadState).isEqualTo(BackupUploadState.Done)
        }
    }

    @Test
    fun `present - logout then cancel`() = runTest {
        val presenter = createLogoutPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(LogoutEvents.Logout(ignoreSdkError = false))
            val confirmationState = awaitItem()
            assertThat(confirmationState.logoutAction).isEqualTo(AsyncAction.ConfirmingNoParams)
            initialState.eventSink.invoke(LogoutEvents.CloseDialogs)
            val finalState = awaitItem()
            assertThat(finalState.logoutAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - logout then confirm`() = runTest {
        val presenter = createLogoutPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(LogoutEvents.Logout(ignoreSdkError = false))
            val confirmationState = awaitItem()
            assertThat(confirmationState.logoutAction).isEqualTo(AsyncAction.ConfirmingNoParams)
            confirmationState.eventSink.invoke(LogoutEvents.Logout(ignoreSdkError = false))
            val loadingState = awaitItem()
            assertThat(loadingState.logoutAction).isInstanceOf(AsyncAction.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.logoutAction).isInstanceOf(AsyncAction.Success::class.java)
        }
    }

    @Test
    fun `present - logout with error then cancel`() = runTest {
        val matrixClient = FakeMatrixClient().apply {
            logoutLambda = { _, _ ->
                throw A_THROWABLE
            }
        }
        val presenter = createLogoutPresenter(
            matrixClient,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(LogoutEvents.Logout(ignoreSdkError = false))
            val confirmationState = awaitItem()
            assertThat(confirmationState.logoutAction).isEqualTo(AsyncAction.ConfirmingNoParams)
            confirmationState.eventSink.invoke(LogoutEvents.Logout(ignoreSdkError = false))
            val loadingState = awaitItem()
            assertThat(loadingState.logoutAction).isInstanceOf(AsyncAction.Loading::class.java)
            val errorState = awaitItem()
            assertThat(errorState.logoutAction).isEqualTo(AsyncAction.Failure(A_THROWABLE))
            errorState.eventSink.invoke(LogoutEvents.CloseDialogs)
            val finalState = awaitItem()
            assertThat(finalState.logoutAction).isEqualTo(AsyncAction.Uninitialized)
        }
    }

    @Test
    fun `present - logout with error then force`() = runTest {
        val matrixClient = FakeMatrixClient().apply {
            logoutLambda = { ignoreSdkError, _ ->
                if (!ignoreSdkError) {
                    throw A_THROWABLE
                } else {
                    null
                }
            }
        }
        val presenter = createLogoutPresenter(
            matrixClient,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitFirstItem()
            initialState.eventSink.invoke(LogoutEvents.Logout(ignoreSdkError = false))
            val confirmationState = awaitItem()
            assertThat(confirmationState.logoutAction).isEqualTo(AsyncAction.ConfirmingNoParams)
            confirmationState.eventSink.invoke(LogoutEvents.Logout(ignoreSdkError = false))
            val loadingState = awaitItem()
            assertThat(loadingState.logoutAction).isInstanceOf(AsyncAction.Loading::class.java)
            val errorState = awaitItem()
            assertThat(errorState.logoutAction).isEqualTo(AsyncAction.Failure(A_THROWABLE))
            errorState.eventSink.invoke(LogoutEvents.Logout(ignoreSdkError = true))
            val loadingState2 = awaitItem()
            assertThat(loadingState2.logoutAction).isInstanceOf(AsyncAction.Loading::class.java)
            val successState = awaitItem()
            assertThat(successState.logoutAction).isInstanceOf(AsyncAction.Success::class.java)
        }
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitFirstItem(): T {
        skipItems(2)
        return awaitItem()
    }

    private fun createLogoutPresenter(
        matrixClient: MatrixClient = FakeMatrixClient(),
        encryptionService: EncryptionService = FakeEncryptionService(),
    ): LogoutPresenter = LogoutPresenter(
        matrixClient = matrixClient,
        encryptionService = encryptionService,
    )
}
