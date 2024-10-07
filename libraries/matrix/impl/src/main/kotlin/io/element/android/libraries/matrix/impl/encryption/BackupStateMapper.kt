/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.encryption

import io.element.android.libraries.matrix.api.encryption.BackupState
import org.matrix.rustcomponents.sdk.BackupState as RustBackupState

class BackupStateMapper {
    fun map(backupState: RustBackupState): BackupState {
        return when (backupState) {
            RustBackupState.UNKNOWN -> BackupState.UNKNOWN
            RustBackupState.CREATING -> BackupState.CREATING
            RustBackupState.ENABLING -> BackupState.ENABLING
            RustBackupState.RESUMING -> BackupState.RESUMING
            RustBackupState.ENABLED -> BackupState.ENABLED
            RustBackupState.DOWNLOADING -> BackupState.DOWNLOADING
            RustBackupState.DISABLING -> BackupState.DISABLING
        }
    }
}
