/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.session

import androidx.annotation.VisibleForTesting
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.coroutine.childScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.api.recordTransaction
import io.element.android.services.analyticsproviders.api.AnalyticsUserData
import io.element.android.services.appnavstate.api.AppForegroundStateService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class SyncOrchestrator(
    @Assisted private val matrixClient: MatrixClient,
    @Assisted sessionCoroutineScope: CoroutineScope,
    private val appForegroundStateService: AppForegroundStateService,
    private val networkMonitor: NetworkMonitor,
    dispatchers: CoroutineDispatchers,
    private val analyticsService: AnalyticsService,
) {
    @AssistedFactory
    interface Factory {
        fun create(
            matrixClient: MatrixClient,
            sessionCoroutineScope: CoroutineScope,
        ): SyncOrchestrator
    }

    private val tag = "SyncOrchestrator"

    private val syncService = matrixClient.syncService

    private val coroutineScope = sessionCoroutineScope.childScope(dispatchers.io, tag)

    private val started = AtomicBoolean(false)

    /**
     * Number of consecutive attempts at restarting a sync service which stopped on its own.
     * Reset once the sync service has been running for [RESTART_BACKOFF_RESET_DELAY].
     */
    private val restartAttempt = AtomicInteger(0)

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
                transaction.putIndexableData(AnalyticsUserData.FIRST_SYNC_STATE, firstState.name)
            }

            observeStates()
        }
    }

    @OptIn(FlowPreview::class)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun observeStates() = coroutineScope.launch {
        Timber.tag(tag).d("start observing the app and network state")
        // Reset the restart backoff once the sync service has been steadily running for a while. Note that
        // SyncService.startSync() moves the state to Running right away, so resetting on any Running value
        // would defeat the backoff when the sync service dies again immediately after each restart.
        syncService.syncState
            .debounce(RESTART_BACKOFF_RESET_DELAY)
            .onEach { syncState ->
                if (syncState == SyncState.Running) {
                    restartAttempt.set(0)
                }
            }
            .launchIn(this)
        val isAppActiveFlows = listOf(
            appForegroundStateService.isInForeground,
            appForegroundStateService.isInCall,
            appForegroundStateService.isSyncingNotificationEvent,
            appForegroundStateService.hasRingingCall,
            appForegroundStateService.isSharingLiveLocation,
        )
        val isAppActiveFlow = combine(isAppActiveFlows) { actives -> actives.any { it } }
        combine(
            // small debounce to avoid spamming startSync when the state is changing quickly in case of error.
            syncService.syncState.debounce(100.milliseconds),
            networkMonitor.connectivity,
            isAppActiveFlow,
        ) { syncState, networkState, isAppActive ->
            if (matrixClient.isShuttingDown) {
                Timber.tag(tag).d("Matrix client is shutting down, no need to start the sync service again")
                return@combine SyncStateAction.NoOp
            }

            val isNetworkAvailable = networkState == NetworkStatus.Connected

            Timber.tag(tag).d("isAppActive=$isAppActive, isNetworkAvailable=$isNetworkAvailable")
            if (syncState == SyncState.Running && !isAppActive) {
                SyncStateAction.StopSync
            } else if (syncState == SyncState.Idle && isAppActive && isNetworkAvailable) {
                SyncStateAction.StartSync
            } else if (syncState.isDead() && isAppActive && isNetworkAvailable) {
                // The sync service has given up, for instance because the homeserver has expired our sliding sync
                // session and answered 400 M_UNKNOWN_POS. Nothing else will ever restart it, so do it from here,
                // else the application is stuck until its data is cleared.
                SyncStateAction.RestartSync
            } else {
                SyncStateAction.NoOp
            }
        }
            .distinctUntilChanged()
            .debounce { action ->
                when (action) {
                    // Don't stop the sync immediately, wait a bit to avoid starting/stopping the sync too often
                    SyncStateAction.StopSync -> 3.seconds
                    // Back off before restarting a dead sync service, to avoid hammering a failing homeserver.
                    // A state change in the meantime cancels the pending restart.
                    SyncStateAction.RestartSync -> restartDelay(restartAttempt.get())
                    SyncStateAction.StartSync,
                    SyncStateAction.NoOp -> 0.seconds
                }
            }
            .onCompletion {
                Timber.tag(tag).d("has been stopped")
            }
            .collect { action ->
                when (action) {
                    SyncStateAction.StartSync -> {
                        syncService.startSync()
                    }
                    SyncStateAction.RestartSync -> {
                        val attempt = restartAttempt.incrementAndGet()
                        Timber.tag(tag).w("sync service is dead, restarting it (attempt $attempt)")
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

@VisibleForTesting
internal val FIRST_RESTART_DELAY = 1.seconds

@VisibleForTesting
internal val MAX_RESTART_DELAY = 30.seconds

/**
 * Duration during which the sync service has to stay running before the restart backoff is reset.
 */
@VisibleForTesting
internal val RESTART_BACKOFF_RESET_DELAY = 30.seconds

/**
 * Return true if the sync service has stopped and will not restart by itself.
 */
private fun SyncState.isDead() = this == SyncState.Error || this == SyncState.Terminated

/**
 * Exponential backoff, from [FIRST_RESTART_DELAY], doubling on every attempt, up to [MAX_RESTART_DELAY].
 */
private fun restartDelay(attempt: Int): Duration {
    val exponent = attempt.coerceIn(0, 5)
    return (FIRST_RESTART_DELAY * (1 shl exponent)).coerceAtMost(MAX_RESTART_DELAY)
}

private enum class SyncStateAction {
    StartSync,
    RestartSync,
    StopSync,
    NoOp,
}
