/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.sync

import io.element.android.libraries.core.coroutine.mapState
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.sync.SyncState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.SyncServiceState
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import org.matrix.rustcomponents.sdk.SyncService as InnerSyncService

class RustSyncService(
    private val inner: InnerSyncService,
    private val dispatcher: CoroutineDispatcher,
    sessionCoroutineScope: CoroutineScope,
) : SyncService {
    private val isServiceReady = AtomicBoolean(true)

    override suspend fun startSync() = withContext(dispatcher) {
        runCatchingExceptions {
            if (!isServiceReady.get()) {
                Timber.d("Can't start sync: service is not ready")
                return@runCatchingExceptions
            }
            Timber.i("Start sync")
            inner.start()
        }.onFailure {
            Timber.d("Start sync failed: $it")
        }
    }

    override suspend fun stopSync() = withContext(dispatcher) {
        runCatchingExceptions {
            if (!isServiceReady.get()) {
                Timber.d("Can't stop sync: service is not ready")
                return@runCatchingExceptions
            }
            Timber.i("Stop sync")
            inner.stop()
        }.onFailure {
            Timber.d("Stop sync failed: $it")
        }
    }

    suspend fun destroy() = withContext(NonCancellable) {
        // If the service was still running, stop it
        stopSync()
        Timber.d("Destroying sync service")
        isServiceReady.set(false)
        inner.destroy()
    }

    override val syncState: StateFlow<SyncState> =
        inner.stateFlow()
            .map(SyncServiceState::toSyncState)
            .distinctUntilChanged()
            .onEach { state ->
                Timber.i("Sync state=$state")
            }
            .stateIn(sessionCoroutineScope, SharingStarted.Eagerly, SyncState.Idle)

    override val isOnline: StateFlow<Boolean> = syncState.mapState { it != SyncState.Offline }
}
