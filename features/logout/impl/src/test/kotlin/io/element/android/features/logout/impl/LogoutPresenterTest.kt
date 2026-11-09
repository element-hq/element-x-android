/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.logout.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.encryption.BackupUploadState
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.encryption.SteadyStateException
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.test.FakeWorkManagerScheduler
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class LogoutPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - the confirmation is loading until the devices to verify against are known`() = runTest {
        val presenter = createLogoutPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.confirmation).isEqualTo(LogoutConfirmation.Loading)
            assertThat(initialState.logoutAction).isEqualTo(AsyncAction.Uninitialized)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - another verified device only needs a reminder`() = runTest {
        val encryptionService = FakeEncryptionService().apply {
            emitHasDevicesToVerifyAgainst(AsyncData.Success(true))
            emitRecoveryState(RecoveryState.DISABLED)
        }
        assertConfirmation(encryptionService, LogoutConfirmation.OtherVerifiedDevice)
    }

    @Test
    fun `present - the only device with a recovery key can generate or check it`() = runTest {
        val encryptionService = FakeEncryptionService().apply {
            emitHasDevicesToVerifyAgainst(AsyncData.Success(false))
            emitRecoveryState(RecoveryState.ENABLED)
        }
        assertConfirmation(encryptionService, LogoutConfirmation.RecoveryKeyOnly)
    }

    @Test
    fun `present - the only device without recovery needs to set it up`() = runTest {
        val encryptionService = FakeEncryptionService().apply {
            emitHasDevicesToVerifyAgainst(AsyncData.Success(false))
            emitRecoveryState(RecoveryState.DISABLED)
        }
        assertConfirmation(encryptionService, LogoutConfirmation.NoRecovery)
    }

    @Test
    fun `present - the only device with an incomplete recovery needs to set it up`() = runTest {
        val encryptionService = FakeEncryptionService().apply {
            emitHasDevicesToVerifyAgainst(AsyncData.Success(false))
            emitRecoveryState(RecoveryState.INCOMPLETE)
        }
        assertConfirmation(encryptionService, LogoutConfirmation.NoRecovery)
    }

    @Test
    fun `present - a recovery key without a key backup is reported`() = runTest {
        val encryptionService = FakeEncryptionService().apply {
            emitHasDevicesToVerifyAgainst(AsyncData.Success(false))
            emitRecoveryState(RecoveryState.ENABLED)
            givenDoesBackupExistOnServerResult(Result.success(false))
        }
        assertConfirmation(encryptionService, LogoutConfirmation.KeyBackupDisabled)
    }

    @Test
    fun `present - the confirmation stays loading while the recovery state is not known`() = runTest {
        val encryptionService = FakeEncryptionService().apply {
            emitHasDevicesToVerifyAgainst(AsyncData.Success(false))
            emitRecoveryState(RecoveryState.WAITING_FOR_SYNC)
        }
        moleculeFlow(RecompositionMode.Immediate) {
            createLogoutPresenter(encryptionService = encryptionService).present()
        }.test {
            assertThat(awaitItem().confirmation).isEqualTo(LogoutConfirmation.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - keys still being backed up have to finish first, even with another verified device`() = runTest {
        val encryptionService = FakeEncryptionService().apply {
            emitHasDevicesToVerifyAgainst(AsyncData.Success(true))
            givenWaitForBackupUploadSteadyStateFlow(flowOf(BackupUploadState.Uploading(backedUpCount = 1, totalCount = 2)))
        }
        assertConfirmation(encryptionService, LogoutConfirmation.BackupOngoing)
    }

    @Test
    fun `present - keys that cannot be backed up because of the network are reported`() = runTest {
        val encryptionService = FakeEncryptionService().apply {
            emitHasDevicesToVerifyAgainst(AsyncData.Success(true))
            givenWaitForBackupUploadSteadyStateFlow(flowOf(BackupUploadState.SteadyException(SteadyStateException.Connection("No network"))))
        }
        assertConfirmation(encryptionService, LogoutConfirmation.BackupOffline)
    }

    @Test
    fun `present - logout`() = runTest {
        val cancelWorkManagerJobsLambda = lambdaRecorder<SessionId, WorkManagerRequestType?, Unit> { _, _ -> }
        val presenter = createLogoutPresenter(workManagerScheduler = FakeWorkManagerScheduler(cancelLambda = cancelWorkManagerJobsLambda))
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(LogoutEvent.Logout(ignoreSdkError = false))
            awaitState { it.logoutAction is AsyncAction.Loading }
            awaitState { it.logoutAction is AsyncAction.Success }
            cancelWorkManagerJobsLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - logout with error then cancel`() = runTest {
        val matrixClient = FakeMatrixClient().apply {
            logoutLambda = { _, _ -> throw AN_EXCEPTION }
        }
        val presenter = createLogoutPresenter(matrixClient = matrixClient)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(LogoutEvent.Logout(ignoreSdkError = false))
            awaitState { it.logoutAction is AsyncAction.Loading }
            val errorState = awaitState { it.logoutAction is AsyncAction.Failure }
            assertThat(errorState.logoutAction).isEqualTo(AsyncAction.Failure(AN_EXCEPTION))
            errorState.eventSink(LogoutEvent.CloseDialogs)
            awaitState { it.logoutAction == AsyncAction.Uninitialized }
        }
    }

    @Test
    fun `present - logout with error then force`() = runTest {
        val matrixClient = FakeMatrixClient().apply {
            logoutLambda = { ignoreSdkError, _ ->
                if (!ignoreSdkError) {
                    throw AN_EXCEPTION
                }
            }
        }
        val presenter = createLogoutPresenter(matrixClient = matrixClient)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(LogoutEvent.Logout(ignoreSdkError = false))
            awaitState { it.logoutAction is AsyncAction.Loading }
            val errorState = awaitState { it.logoutAction is AsyncAction.Failure }
            assertThat(errorState.logoutAction).isEqualTo(AsyncAction.Failure(AN_EXCEPTION))
            errorState.eventSink(LogoutEvent.Logout(ignoreSdkError = true))
            awaitState { it.logoutAction is AsyncAction.Loading }
            awaitState { it.logoutAction is AsyncAction.Success }
        }
    }

    @Test
    fun `present - after the recovery key, the device is removed once the keys are backed up`() = runTest {
        val encryptionService = FakeEncryptionService().apply {
            givenWaitForBackupUploadSteadyStateFlow(
                flow {
                    emit(BackupUploadState.Waiting)
                    delay(1)
                    emit(BackupUploadState.Done)
                }
            )
        }
        val logoutLambda = lambdaRecorder<Boolean, Boolean, Unit> { _, _ -> }
        val presenter = createLogoutPresenter(
            matrixClient = FakeMatrixClient().apply { this.logoutLambda = logoutLambda },
            encryptionService = encryptionService,
            removeOnceKeysAreBackedUp = true,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().confirmation).isEqualTo(LogoutConfirmation.WaitingForBackup)
            awaitState { it.logoutAction is AsyncAction.Success }
            logoutLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - after the recovery key, the device is kept if the keys could not be backed up`() = runTest {
        val encryptionService = FakeEncryptionService().apply {
            givenWaitForBackupUploadSteadyStateFlow(flowOf(BackupUploadState.Error))
        }
        val logoutLambda = lambdaRecorder<Boolean, Boolean, Unit> { _, _ -> }
        val presenter = createLogoutPresenter(
            matrixClient = FakeMatrixClient().apply { this.logoutLambda = logoutLambda },
            encryptionService = encryptionService,
            removeOnceKeysAreBackedUp = true,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val failureState = awaitState { it.logoutAction is AsyncAction.Failure }
            logoutLambda.assertions().isNeverCalled()
            failureState.eventSink(LogoutEvent.CloseDialogs)
            val closedState = awaitState { it.logoutAction == AsyncAction.Uninitialized }
            assertThat(closedState.confirmation).isNotEqualTo(LogoutConfirmation.WaitingForBackup)
        }
    }

    @Test
    fun `present - after the recovery key, the device is kept while offline`() = runTest {
        val encryptionService = FakeEncryptionService().apply {
            givenWaitForBackupUploadSteadyStateFlow(flowOf(BackupUploadState.SteadyException(SteadyStateException.Connection("No network"))))
        }
        val logoutLambda = lambdaRecorder<Boolean, Boolean, Unit> { _, _ -> }
        val presenter = createLogoutPresenter(
            matrixClient = FakeMatrixClient().apply { this.logoutLambda = logoutLambda },
            encryptionService = encryptionService,
            removeOnceKeysAreBackedUp = true,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitState { it.confirmation == LogoutConfirmation.BackupOffline }
            logoutLambda.assertions().isNeverCalled()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - a recovery key is not reported as lacking a key backup when this cannot be checked`() = runTest {
        val encryptionService = FakeEncryptionService().apply {
            emitHasDevicesToVerifyAgainst(AsyncData.Success(false))
            emitRecoveryState(RecoveryState.ENABLED)
            givenDoesBackupExistOnServerResult(Result.failure(AN_EXCEPTION))
        }
        assertConfirmation(encryptionService, LogoutConfirmation.RecoveryKeyOnly)
    }

    @Test
    fun `present - after the recovery key, the device is kept if the backup is turned off meanwhile`() = runTest {
        val encryptionService = FakeEncryptionService().apply {
            givenWaitForBackupUploadSteadyStateFlow(flowOf(BackupUploadState.SteadyException(SteadyStateException.BackupDisabled("Disabled"))))
        }
        val logoutLambda = lambdaRecorder<Boolean, Boolean, Unit> { _, _ -> }
        val presenter = createLogoutPresenter(
            matrixClient = FakeMatrixClient().apply { this.logoutLambda = logoutLambda },
            encryptionService = encryptionService,
            removeOnceKeysAreBackedUp = true,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitState { it.logoutAction is AsyncAction.Failure }
            logoutLambda.assertions().isNeverCalled()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun assertConfirmation(encryptionService: EncryptionService, expected: LogoutConfirmation) {
        moleculeFlow(RecompositionMode.Immediate) {
            createLogoutPresenter(encryptionService = encryptionService).present()
        }.test {
            assertThat(awaitState { it.confirmation == expected }.confirmation).isEqualTo(expected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun ReceiveTurbine<LogoutState>.awaitState(predicate: (LogoutState) -> Boolean): LogoutState {
        while (true) {
            val state = awaitItem()
            if (predicate(state)) return state
        }
    }
}

internal fun createLogoutPresenter(
    matrixClient: MatrixClient = FakeMatrixClient(),
    encryptionService: EncryptionService = FakeEncryptionService(),
    workManagerScheduler: FakeWorkManagerScheduler = FakeWorkManagerScheduler(cancelLambda = { _, _ -> }),
    removeOnceKeysAreBackedUp: Boolean = false,
): LogoutPresenter = LogoutPresenter(
    removeOnceKeysAreBackedUp = removeOnceKeysAreBackedUp,
    matrixClient = matrixClient,
    encryptionService = encryptionService,
    workManagerScheduler = workManagerScheduler,
)
