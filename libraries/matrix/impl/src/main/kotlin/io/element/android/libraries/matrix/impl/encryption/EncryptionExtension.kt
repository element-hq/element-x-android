/*
 * Copyright (c) 2024 New Vector Ltd
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
