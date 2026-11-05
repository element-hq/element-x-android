/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.impl.db.PushRequest
import io.element.android.libraries.push.impl.history.PushHistoryService
import io.element.android.services.appnavstate.api.AppForegroundStateService
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private const val MAX_ROOMS_TO_SUBSCRIBE_TO = 20
private const val SYNC_TIMEOUT_SECONDS = 10
private const val MIN_INTERVAL_BETWEEN_SYNC_SECONDS = 60

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultSyncOnNotifiableEvent(
    private val matrixClientProvider: MatrixClientProvider,
    private val featureFlagService: FeatureFlagService,
    private val appForegroundStateService: AppForegroundStateService,
    private val dispatchers: CoroutineDispatchers,
    private val pushHistoryService: PushHistoryService,
    private val systemClock: SystemClock,
) : SyncOnNotifiableEvent {
    private val lastSyncOnPushTimestamp = AtomicLong(0L)

    override suspend operator fun invoke(requests: List<PushRequest>) = withContext(dispatchers.io) {
        if (!featureFlagService.isFeatureEnabled(FeatureFlags.SyncOnPush)) {
            return@withContext
        }

        if (requests.isEmpty()) {
            Timber.w("DefaultSyncOnNotifiableEvent called with no push requests to sync on")
            return@withContext
        }

        val start = Instant.fromEpochMilliseconds(systemClock.epochMillis())

        val syncJobs = mutableListOf<Job>()
        try {
            if (!appForegroundStateService.isInForeground.value) {
                // Don't sync the app too often, as it can be expensive and we don't want to waste battery.
                if (start.toEpochMilliseconds() - lastSyncOnPushTimestamp.get() < MIN_INTERVAL_BETWEEN_SYNC_SECONDS * 1000) {
                    Timber.d("Skipping opportunistic sync because the last one was too recent")
                    return@withContext
                }

                lastSyncOnPushTimestamp.set(start.toEpochMilliseconds())
                appForegroundStateService.updateIsSyncingNotificationEvent(true)

                val requestsBySessionId = requests.groupBy { it.sessionId }

                for ((sessionId, requests) in requestsBySessionId) {
                    Timber.d("Starting opportunistic room list sync for $sessionId")
                    syncJobs += launch {
                        syncUsingSession(SessionId(sessionId), requests)
                    }
                }

                syncJobs.joinAll()
            }
        } finally {
            if (syncJobs.isNotEmpty()) {
                val end = Instant.fromEpochMilliseconds(systemClock.epochMillis())
                Timber.d("Finished opportunistic room list syncs in ${end - start}")
            }
            appForegroundStateService.updateIsSyncingNotificationEvent(false)
        }
    }

    private suspend fun syncUsingSession(sessionId: SessionId, requests: List<PushRequest>) = withTimeoutOrNull(SYNC_TIMEOUT_SECONDS.seconds) {
        if (requests.isEmpty()) return@withTimeoutOrNull

        val client = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return@withTimeoutOrNull

        val roomListSummariesFlow = client.roomListService.allRooms.summaries
        val mostRecentRoomIds = roomListSummariesFlow.first { it.isNotEmpty() }
            .take(MAX_ROOMS_TO_SUBSCRIBE_TO)
            .map { it.roomId }
        client.roomListService.subscribeToVisibleRooms(mostRecentRoomIds)

        // Give the sync some time to complete in background, but stop early if a newer push is waiting: the work fetching it can only
        // start once this one is done, and an incoming call can't wait that long to ring.
        // Pushes handled by this work and pending again after a network error keep their push date, so they don't end it early.
        val newestHandledPushDate = Instant.fromEpochMilliseconds(requests.maxOf { it.pushDate })

        // Let's wait for either a new push to be queued or the room list to be updated, whichever happens first.
        // If a new push is queued, we can end the opportunistic sync early.
        val currentRoomSummaries = roomListSummariesFlow.first()
        val newPushQueuedFlow = pushHistoryService.getPendingPushRequestsFlow(sessionId, since = newestHandledPushDate).onStart { emit(emptyList()) }

        val (newPushQueued, updatedRoomList) = combine(newPushQueuedFlow, roomListSummariesFlow) { newPushes, newSummaries ->
            newPushes.isNotEmpty() to (newSummaries != currentRoomSummaries)
        }.first { (newPushQueued, roomListUpdated) -> newPushQueued || roomListUpdated }

        if (newPushQueued) {
            Timber.d("A newer push is waiting to be fetched, ending the opportunistic sync early")
            return@withTimeoutOrNull
        } else if (updatedRoomList) {
            Timber.d("The room list has been updated, opportunistic sync completed")
        }
    }
}
