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
import io.element.android.libraries.core.extensions.mapFailure
import io.element.android.libraries.matrix.api.encryption.BackupState
import io.element.android.libraries.matrix.api.encryption.BackupUploadState
import io.element.android.libraries.matrix.api.encryption.EnableRecoveryProgress
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.matrix.impl.sync.RustSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
    syncService: RustSyncService,
    sessionCoroutineScope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
) : EncryptionService {

    private val service: Encryption = client.encryption()

    private val backupStateMapper = BackupStateMapper()
    private val recoveryStateMapper = RecoveryStateMapper()
    private val enableRecoveryProgressMapper = EnableRecoveryProgressMapper()
    private val backupUploadStateMapper = BackupUploadStateMapper()
    private val steadyStateExceptionMapper = SteadyStateExceptionMapper()

    private val backupStateFlow = MutableStateFlow(service.backupState().let(backupStateMapper::map))

    override val backupStateStateFlow = combine(
        backupStateFlow,
        syncService.syncState,
    ) { backupState, syncState ->
        if (syncState == SyncState.Running) {
            backupState
        } else {
            BackupState.WAITING_FOR_SYNC
        }
    }.stateIn(sessionCoroutineScope, SharingStarted.Eagerly, BackupState.WAITING_FOR_SYNC)

    private val recoveryStateFlow: MutableStateFlow<RecoveryState> = MutableStateFlow(service.recoveryState().let(recoveryStateMapper::map))

    override val recoveryStateStateFlow = combine(
        recoveryStateFlow,
        syncService.syncState,
    ) { recoveryState, syncState ->
        if (syncState == SyncState.Running) {
            recoveryState
        } else {
            RecoveryState.WAITING_FOR_SYNC
        }
    }.stateIn(sessionCoroutineScope, SharingStarted.Eagerly, RecoveryState.WAITING_FOR_SYNC)

    override val enableRecoveryProgressStateFlow: MutableStateFlow<EnableRecoveryProgress> = MutableStateFlow(EnableRecoveryProgress.Starting)

    fun start() {
        service.backupStateListener(object : BackupStateListener {
            override fun onUpdate(status: RustBackupState) {
                backupStateFlow.value = backupStateMapper.map(status)
            }
        })

        service.recoveryStateListener(object : RecoveryStateListener {
            override fun onUpdate(status: RustRecoveryState) {
                recoveryStateFlow.value = recoveryStateMapper.map(status)
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
        }.mapFailure {
            it.mapRecoveryException()
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
        }.mapFailure {
            it.mapRecoveryException()
        }
    }

    override suspend fun doesBackupExistOnServer(): Result<Boolean> = withContext(dispatchers.io) {
        runCatching {
            service.backupExistsOnServer()
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
        }.mapFailure {
            it.mapRecoveryException()
        }
    }

    override suspend fun isLastDevice(): Result<Boolean> = withContext(dispatchers.io) {
        runCatching {
            service.isLastDevice()
        }.mapFailure {
            it.mapRecoveryException()
        }
    }

    override suspend fun resetRecoveryKey(): Result<String> = withContext(dispatchers.io) {
        runCatching {
            service.resetRecoveryKey()
        }.mapFailure {
            it.mapRecoveryException()
        }
    }

    override suspend fun recover(recoveryKey: String): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            service.recover(recoveryKey)
        }.mapFailure {
            it.mapRecoveryException()
        }
    }
}
