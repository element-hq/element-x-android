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

package io.element.android.libraries.matrix.impl.sync

import io.element.android.libraries.matrix.api.sync.StartSyncReason
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.sync.SyncState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.matrix.rustcomponents.sdk.SyncServiceInterface
import org.matrix.rustcomponents.sdk.SyncServiceState
import timber.log.Timber

class RustSyncService(
    private val innerSyncService: SyncServiceInterface,
    sessionCoroutineScope: CoroutineScope
) : SyncService {
    private val mutex = Mutex()
    private val startSyncReasonSet = mutableSetOf<StartSyncReason>()

    override suspend fun startSync(reason: StartSyncReason): Result<Unit> {
        return mutex.withLock {
            startSyncReasonSet.add(reason)
            runCatching {
                Timber.d("Start sync")
                innerSyncService.start()
            }.onFailure {
                Timber.e("Start sync failed: $it")
            }
        }
    }

    override suspend fun stopSync(reason: StartSyncReason): Result<Unit> {
        return mutex.withLock {
            startSyncReasonSet.remove(reason)
            if (startSyncReasonSet.isEmpty()) {
                runCatching {
                    Timber.d("Stop sync")
                    innerSyncService.stop()
                }.onFailure {
                    Timber.e("Stop sync failed: $it")
                }
            } else {
                Timber.d("Stop sync skipped, still $startSyncReasonSet")
                Result.success(Unit)
            }
        }
    }

    override val syncState: StateFlow<SyncState> =
        innerSyncService.stateFlow()
            .map(SyncServiceState::toSyncState)
            .onEach { state ->
                Timber.i("Sync state=$state")
            }
            .distinctUntilChanged()
            .stateIn(sessionCoroutineScope, SharingStarted.Eagerly, SyncState.Idle)
}
