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
import io.element.android.libraries.matrix.api.encryption.IdentityResetHandle
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeEncryptionService(
    var startIdentityResetLambda: () -> Result<IdentityResetHandle?> = { lambdaError() },
) : EncryptionService {
    private var disableRecoveryFailure: Exception? = null
    override val backupStateStateFlow: MutableStateFlow<BackupState> = MutableStateFlow(BackupState.UNKNOWN)
    override val recoveryStateStateFlow: MutableStateFlow<RecoveryState> = MutableStateFlow(RecoveryState.UNKNOWN)
    override val enableRecoveryProgressStateFlow: MutableStateFlow<EnableRecoveryProgress> = MutableStateFlow(EnableRecoveryProgress.Starting)
    override val isLastDevice: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var waitForBackupUploadSteadyStateFlow: Flow<BackupUploadState> = flowOf()

    private var recoverFailure: Exception? = null
    private var doesBackupExistOnServerResult: Result<Boolean> = Result.success(true)

    private var enableBackupsFailure: Exception? = null

    private var curve25519: String? = null
    private var ed25519: String? = null

    fun givenEnableBackupsFailure(exception: Exception?) {
        enableBackupsFailure = exception
    }

    override suspend fun enableBackups(): Result<Unit> = simulateLongTask {
        enableBackupsFailure?.let { return Result.failure(it) }
        return Result.success(Unit)
    }

    fun givenDisableRecoveryFailure(exception: Exception) {
        disableRecoveryFailure = exception
    }

    fun givenRecoverFailure(exception: Exception?) {
        recoverFailure = exception
    }

    override suspend fun disableRecovery(): Result<Unit> = simulateLongTask {
        disableRecoveryFailure?.let { return Result.failure(it) }
        return Result.success(Unit)
    }

    fun givenDoesBackupExistOnServerResult(result: Result<Boolean>) {
        doesBackupExistOnServerResult = result
    }

    override suspend fun doesBackupExistOnServer(): Result<Boolean> = simulateLongTask {
        return doesBackupExistOnServerResult
    }

    override suspend fun recover(recoveryKey: String): Result<Unit> = simulateLongTask {
        recoverFailure?.let { return Result.failure(it) }
        return Result.success(Unit)
    }

    fun emitIsLastDevice(isLastDevice: Boolean) {
        this.isLastDevice.value = isLastDevice
    }

    override suspend fun resetRecoveryKey(): Result<String> = simulateLongTask {
        return Result.success(FAKE_RECOVERY_KEY)
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

    fun givenDeviceKeys(curve25519: String?, ed25519: String?) {
        this.curve25519 = curve25519
        this.ed25519 = ed25519
    }

    override suspend fun deviceCurve25519(): String? = curve25519

    override suspend fun deviceEd25519(): String? = ed25519

    suspend fun emitBackupState(state: BackupState) {
        backupStateStateFlow.emit(state)
    }

    suspend fun emitRecoveryState(state: RecoveryState) {
        recoveryStateStateFlow.emit(state)
    }

    suspend fun emitEnableRecoveryProgress(state: EnableRecoveryProgress) {
        enableRecoveryProgressStateFlow.emit(state)
    }

    override suspend fun startIdentityReset(): Result<IdentityResetHandle?> {
        return startIdentityResetLambda()
    }

    companion object {
        const val FAKE_RECOVERY_KEY = "fake"
    }
}
