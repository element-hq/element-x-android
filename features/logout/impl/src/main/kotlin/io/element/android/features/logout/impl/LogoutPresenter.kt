/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.logout.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.logout.impl.tools.isBackingUp
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.bool.orTrue
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.BackupUploadState
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.encryption.SteadyStateException
import io.element.android.libraries.workmanager.api.WorkManagerScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AssistedInject
class LogoutPresenter(
    @Assisted private val removeOnceKeysAreBackedUp: Boolean,
    private val matrixClient: MatrixClient,
    private val encryptionService: EncryptionService,
    private val workManagerScheduler: WorkManagerScheduler,
) : Presenter<LogoutState> {
    @AssistedFactory
    interface Factory {
        /**
         * @param removeOnceKeysAreBackedUp whether the user has just finished with their recovery key, so that the device
         * is removed as soon as the room keys are backed up, instead of asking again.
         */
        fun create(removeOnceKeysAreBackedUp: Boolean): LogoutPresenter
    }

    @Composable
    override fun present(): LogoutState {
        val localCoroutineScope = rememberCoroutineScope()
        val logoutAction: MutableState<AsyncAction<Unit>> = remember {
            mutableStateOf(AsyncAction.Uninitialized)
        }

        val backupUploadState: BackupUploadState by remember {
            encryptionService.waitForBackupUploadSteadyState()
        }
            .collectAsState(initial = BackupUploadState.Unknown)
        val hasDevicesToVerifyAgainst by encryptionService.hasDevicesToVerifyAgainst.collectAsState()
        val backupState by encryptionService.backupStateStateFlow.collectAsState()
        val recoveryState by encryptionService.recoveryStateStateFlow.collectAsState()

        val doesBackupExistOnServerAction: MutableState<AsyncData<Boolean>> = remember {
            mutableStateOf(AsyncData.Uninitialized)
        }
        LaunchedEffect(backupState) {
            if (backupState == BackupState.UNKNOWN) {
                getKeyBackupStatus(doesBackupExistOnServerAction)
            }
        }

        var isWaitingForBackup by remember { mutableStateOf(removeOnceKeysAreBackedUp) }
        LaunchedEffect(isWaitingForBackup, backupUploadState) {
            if (!isWaitingForBackup) return@LaunchedEffect
            when {
                backupUploadState == BackupUploadState.Done -> {
                    isWaitingForBackup = false
                    localCoroutineScope.logout(logoutAction, ignoreSdkError = false)
                }
                backupUploadState.hasFailed() -> {
                    isWaitingForBackup = false
                    logoutAction.value = AsyncAction.Failure(IllegalStateException("The room keys could not be backed up"))
                }
            }
        }

        fun handleEvent(event: LogoutEvent) {
            when (event) {
                is LogoutEvent.Logout -> localCoroutineScope.logout(logoutAction, event.ignoreSdkError)
                LogoutEvent.CloseDialogs -> {
                    isWaitingForBackup = false
                    logoutAction.value = AsyncAction.Uninitialized
                }
            }
        }

        val isKeyBackupDisabled = backupState == BackupState.UNKNOWN && !doesBackupExistOnServerAction.value.dataOrNull().orTrue()
        return LogoutState(
            confirmation = when {
                backupUploadState.isOffline() -> LogoutConfirmation.BackupOffline
                isWaitingForBackup -> LogoutConfirmation.WaitingForBackup
                backupUploadState.isBackingUp() -> LogoutConfirmation.BackupOngoing
                else -> confirmationFor(hasDevicesToVerifyAgainst, recoveryState, isKeyBackupDisabled)
            },
            logoutAction = logoutAction.value,
            eventSink = ::handleEvent,
        )
    }

    private fun confirmationFor(
        hasDevicesToVerifyAgainst: AsyncData<Boolean>,
        recoveryState: RecoveryState,
        isKeyBackupDisabled: Boolean,
    ): LogoutConfirmation {
        val hasOtherVerifiedDevice = hasDevicesToVerifyAgainst.dataOrNull() ?: return LogoutConfirmation.Loading
        return when {
            hasOtherVerifiedDevice -> LogoutConfirmation.OtherVerifiedDevice
            // Whether there is a recovery key is not known yet, and telling the user that there is none could make them replace it.
            recoveryState == RecoveryState.UNKNOWN || recoveryState == RecoveryState.WAITING_FOR_SYNC -> LogoutConfirmation.Loading
            recoveryState == RecoveryState.ENABLED && isKeyBackupDisabled -> LogoutConfirmation.KeyBackupDisabled
            recoveryState == RecoveryState.ENABLED -> LogoutConfirmation.RecoveryKeyOnly
            else -> LogoutConfirmation.NoRecovery
        }
    }

    private fun BackupUploadState.isOffline(): Boolean {
        return (this as? BackupUploadState.SteadyException)?.exception is SteadyStateException.Connection
    }

    private fun BackupUploadState.hasFailed(): Boolean {
        return this == BackupUploadState.Error || this is BackupUploadState.SteadyException && !isOffline()
    }

    private fun CoroutineScope.getKeyBackupStatus(action: MutableState<AsyncData<Boolean>>) = launch {
        suspend {
            encryptionService.doesBackupExistOnServer().getOrThrow()
        }.runCatchingUpdatingState(action)
    }

    private fun CoroutineScope.logout(
        logoutAction: MutableState<AsyncAction<Unit>>,
        ignoreSdkError: Boolean,
    ) = launch {
        suspend {
            // Cancel any pending work (e.g. notification sync)
            workManagerScheduler.cancel(matrixClient.sessionId)

            matrixClient.logout(userInitiated = true, ignoreSdkError)
        }.runCatchingUpdatingState(logoutAction)
    }
}
