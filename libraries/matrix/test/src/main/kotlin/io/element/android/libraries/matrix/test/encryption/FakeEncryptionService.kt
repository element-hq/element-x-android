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

package io.element.android.libraries.matrix.test.encryption

import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.BackupUploadState
import io.element.android.libraries.matrix.api.encryption.EnableRecoveryProgress
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeEncryptionService : EncryptionService {
    private var disableRecoveryFailure: Exception? = null
    override val backupStateStateFlow: MutableStateFlow<BackupState> = MutableStateFlow(BackupState.UNKNOWN)
    override val recoveryStateStateFlow: MutableStateFlow<RecoveryState> = MutableStateFlow(RecoveryState.UNKNOWN)
    override val enableRecoveryProgressStateFlow: MutableStateFlow<EnableRecoveryProgress> = MutableStateFlow(EnableRecoveryProgress.Unknown)
    private var waitForBackupUploadSteadyStateFlow: Flow<BackupUploadState> = flowOf()

    private var fixRecoveryIssuesFailure: Exception? = null

    override suspend fun enableBackups(): Result<Unit> = simulateLongTask {
        return Result.success(Unit)
    }

    fun givenDisableRecoveryFailure(exception: Exception) {
        disableRecoveryFailure = exception
    }

    fun givenFixRecoveryIssuesFailure(exception: Exception?) {
        fixRecoveryIssuesFailure = exception
    }

    override suspend fun disableRecovery(): Result<Unit> = simulateLongTask {
        disableRecoveryFailure?.let { return Result.failure(it) }
        return Result.success(Unit)
    }

    override suspend fun fixRecoveryIssues(recoveryKey: String): Result<Unit> = simulateLongTask {
        fixRecoveryIssuesFailure?.let { return Result.failure(it) }
        return Result.success(Unit)
    }

    private var isLastDevice = false

    fun givenIsLastDevice(isLastDevice: Boolean) {
        this.isLastDevice = isLastDevice
    }

    override suspend fun isLastDevice(): Result<Boolean> {
        return Result.success(isLastDevice)
    }

    override suspend fun resetRecoveryKey(): Result<String> = simulateLongTask {
        return Result.success(fakeRecoveryKey)
    }

    override suspend fun enableRecovery(waitForBackupsToUpload: Boolean): Result<Unit> = simulateLongTask {
        return Result.success(Unit)
    }

    fun givenWaitForBackupUploadSteadyStateFlow(flow: Flow<BackupUploadState>) {
        waitForBackupUploadSteadyStateFlow = flow
    }

    override fun waitForBackupUploadSteadyState(): Flow<BackupUploadState> {
        return waitForBackupUploadSteadyStateFlow
    }

    suspend fun emitBackupState(state: BackupState) {
        backupStateStateFlow.emit(state)
    }

    suspend fun emitEnableRecoveryProgress(state: EnableRecoveryProgress) {
        enableRecoveryProgressStateFlow.emit(state)
    }

    companion object {
        const val fakeRecoveryKey = "fake"
    }
}
