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
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.push.impl.db.PushRequest
import io.element.android.libraries.push.impl.history.PushHistoryService
import io.element.android.services.appnavstate.api.AppForegroundStateService
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

private const val MAX_ROOMS_TO_SUBSCRIBE_TO = 20
private val SYNC_TIMEOUT = 10.seconds

/**
 * Not a singleton: the state shared between calls (throttling, syncing flag) lives in [OpportunisticSyncTracker].
 */
@ContributesBinding(AppScope::class)
class DefaultSyncOnNotifiableEvent(
    private val matrixClientProvider: MatrixClientProvider,
    private val featureFlagService: FeatureFlagService,
    private val appForegroundStateService: AppForegroundStateService,
    private val dispatchers: CoroutineDispatchers,
    private val pushHistoryService: PushHistoryService,
    private val opportunisticSyncTracker: OpportunisticSyncTracker,
) : SyncOnNotifiableEvent {
    override suspend operator fun invoke(requests: List<PushRequest>) = withContext(dispatchers.io) {
        if (!featureFlagService.isFeatureEnabled(FeatureFlags.SyncOnPush)) {
            return@withContext
        }

        if (requests.isEmpty()) {
            Timber.w("DefaultSyncOnNotifiableEvent called with no push requests to sync on")
            return@withContext
        }

        if (appForegroundStateService.isInForeground.value) {
            // The sync service is already running
            return@withContext
        }

        requests.groupBy { SessionId(it.sessionId) }
            .map { (sessionId, sessionRequests) ->
                launch {
                    // Don't sync the app too often, as it can be expensive and we don't want to waste battery.
                    opportunisticSyncTracker.syncIfAllowed(sessionId) {
                        Timber.d("Starting opportunistic room list sync for $sessionId")
                        syncUsingSession(sessionId, sessionRequests)
                    }
                }
            }
            .joinAll()
    }

    /**
     * @return true if the sync completed, false if it couldn't start, timed out or was ended early because a newer push is waiting.
     */
    private suspend fun syncUsingSession(sessionId: SessionId, requests: List<PushRequest>): Boolean = withTimeoutOrNull(SYNC_TIMEOUT) {
        val client = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return@withTimeoutOrNull false

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

        val (newPushQueued, _) = combine(newPushQueuedFlow, roomListSummariesFlow) { newPushes, newSummaries ->
            newPushes.isNotEmpty() to roomListHadRelevantChanges(currentRoomSummaries, newSummaries)
        }.first { (newPushQueued, roomListUpdated) -> newPushQueued || roomListUpdated }

        if (newPushQueued) {
            // Don't count it as a completed sync, so the work handling the newer push can sync again right away.
            Timber.d("A newer push is waiting to be fetched, ending the opportunistic sync early")
            false
        } else {
            Timber.d("The room list has been updated, opportunistic sync completed")
            true
        }
    } ?: false

    private fun roomListHadRelevantChanges(oldSummaries: List<RoomSummary>, newSummaries: List<RoomSummary>): Boolean {
        val oldSummariesById = oldSummaries.associateBy { it.roomId }
        val newSummariesById = newSummaries.associateBy { it.roomId }
        val oldRoomIds = oldSummariesById.keys
        val newRoomIds = newSummariesById.keys

        val allRoomIds = oldRoomIds union newRoomIds
        val unchangedRoomIds = oldRoomIds intersect newRoomIds
        val changedRoomIds = allRoomIds - unchangedRoomIds

        // If any room was added or removed, we consider it a relevant change.
        if (changedRoomIds.isNotEmpty()) {
            Timber.d("Room list changed, changed room ids: $changedRoomIds")
            return true
        }
        // If any room's summary changed, we consider it a relevant change too.
        for (roomId in unchangedRoomIds) {
            val oldSummary = oldSummariesById[roomId]
            val newSummary = newSummariesById[roomId]
            if (oldSummary != newSummary) {
                Timber.d("Room summary changed for room $roomId, old: $oldSummary, new: $newSummary")
                return true
            }
        }

        return false
    }
}
