/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.encryption

import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import kotlinx.coroutines.flow.Flow
import org.matrix.rustcomponents.sdk.BackupStateListener
import org.matrix.rustcomponents.sdk.EncryptionInterface
import org.matrix.rustcomponents.sdk.RecoveryStateListener
import org.matrix.rustcomponents.sdk.BackupState as RustBackupState
import org.matrix.rustcomponents.sdk.RecoveryState as RustRecoveryState

internal fun EncryptionInterface.backupStateFlow(): Flow<BackupState> = mxCallbackFlow {
    val backupStateMapper = BackupStateMapper()
    trySend(backupStateMapper.map(backupState()))
    val listener = object : BackupStateListener {
        override fun onUpdate(status: RustBackupState) {
            trySend(backupStateMapper.map(status))
        }
    }
    backupStateListener(listener)
}

internal fun EncryptionInterface.recoveryStateFlow(): Flow<RecoveryState> = mxCallbackFlow {
    val recoveryStateMapper = RecoveryStateMapper()
    trySend(recoveryStateMapper.map(recoveryState()))
    val listener = object : RecoveryStateListener {
        override fun onUpdate(status: RustRecoveryState) {
            trySend(recoveryStateMapper.map(status))
        }
    }
    recoveryStateListener(listener)
}
