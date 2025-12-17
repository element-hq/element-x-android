/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.root

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
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
            aSecureBackupRootState(
                backupState = BackupState.CREATING,
                enableAction = AsyncAction.Failure(Exception("Error")),
            ),
            aSecureBackupRootState(backupState = BackupState.ENABLING),
            aSecureBackupRootState(backupState = BackupState.RESUMING),
            aSecureBackupRootState(backupState = BackupState.DOWNLOADING),
            aSecureBackupRootState(backupState = BackupState.DISABLING),
            aSecureBackupRootState(backupState = BackupState.ENABLED),
            aSecureBackupRootState(backupState = BackupState.ENABLED, recoveryState = RecoveryState.UNKNOWN),
            aSecureBackupRootState(backupState = BackupState.ENABLED, recoveryState = RecoveryState.ENABLED),
            aSecureBackupRootState(backupState = BackupState.ENABLED, recoveryState = RecoveryState.DISABLED),
            aSecureBackupRootState(backupState = BackupState.ENABLED, recoveryState = RecoveryState.INCOMPLETE),
            aSecureBackupRootState(
                backupState = BackupState.UNKNOWN,
                doesBackupExistOnServer = AsyncData.Success(false),
                recoveryState = RecoveryState.ENABLED,
            ),
            aSecureBackupRootState(
                backupState = BackupState.UNKNOWN,
                doesBackupExistOnServer = AsyncData.Success(false),
                recoveryState = RecoveryState.ENABLED,
                displayKeyStorageDisabledError = true,
            ),
        )
}

fun aSecureBackupRootState(
    enableAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    backupState: BackupState = BackupState.UNKNOWN,
    doesBackupExistOnServer: AsyncData<Boolean> = AsyncData.Uninitialized,
    recoveryState: RecoveryState = RecoveryState.UNKNOWN,
    displayKeyStorageDisabledError: Boolean = false,
    snackbarMessage: SnackbarMessage? = null,
) = SecureBackupRootState(
    enableAction = enableAction,
    backupState = backupState,
    doesBackupExistOnServer = doesBackupExistOnServer,
    recoveryState = recoveryState,
    appName = "Element",
    displayKeyStorageDisabledError = displayKeyStorageDisabledError,
    snackbarMessage = snackbarMessage,
    eventSink = {},
)
