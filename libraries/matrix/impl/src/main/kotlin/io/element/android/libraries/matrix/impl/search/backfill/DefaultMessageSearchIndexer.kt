/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search.backfill

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.search.MessageSearchIndexer
import io.element.android.libraries.matrix.api.search.MessageSearchSweepActivity
import io.element.android.libraries.matrix.api.search.SearchBackfillCursor
import io.element.android.libraries.workmanager.api.WorkActivityState
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.WorkManagerScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import timber.log.Timber

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultMessageSearchIndexer(
    private val storeHolder: SearchBackfillStoreHolder,
    private val workManagerScheduler: WorkManagerScheduler,
) : MessageSearchIndexer {
    override fun cursorFlow(sessionId: SessionId): Flow<SearchBackfillCursor?> {
        return storeHolder.storeFor(sessionId).cursorFlow()
    }

    override fun userSweepActivityFlow(sessionId: SessionId): Flow<MessageSearchSweepActivity> {
        // Two tags, two meanings. RUNNING comes from the shared tag: a sweep that is executing is
        // indexing regardless of who started it, and reporting a running background sweep as
        // "paused" would offer a Resume button for work already in flight. WAITING comes from the
        // user-only marker tag: the background sweep is enqueued on every client start, so the
        // shared tag being ENQUEUED is the steady state and says nothing about what the user asked.
        return combine(
            workManagerScheduler.workStateFlow(sessionId, WorkManagerRequestType.SEARCH_BACKFILL),
            workManagerScheduler.workStateFlow(sessionId, WorkManagerRequestType.SEARCH_BACKFILL_USER),
        ) { anySweep, userSweep ->
            when {
                anySweep == WorkActivityState.RUNNING || userSweep == WorkActivityState.RUNNING ->
                    MessageSearchSweepActivity.RUNNING
                userSweep == WorkActivityState.ENQUEUED -> MessageSearchSweepActivity.WAITING
                else -> MessageSearchSweepActivity.NONE
            }
        }
    }

    override suspend fun startUserInitiatedSweep(sessionId: SessionId) {
        Timber.tag("SearchBackfill").i("User-initiated sweep requested for $sessionId")
        workManagerScheduler.submit(SearchBackfillRequestBuilder(sessionId, userInitiated = true))
    }

    override fun cancelSweep(sessionId: SessionId) {
        // The user-initiated request carries the SEARCH_BACKFILL tag too, so one cancel reaches
        // both it and any background request.
        workManagerScheduler.cancel(sessionId, WorkManagerRequestType.SEARCH_BACKFILL)
    }
}
