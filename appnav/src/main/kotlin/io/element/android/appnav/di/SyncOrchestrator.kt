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
import io.element.android.libraries.core.coroutine.childScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.services.appnavstate.api.AppForegroundStateService
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SyncOrchestrator @AssistedInject constructor(
    @Assisted matrixClient: MatrixClient,
    private val appForegroundStateService: AppForegroundStateService,
    private val networkMonitor: NetworkMonitor,
    dispatchers: CoroutineDispatchers,
) {
    @AssistedFactory
    interface Factory {
        fun create(matrixClient: MatrixClient): SyncOrchestrator
    }

    private val syncService = matrixClient.syncService()

    private val tag = "SyncOrchestrator"

    private val coroutineScope = matrixClient.sessionCoroutineScope.childScope(dispatchers.io, tag)

    private val started = AtomicBoolean(false)

    /**
     * Starting observing the app state and network state to start/stop the sync service.
     *
     * Before observing the state, a first attempt at starting the sync service will happen if it's not already running.
     */
    @OptIn(FlowPreview::class)
    fun start() {
        if (!started.compareAndSet(false, true)) {
            Timber.tag(tag).d("already started, exiting early")
            return
        }

        Timber.tag(tag).d("start observing the app and network state")

        combine(
            // small debounce to avoid spamming startSync when the state is changing quickly in case of error.
            syncService.syncState.debounce(100.milliseconds),
            networkMonitor.connectivity,
            appForegroundStateService.isInForeground,
            appForegroundStateService.isInCall,
            appForegroundStateService.isSyncingNotificationEvent,
        ) { syncState, networkState, isInForeground, isInCall, isSyncingNotificationEvent ->
            val isAppActive = isInForeground || isInCall || isSyncingNotificationEvent
            val isNetworkAvailable = networkState == NetworkStatus.Connected

            Timber.tag(tag).d("isAppActive=$isAppActive, isNetworkAvailable=$isNetworkAvailable")
            if (syncState == SyncState.Running && !isAppActive) {
                SyncStateAction.StopSync
            } else if (syncState != SyncState.Running && isAppActive && isNetworkAvailable) {
                SyncStateAction.StartSync
            } else {
                SyncStateAction.NoOp
            }
        }
            .distinctUntilChanged()
            .debounce { action ->
                // Don't stop the sync immediately, wait a bit to avoid starting/stopping the sync too often
                if (action == SyncStateAction.StopSync) 3.seconds else 0.seconds
            }
            .onEach { action ->
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
            .onCompletion {
                Timber.tag(tag).d("has been stopped")
            }
            .launchIn(coroutineScope)
    }
}

private enum class SyncStateAction {
    StartSync,
    StopSync,
    NoOp,
}
