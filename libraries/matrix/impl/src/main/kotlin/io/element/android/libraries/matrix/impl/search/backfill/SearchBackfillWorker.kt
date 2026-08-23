/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search.backfill

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.workmanager.api.di.MetroWorkerFactory
import io.element.android.libraries.workmanager.api.di.WorkerKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

/**
 * Hosts [SearchBackfillRunner] on WorkManager so the sweep survives the app being backgrounded and
 * resumes under its constraints rather than running whenever the user happens to be looking.
 *
 * The worker owns scheduling concerns only. All loop and budget logic lives in the runner, which is a
 * plain suspend class precisely so it stays testable without Robolectric.
 */
@AssistedInject
class SearchBackfillWorker(
    @Assisted params: WorkerParameters,
    @ApplicationContext private val context: Context,
    private val matrixClientProvider: MatrixClientProvider,
    private val featureFlagService: FeatureFlagService,
) : CoroutineWorker(context, params) {
    companion object {
        const val SESSION_ID_PARAM = "session_id"
    }

    override suspend fun doWork(): Result {
        val sessionId = inputData.getString(SESSION_ID_PARAM)?.let(::SessionId) ?: return Result.failure()

        // Re-checked LIVE rather than trusting the value frozen at enqueue time: a user who turned
        // message search off should stop paying for history downloads at the next execution.
        if (!featureFlagService.isFeatureEnabled(FeatureFlags.MessageSearch)) {
            Timber.tag("SearchBackfill").d("Message search disabled, skipping sweep")
            return Result.success()
        }

        val client = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return Result.failure()

        // The index only exists when it was attached at client-build time; without it, paginating
        // would spend network on history that nothing is going to index.
        if (!client.isMessageSearchAvailable) {
            Timber.tag("SearchBackfill").d("No search index for this session, skipping sweep")
            return Result.success()
        }

        val store = DataStoreSearchBackfillStore(
            context = context,
            sessionId = sessionId,
            sessionCoroutineScope = client.sessionCoroutineScope,
        )

        val runner = SearchBackfillRunner(
            client = client,
            store = store,
            roomsProvider = { client.roomQueue() },
        )

        return runCatchingExceptions { runner.runOnce() }
            .fold(
                onSuccess = { cursor ->
                    Timber.tag("SearchBackfill").d(
                        "Sweep execution done: %d/%d rooms, %d pages",
                        cursor.index,
                        cursor.queue.size,
                        cursor.pagesIssued,
                    )
                    // More rooms left — or a queue that came up empty because the room list had
                    // not synced yet — means another execution is wanted, not that this one failed.
                    if (cursor.needsAnotherExecution) Result.retry() else Result.success()
                },
                onFailure = { error ->
                    Timber.tag("SearchBackfill").w(error, "Sweep execution failed")
                    Result.retry()
                },
            )
    }

    /**
     * Room summaries come from the sliding-sync room list, which needs a running sync — and this
     * worker is headless, so it may never populate. We wait briefly and fall back to the flat joined
     * room list rather than sweeping nothing.
     *
     * The fallback is logged at WARN on purpose: it is unordered, so "the most recent rooms first"
     * silently becomes "an arbitrary 200 rooms". If that branch turns out to be the one production
     * always takes, the prioritisation story is fiction and the log is how anyone would find out.
     */
    private suspend fun MatrixClient.roomQueue(): List<RoomId> {
        val summaries = withTimeoutOrNull(ROOM_LIST_TIMEOUT_MILLIS) {
            roomListService.allRooms.summaries.firstOrNull { it.isNotEmpty() }
        }
        if (!summaries.isNullOrEmpty()) {
            return planSearchBackfill(summaries)
        }
        Timber.tag("SearchBackfill").w("Room list unavailable headless; falling back to unordered joined rooms")
        return getJoinedRoomIds().getOrNull().orEmpty().take(ROOM_QUEUE_LIMIT)
    }

    @ContributesIntoMap(AppScope::class, binding = binding<MetroWorkerFactory.WorkerInstanceFactory<*>>())
    @WorkerKey(SearchBackfillWorker::class)
    @AssistedFactory
    interface Factory : MetroWorkerFactory.WorkerInstanceFactory<SearchBackfillWorker>
}

private const val ROOM_LIST_TIMEOUT_MILLIS = 30_000L
