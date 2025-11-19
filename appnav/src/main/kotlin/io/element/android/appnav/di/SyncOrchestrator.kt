/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.di

import androidx.annotation.VisibleForTesting
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.coroutine.childScope
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.api.recordTransaction
import io.element.android.services.appnavstate.api.AppForegroundStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class SyncOrchestrator(
    @Assisted private val syncService: SyncService,
    @Assisted sessionCoroutineScope: CoroutineScope,
    private val appForegroundStateService: AppForegroundStateService,
    private val networkMonitor: NetworkMonitor,
    dispatchers: CoroutineDispatchers,
    private val analyticsService: AnalyticsService,
) {
    @AssistedFactory
    interface Factory {
        fun create(
            syncService: SyncService,
            sessionCoroutineScope: CoroutineScope,
        ): SyncOrchestrator
    }

    private val tag = "SyncOrchestrator"

    private val coroutineScope = sessionCoroutineScope.childScope(dispatchers.io, tag)

    private val started = AtomicBoolean(false)

    /**
     * Starting observing the app state and network state to start/stop the sync service.
     *
     * Before observing the state, a first attempt at starting the sync service will happen if it's not already running.
     */
    fun start() {
        if (!started.compareAndSet(false, true)) {
            Timber.tag(tag).d("already started, exiting early")
            return
        }

        coroutineScope.launch {
            // Perform an initial sync if the sync service is not running, to check whether the homeserver is accessible
            // Otherwise, if the device is offline the sync service will never start and the SyncState will be Idle, not Offline
            Timber.tag(tag).d("performing initial sync attempt")
            analyticsService.recordTransaction("First sync", "syncService.startSync()") { transaction ->
                syncService.startSync()

                // Wait until the sync service is not idle, either it will be running or in error/offline state
                val firstState = syncService.syncState.first { it != SyncState.Idle }
                transaction.setData("first_sync_state", firstState.name)
            }

            observeStates()
        }
    }

    @OptIn(FlowPreview::class)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun observeStates() = coroutineScope.launch {
        Timber.tag(tag).d("start observing the app and network state")

        val isAppActiveFlow = combine(
            appForegroundStateService.isInForeground,
            appForegroundStateService.isInCall,
            appForegroundStateService.isSyncingNotificationEvent,
            appForegroundStateService.hasRingingCall,
        ) { isInForeground, isInCall, isSyncingNotificationEvent, hasRingingCall ->
            isInForeground || isInCall || isSyncingNotificationEvent || hasRingingCall
        }

        combine(
            // small debounce to avoid spamming startSync when the state is changing quickly in case of error.
            syncService.syncState.debounce(100.milliseconds),
            networkMonitor.connectivity,
            isAppActiveFlow,
        ) { syncState, networkState, isAppActive ->
            val isNetworkAvailable = networkState == NetworkStatus.Connected

            Timber.tag(tag).d("isAppActive=$isAppActive, isNetworkAvailable=$isNetworkAvailable")
            if (syncState == SyncState.Running && !isAppActive) {
                SyncStateAction.StopSync
            } else if (syncState == SyncState.Idle && isAppActive && isNetworkAvailable) {
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
            .onCompletion {
                Timber.tag(tag).d("has been stopped")
            }
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

private enum class SyncStateAction {
    StartSync,
    StopSync,
    NoOp,
}
