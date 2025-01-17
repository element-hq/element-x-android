/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.di

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.services.appnavstate.api.AppForegroundStateService
import io.element.android.services.appnavstate.api.SyncOrchestrator
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class DefaultSyncOrchestrator @AssistedInject constructor(
    @Assisted matrixClient: MatrixClient,
    private val baseCoroutineScope: CoroutineScope = matrixClient.sessionCoroutineScope,
    private val appForegroundStateService: AppForegroundStateService,
    private val networkMonitor: NetworkMonitor,
    private val dispatchers: CoroutineDispatchers,
) : SyncOrchestrator {
    @AssistedFactory
    interface Factory {
        fun create(matrixClient: MatrixClient): DefaultSyncOrchestrator
    }

    private val syncService = matrixClient.syncService()

    private val initialSyncMutex = Mutex()

    private var coroutineScope: CoroutineScope? = null

    private val tag = "SyncOrchestrator"

    private val started = AtomicBoolean(false)

    /**
     * Starting observing the app state and network state to start/stop the sync service.
     *
     * Before observing the state, a first attempt at starting the sync service will happen if it's not already running.
     */
    @OptIn(FlowPreview::class)
    override fun start() {
        if (!started.compareAndSet(false, true)) {
            Timber.tag(tag).d("already started, exiting early")
            return
        }

        Timber.tag(tag).d("start observing the app and network state")

        if (syncService.syncState.value != SyncState.Running) {
            Timber.tag(tag).d("initial startSync")
            baseCoroutineScope.launch(dispatchers.io) {
                try {
                    initialSyncMutex.lock()
                    syncService.startSync()

                    // Wait until it's running
                    syncService.syncState.first { it == SyncState.Running }
                } finally {
                    initialSyncMutex.unlock()
                }
            }
        }

        coroutineScope = CoroutineScope(baseCoroutineScope.coroutineContext + CoroutineName(tag) + dispatchers.io)

        coroutineScope?.launch {
            // Wait until the initial sync is done, either successfully or failing
            initialSyncMutex.lock()

            combine(
                // small debounce to avoid spamming startSync when the state is changing quickly in case of error.
                syncService.syncState.debounce(100.milliseconds),
                networkMonitor.connectivity,
                appForegroundStateService.isInForeground,
                appForegroundStateService.isInCall,
                appForegroundStateService.isSyncingNotificationEvent,
            ) { syncState, networkState, isInForeground, isInCall, isSyncingNotificationEvent ->
                val isAppActive = isInForeground || isInCall || isSyncingNotificationEvent
                val isNetworkAvailable = networkState == NetworkStatus.Online

                Timber.tag(tag).d("isAppActive=$isAppActive, isNetworkAvailable=$isNetworkAvailable")
                if (syncState == SyncState.Running && (!isAppActive || !isNetworkAvailable)) {
                    // Don't stop the sync immediately, wait a bit to avoid starting/stopping the sync too often
                    delay(3.seconds)
                    SyncStateAction.StopSync
                } else if (syncState != SyncState.Running && isAppActive && isNetworkAvailable) {
                    SyncStateAction.StartSync
                } else {
                    SyncStateAction.NoOp
                }
            }
                .distinctUntilChanged()
                .collect { action ->
                    when (action) {
                        SyncStateAction.StartSync -> {
                            syncService.startSync()
                        }
                        SyncStateAction.StopSync -> {
                            syncService.stopSync()
                        }
                        SyncStateAction.NoOp -> Unit
                    }
                }
        }
    }

    /**
     * Stop observing the app state and network state.
     */
    override fun stop() {
        if (!started.compareAndSet(true, false)) {
            Timber.tag(tag).d("already stopped, exiting early")
            return
        }
        Timber.tag(tag).d("stop observing the app and network state")
        coroutineScope?.cancel()
        coroutineScope = null
    }
}

private enum class SyncStateAction {
    StartSync,
    StopSync,
    NoOp,
}
