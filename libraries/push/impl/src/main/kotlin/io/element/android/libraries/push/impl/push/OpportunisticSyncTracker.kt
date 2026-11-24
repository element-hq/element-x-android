/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.services.appnavstate.api.AppForegroundStateService
import io.element.android.services.toolbox.api.systemclock.SystemClock
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Minimum time between 2 completed opportunistic syncs of the same session.
 */
internal val MIN_INTERVAL_BETWEEN_SYNC = 60.seconds

/**
 * Keeps the state of the opportunistic syncs done on push, shared by all the [DefaultSyncOnNotifiableEvent] instances.
 *
 * - The throttling is done per session, so a sync for a session doesn't prevent another session from syncing.
 * - Only one sync can run at a time for a given session.
 * - [AppForegroundStateService.isSyncingNotificationEvent] stays `true` while at least one sync is running, whatever the session.
 */
@SingleIn(AppScope::class)
@Inject
class OpportunisticSyncTracker(
    private val appForegroundStateService: AppForegroundStateService,
    private val systemClock: SystemClock,
) {
    // All the state is read and updated under this lock, so concurrent callers can't both start a sync for the same session
    // or toggle the syncing flag out of order.
    private val lock = Any()
    private val lastCompletedSyncs = mutableMapOf<SessionId, Instant>()
    private val ongoingSyncs = mutableSetOf<SessionId>()

    /**
     * Runs [sync] for [sessionId], unless a sync is already running for this session or the last one completed too recently.
     * @param sessionId the session to sync.
     * @param sync returns true if the sync completed. If it didn't (error, timeout, ended early), the next push can sync again right away.
     */
    suspend fun syncIfAllowed(sessionId: SessionId, sync: suspend () -> Boolean) {
        if (!tryStart(sessionId)) {
            Timber.d("Skipping opportunistic sync for $sessionId: one is already running or the last one was too recent")
            return
        }

        var completedAt: Instant? = null
        try {
            if (sync()) {
                completedAt = now()
            }
        } finally {
            finish(sessionId, completedAt)
        }
    }

    private fun tryStart(sessionId: SessionId): Boolean = synchronized(lock) {
        if (sessionId in ongoingSyncs) return false
        val lastCompletedSync = lastCompletedSyncs[sessionId]

        // A negative elapsed time means the wall clock went backwards: don't block the syncs until it catches up.
        val invalidBetweenSyncsInterval = Duration.ZERO..<MIN_INTERVAL_BETWEEN_SYNC
        if (lastCompletedSync != null && elapsedTimeSinceLastSync(lastCompletedSync) in invalidBetweenSyncsInterval) return false

        ongoingSyncs += sessionId
        if (ongoingSyncs.size == 1) {
            appForegroundStateService.updateIsSyncingNotificationEvent(true)
        }
        true
    }

    private fun finish(sessionId: SessionId, completedAt: Instant?) = synchronized(lock) {
        ongoingSyncs -= sessionId
        if (completedAt != null) {
            lastCompletedSyncs[sessionId] = completedAt
        }
        if (ongoingSyncs.isEmpty()) {
            appForegroundStateService.updateIsSyncingNotificationEvent(false)
        }
    }

    private fun now() = Instant.fromEpochMilliseconds(systemClock.epochMillis())

    private fun elapsedTimeSinceLastSync(lastCompletedSyncTime: Instant): Duration = now() - lastCompletedSyncTime
}
