/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.workmanager.api

import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.Flow

/**
 * Schedules background work, wrapping `WorkManager` so the rest of the app does not depend on it directly.
 */
interface WorkManagerScheduler {
    /**
     * Submits a new work request built from [workManagerRequestBuilder] to run in `WorkManager`.
     *
     * @param workManagerRequestBuilder describes the work to schedule.
     */
    suspend fun submit(workManagerRequestBuilder: WorkManagerRequestBuilder)

    /**
     * Checks if there are any pending requests scheduled for the provided [sessionId] and [requestType].
     *
     * @param sessionId the session the work belongs to.
     * @param requestType the kind of work to look for.
     */
    fun hasPendingWork(sessionId: SessionId, requestType: WorkManagerRequestType): Boolean

    /**
     * Emits the aggregated activity of the work tagged for [sessionId] and [requestType], live.
     * Unlike [hasPendingWork] this is observable, so UI can follow a worker through
     * enqueued → running → gone.
     */
    fun workStateFlow(sessionId: SessionId, requestType: WorkManagerRequestType): Flow<WorkActivityState>

    /**
     * Cancel pending work requests for the session [SessionId].
     * If [requestType] is provided, it will only cancel requests for that type, otherwise it will cancel all requests.
     *
     * @param sessionId the session whose work is cancelled.
     * @param requestType the kind of work to cancel, or `null` to cancel every kind.
     */
    fun cancel(sessionId: SessionId, requestType: WorkManagerRequestType? = null)
}

/**
 * Coarse activity of a set of tagged work, aggregated across requests: [RUNNING] wins over
 * [ENQUEUED], which wins over [NONE]. Finished (succeeded/failed/cancelled) work counts as [NONE].
 */
enum class WorkActivityState {
    NONE,
    ENQUEUED,
    RUNNING,
}

fun workManagerTag(sessionId: SessionId, requestType: WorkManagerRequestType): String {
    val prefix = when (requestType) {
        WorkManagerRequestType.NOTIFICATION_SYNC -> "notifications"
        WorkManagerRequestType.DB_VACUUM -> "db_vacuum"
        WorkManagerRequestType.SEARCH_BACKFILL -> "search_backfill"
        WorkManagerRequestType.SEARCH_BACKFILL_USER -> "search_backfill_user"
    }
    return "$prefix-$sessionId"
}

enum class WorkManagerRequestType {
    NOTIFICATION_SYNC,
    DB_VACUUM,

    /**
     * Back-paginates rooms so their history reaches the local message search index.
     *
     * Work of this type keeps fetching history in the background, so it MUST be cancelled when the
     * session goes away — see the cancel-by-tag test. Any request builder for this type has to call
     * `addTag(workManagerTag(sessionId, SEARCH_BACKFILL))`; nothing enforces that at compile time.
     */
    SEARCH_BACKFILL,

    /**
     * Marker tag carried *in addition to* [SEARCH_BACKFILL] by sweeps the user explicitly started
     * from developer settings. It exists purely so UI can observe the user's own sweep without the
     * always-enqueued background one drowning it out; the actual work request is the same worker
     * under the same unique name. Never enqueue work carrying only this tag: [SEARCH_BACKFILL] is
     * what logout cancellation and `hasPendingWork` key on.
     */
    SEARCH_BACKFILL_USER,
}
