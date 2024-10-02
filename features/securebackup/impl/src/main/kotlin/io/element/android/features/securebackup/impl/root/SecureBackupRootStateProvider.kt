/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.root

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.RecoveryState

open class SecureBackupRootStateProvider : PreviewParameterProvider<SecureBackupRootState> {
    override val values: Sequence<SecureBackupRootState>
        get() = sequenceOf(
            aSecureBackupRootState(backupState = BackupState.UNKNOWN, doesBackupExistOnServer = AsyncData.Uninitialized),
            aSecureBackupRootState(backupState = BackupState.UNKNOWN, doesBackupExistOnServer = AsyncData.Success(true)),
            aSecureBackupRootState(backupState = BackupState.UNKNOWN, doesBackupExistOnServer = AsyncData.Success(false)),
            aSecureBackupRootState(backupState = BackupState.UNKNOWN, doesBackupExistOnServer = AsyncData.Failure(Exception("An error"))),
            aSecureBackupRootState(backupState = BackupState.WAITING_FOR_SYNC),
            aSecureBackupRootState(backupState = BackupState.CREATING),
            aSecureBackupRootState(backupState = BackupState.RESUMING),
            aSecureBackupRootState(backupState = BackupState.DOWNLOADING),
            aSecureBackupRootState(backupState = BackupState.DISABLING),
            aSecureBackupRootState(backupState = BackupState.ENABLED),
            aSecureBackupRootState(recoveryState = RecoveryState.UNKNOWN),
            aSecureBackupRootState(recoveryState = RecoveryState.ENABLED),
            aSecureBackupRootState(recoveryState = RecoveryState.DISABLED),
            aSecureBackupRootState(recoveryState = RecoveryState.INCOMPLETE),
            // Add other states here
        )
}

fun aSecureBackupRootState(
    backupState: BackupState = BackupState.UNKNOWN,
    doesBackupExistOnServer: AsyncData<Boolean> = AsyncData.Uninitialized,
    recoveryState: RecoveryState = RecoveryState.UNKNOWN,
    snackbarMessage: SnackbarMessage? = null,
) = SecureBackupRootState(
    backupState = backupState,
    doesBackupExistOnServer = doesBackupExistOnServer,
    recoveryState = recoveryState,
    appName = "Element",
    snackbarMessage = snackbarMessage,
    eventSink = {},
)
