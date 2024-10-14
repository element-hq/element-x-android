/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.logout.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.BackupUploadState
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.encryption.SteadyStateException

open class LogoutStateProvider : PreviewParameterProvider<LogoutState> {
    override val values: Sequence<LogoutState>
        get() = sequenceOf(
            aLogoutState(),
            aLogoutState(isLastDevice = true),
            aLogoutState(isLastDevice = false, backupUploadState = BackupUploadState.Uploading(66, 200)),
            aLogoutState(isLastDevice = true, backupUploadState = BackupUploadState.Done),
            aLogoutState(logoutAction = AsyncAction.ConfirmingNoParams),
            aLogoutState(logoutAction = AsyncAction.Loading),
            aLogoutState(logoutAction = AsyncAction.Failure(Exception("Failed to logout"))),
            aLogoutState(backupUploadState = BackupUploadState.SteadyException(SteadyStateException.Connection("No network"))),
            // Last session no recovery
            aLogoutState(isLastDevice = true, recoveryState = RecoveryState.DISABLED),
            // Last session no backup
            aLogoutState(isLastDevice = true, backupState = BackupState.UNKNOWN, doesBackupExistOnServer = false),
        )
}

fun aLogoutState(
    isLastDevice: Boolean = false,
    backupState: BackupState = BackupState.ENABLED,
    doesBackupExistOnServer: Boolean = true,
    recoveryState: RecoveryState = RecoveryState.ENABLED,
    backupUploadState: BackupUploadState = BackupUploadState.Unknown,
    logoutAction: AsyncAction<String?> = AsyncAction.Uninitialized,
    eventSink: (LogoutEvents) -> Unit = {},
) = LogoutState(
    isLastDevice = isLastDevice,
    backupState = backupState,
    doesBackupExistOnServer = doesBackupExistOnServer,
    recoveryState = recoveryState,
    backupUploadState = backupUploadState,
    logoutAction = logoutAction,
    eventSink = eventSink,
)
