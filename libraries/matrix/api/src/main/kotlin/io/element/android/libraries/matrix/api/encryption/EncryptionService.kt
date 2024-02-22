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

package io.element.android.libraries.matrix.api.encryption

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface EncryptionService {
    val backupStateStateFlow: StateFlow<BackupState>
    val recoveryStateStateFlow: StateFlow<RecoveryState>
    val enableRecoveryProgressStateFlow: StateFlow<EnableRecoveryProgress>
    val isLastDevice: StateFlow<Boolean>

    suspend fun enableBackups(): Result<Unit>

    /**
     * Enable recovery. Observe enableProgressStateFlow to get progress and recovery key.
     */
    suspend fun enableRecovery(waitForBackupsToUpload: Boolean): Result<Unit>

    /**
     * Change the recovery and return the new recovery key.
     */
    suspend fun resetRecoveryKey(): Result<String>

    suspend fun disableRecovery(): Result<Unit>

    suspend fun doesBackupExistOnServer(): Result<Boolean>

    /**
     * Note: accept both recoveryKey and passphrase.
     */
    suspend fun recover(recoveryKey: String): Result<Unit>

    /**
     * Wait for backup upload steady state.
     */
    fun waitForBackupUploadSteadyState(): Flow<BackupUploadState>
}
