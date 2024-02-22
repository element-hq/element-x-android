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
            aLogoutState(logoutAction = AsyncAction.Confirming),
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
