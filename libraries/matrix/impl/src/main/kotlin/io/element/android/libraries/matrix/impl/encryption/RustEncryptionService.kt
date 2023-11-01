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

package io.element.android.libraries.matrix.impl.encryption

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.BackupUploadState
import io.element.android.libraries.matrix.api.encryption.EnableRecoveryProgress
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.BackupStateListener
import org.matrix.rustcomponents.sdk.BackupSteadyStateListener
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.EnableRecoveryProgressListener
import org.matrix.rustcomponents.sdk.Encryption
import org.matrix.rustcomponents.sdk.RecoveryStateListener
import org.matrix.rustcomponents.sdk.BackupState as RustBackupState
import org.matrix.rustcomponents.sdk.BackupUploadState as RustBackupUploadState
import org.matrix.rustcomponents.sdk.EnableRecoveryProgress as RustEnableRecoveryProgress
import org.matrix.rustcomponents.sdk.RecoveryState as RustRecoveryState
import org.matrix.rustcomponents.sdk.SteadyStateException as RustSteadyStateException

internal class RustEncryptionService(
    client: Client,
    private val dispatchers: CoroutineDispatchers,
) : EncryptionService {

    private val service: Encryption = client.encryption()

    private val backupStateMapper = BackupStateMapper()
    private val recoveryStateMapper = RecoveryStateMapper()
    private val enableRecoveryProgressMapper = EnableRecoveryProgressMapper()
    private val backupUploadStateMapper = BackupUploadStateMapper()
    private val steadyStateExceptionMapper = SteadyStateExceptionMapper()

    override val backupStateStateFlow: MutableStateFlow<BackupState> = MutableStateFlow(service.backupState().let(backupStateMapper::map))
    override val recoveryStateStateFlow: MutableStateFlow<RecoveryState> = MutableStateFlow(service.recoveryState().let(recoveryStateMapper::map))
    override val enableRecoveryProgressStateFlow: MutableStateFlow<EnableRecoveryProgress> = MutableStateFlow(EnableRecoveryProgress.Unknown)

    fun start() {
        service.backupStateListener(object : BackupStateListener {
            override fun onUpdate(status: RustBackupState) {
                backupStateStateFlow.value = backupStateMapper.map(status)
            }
        })

        service.recoveryStateListener(object : RecoveryStateListener {
            override fun onUpdate(status: RustRecoveryState) {
                recoveryStateStateFlow.value = recoveryStateMapper.map(status)
            }
        })
    }

    fun destroy() {
        // No way to remove the listeners...
        service.destroy()
    }

    override suspend fun enableBackups(): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            service.enableBackups()
        }
    }

    override suspend fun enableRecovery(
        waitForBackupsToUpload: Boolean,
    ): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            service.enableRecovery(
                waitForBackupsToUpload = waitForBackupsToUpload,
                progressListener = object : EnableRecoveryProgressListener {
                    override fun onUpdate(status: RustEnableRecoveryProgress) {
                        enableRecoveryProgressStateFlow.value = enableRecoveryProgressMapper.map(status)
                    }
                }
            )
                // enableRecovery returns the encryption key, but we read it from the state flow
                .let { }
        }
    }

    override fun waitForBackupUploadSteadyState(): Flow<BackupUploadState> {
        return callbackFlow {
            runCatching {
                service.waitForBackupUploadSteadyState(
                    progressListener = object : BackupSteadyStateListener {
                        override fun onUpdate(status: RustBackupUploadState) {
                            trySend(backupUploadStateMapper.map(status))
                            if (status == RustBackupUploadState.Done) {
                                close()
                            }
                        }
                    }
                )
            }.onFailure {
                if (it is RustSteadyStateException) {
                    trySend(BackupUploadState.SteadyException(steadyStateExceptionMapper.map(it)))
                } else {
                    trySend(BackupUploadState.Error)
                }
                close()
            }
            awaitClose {}
        }
    }

    override suspend fun disableRecovery(): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            service.disableRecovery()
        }
    }

    override suspend fun isLastDevice(): Result<Boolean> = withContext(dispatchers.io) {
        runCatching {
            service.isLastDevice()
        }
    }

    override suspend fun resetRecoveryKey(): Result<String> = withContext(dispatchers.io) {
        runCatching {
            service.resetRecoveryKey()
        }
    }

    override suspend fun fixRecoveryIssues(recoveryKey: String): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            service.fixRecoveryIssues(recoveryKey)
        }
    }
}
