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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.RecoveryState

open class SecureBackupRootStateProvider : PreviewParameterProvider<SecureBackupRootState> {
    override val values: Sequence<SecureBackupRootState>
        get() = sequenceOf(
            aSecureBackupRootState(backupState = BackupState.UNKNOWN),
            aSecureBackupRootState(backupState = BackupState.ENABLED),
            aSecureBackupRootState(backupState = BackupState.DISABLED),
            aSecureBackupRootState(recoveryState = RecoveryState.UNKNOWN),
            aSecureBackupRootState(recoveryState = RecoveryState.ENABLED),
            aSecureBackupRootState(recoveryState = RecoveryState.DISABLED),
            aSecureBackupRootState(recoveryState = RecoveryState.INCOMPLETE),
            // Add other states here
        )
}

fun aSecureBackupRootState(
    backupState: BackupState = BackupState.UNKNOWN,
    recoveryState: RecoveryState = RecoveryState.UNKNOWN,
    snackbarMessage: SnackbarMessage? = null,
) = SecureBackupRootState(
    backupState = backupState,
    recoveryState = recoveryState,
    appName = "Element",
    snackbarMessage = snackbarMessage,
)
