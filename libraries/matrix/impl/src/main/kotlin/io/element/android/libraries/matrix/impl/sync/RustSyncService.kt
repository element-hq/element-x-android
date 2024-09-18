/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.sync

import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.sync.SyncState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import org.matrix.rustcomponents.sdk.SyncServiceState
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import org.matrix.rustcomponents.sdk.SyncService as InnerSyncService

class RustSyncService(
    private val innerSyncService: InnerSyncService,
    sessionCoroutineScope: CoroutineScope
) : SyncService {
    private val isServiceReady = AtomicBoolean(true)

    override suspend fun startSync() = runCatching {
        if (!isServiceReady.get()) {
            Timber.d("Can't start sync: service is not ready")
            return@runCatching
        }
        Timber.i("Start sync")
        innerSyncService.start()
    }.onFailure {
        Timber.d("Start sync failed: $it")
    }

    override suspend fun stopSync() = runCatching {
        if (!isServiceReady.get()) {
            Timber.d("Can't stop sync: service is not ready")
            return@runCatching
        }
        Timber.i("Stop sync")
        innerSyncService.stop()
    }.onFailure {
        Timber.d("Stop sync failed: $it")
    }

    suspend fun destroy() {
        // If the service was still running, stop it
        stopSync()
        Timber.d("Destroying sync service")
        isServiceReady.set(false)
        innerSyncService.destroy()
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
