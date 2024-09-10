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
import org.matrix.rustcomponents.sdk.SyncServiceInterface
import org.matrix.rustcomponents.sdk.SyncServiceState
import timber.log.Timber

class RustSyncService(
    private val innerSyncService: SyncServiceInterface,
    sessionCoroutineScope: CoroutineScope
) : SyncService {
    override suspend fun startSync() = runCatching {
        Timber.i("Start sync")
        innerSyncService.start()
    }.onFailure {
        Timber.d("Start sync failed: $it")
    }

    override suspend fun stopSync() = runCatching {
        Timber.i("Stop sync")
        innerSyncService.stop()
    }.onFailure {
        Timber.d("Stop sync failed: $it")
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
