/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search.backfill

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.search.RoomSweepOutcome
import io.element.android.libraries.matrix.api.search.SearchBackfillCursor
import io.element.android.libraries.matrix.api.search.SearchBackfillStore
import io.element.android.libraries.matrix.api.timeline.Timeline
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import timber.log.Timber

private const val LOG_TAG = "SearchBackfill"

/**
 * Walks rooms backwards so their history reaches the local message search index.
 *
 * The app never touches the index directly — no such API is exposed. Indexing is a side effect of
 * events passing through the SDK's event cache, so "backfill" here means nothing more than asking
 * for old pages and letting the SDK index what the network returns.
 *
 * **What this cannot do, stated where the code lives.** A room whose history is already in the local
 * event-cache store re-hydrates from disk on pagination, and the SDK drops those updates before the
 * indexer sees them. The sweep gets the same "success" back either way, so it will record a clean
 * outcome for a room into which it indexed exactly nothing. Nothing here can detect that; it is why
 * no state in this package can ever report completeness.
 *
 * Deliberately a plain suspend class rather than a Worker: budgets and loop shape are then testable
 * without Robolectric, and the host decides scheduling.
 */
internal class SearchBackfillRunner(
    private val client: MatrixClient,
    private val store: SearchBackfillStore,
    private val roomsProvider: suspend () -> List<RoomId>,
    private val budget: SearchBackfillBudget = SearchBackfillBudget(),
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) {
    /**
     * Runs one execution: resumes the stored cursor, or starts a new generation when there is none
     * or the previous one drained. Returns the cursor as persisted at the end.
     */
    suspend fun runOnce(): SearchBackfillCursor {
        val startedAt = currentTimeMillis()
        var cursor = resumeOrStartGeneration(startedAt)

        if (cursor.queue.isEmpty()) {
            Timber.tag(LOG_TAG).d("Nothing to sweep")
            return cursor.copy(finishedAt = currentTimeMillis()).also { store.setCursor(it) }
        }

        var pagesThisExecution = 0
        var stopped = false

        while (!stopped && !cursor.isDrained) {
            when (val step = sweepNextRoom(cursor, startedAt, pagesThisExecution)) {
                is SweepStep.Stop -> {
                    cursor = step.cursor
                    stopped = true
                }
                is SweepStep.Continue -> {
                    cursor = step.cursor
                    pagesThisExecution += step.pagesIssued
                }
            }
        }

        val finished = cursor.copy(finishedAt = currentTimeMillis())
        store.setCursor(finished)
        Timber.tag(LOG_TAG).d(
            "Execution finished: %d/%d rooms visited, %d pages issued",
            finished.index,
            finished.queue.size,
            finished.pagesIssued,
        )
        return finished
    }

    /**
     * One iteration of the sweep: the budget and cancellation guards, then at most one room.
     *
     * Extracted so the loop above is a single decision — carry on, or stop with this cursor — instead
     * of a stack of early exits. The guards keep the order they had inline: cancellation, then the
     * page budget, then the deadline, so the clock is read exactly once per surviving iteration.
     */
    private suspend fun sweepNextRoom(
        cursor: SearchBackfillCursor,
        startedAt: Long,
        pagesThisExecution: Int,
    ): SweepStep {
        if (!currentCoroutineContext().isActive) {
            Timber.tag(LOG_TAG).d("Cancelled, stopping at index ${cursor.index}")
            return SweepStep.Stop(cursor)
        }
        if (pagesThisExecution >= budget.maxPagesPerExecution) {
            Timber.tag(LOG_TAG).d("Page budget spent after $pagesThisExecution pages")
            return SweepStep.Stop(cursor.copy(stoppedByBudget = true))
        }
        if (currentTimeMillis() - startedAt >= budget.executionDeadline.inWholeMilliseconds) {
            Timber.tag(LOG_TAG).d("Execution deadline reached")
            return SweepStep.Stop(cursor.copy(stoppedByBudget = true))
        }

        val roomId = cursor.roomAt(cursor.index) ?: return SweepStep.Stop(cursor)
        val result = sweepRoom(roomId, remainingPages = budget.maxPagesPerExecution - pagesThisExecution)

        val updated = cursor.copy(
            index = cursor.index + 1,
            pagesDone = cursor.pagesDone + (roomId.value to result.pagesIssued),
            outcomes = cursor.outcomes + (roomId.value to result.outcome),
            failures = if (result.outcome == RoomSweepOutcome.FAILED) {
                cursor.failures + (roomId.value to (cursor.failures[roomId.value] ?: 0) + 1)
            } else {
                cursor.failures
            },
            pagesIssued = cursor.pagesIssued + result.pagesIssued,
        )
        // Persisted after every room so process death costs at most one room of progress.
        store.setCursor(updated)

        if (!updated.isDrained) {
            delay(budget.delayBetweenRooms)
        }
        return SweepStep.Continue(updated, result.pagesIssued)
    }

    private suspend fun resumeOrStartGeneration(startedAt: Long): SearchBackfillCursor {
        val stored = store.getCursor()
        if (stored != null && !stored.isDrained) {
            Timber.tag(LOG_TAG).d("Resuming generation ${stored.generation} at index ${stored.index}")
            return stored
        }
        val queue = roomsProvider()
        Timber.tag(LOG_TAG).d("Starting generation with ${queue.size} rooms")
        return SearchBackfillCursor(
            generation = (stored?.generation ?: 0) + 1,
            queue = queue.map { it.value },
            startedAt = startedAt,
        )
    }

    private suspend fun sweepRoom(roomId: RoomId, remainingPages: Int): RoomResult {
        val room = client.getJoinedRoom(roomId)
        if (room == null) {
            Timber.tag(LOG_TAG).d("Room $roomId is no longer joined, skipping")
            return RoomResult(RoomSweepOutcome.NOT_JOINED, pagesIssued = 0)
        }

        val roomStartedAt = currentTimeMillis()
        var pages = 0
        var failures = 0

        return try {
            val timeline = room.liveTimeline
            while (pages < budget.maxPagesPerRoom && pages < remainingPages) {
                if (!currentCoroutineContext().isActive) break
                if (currentTimeMillis() - roomStartedAt >= budget.maxRoomDuration.inWholeMilliseconds) {
                    return RoomResult(RoomSweepOutcome.PAGE_CAP, pages)
                }
                // Mandatory: paginate() throws CannotPaginate when the timeline says it cannot, so
                // calling it unguarded turns "nothing left to fetch" into a spurious failure.
                if (!timeline.backwardPaginationStatus.value.canPaginate) {
                    return RoomResult(RoomSweepOutcome.REACHED_START, pages)
                }

                val outcome = timeline.paginate(Timeline.PaginationDirection.BACKWARDS)
                pages++

                outcome.fold(
                    onSuccess = { reachedStart ->
                        failures = 0
                        if (reachedStart) {
                            return RoomResult(RoomSweepOutcome.REACHED_START, pages)
                        }
                    },
                    onFailure = { error ->
                        failures++
                        // Room ids are safe to log; message content never is.
                        Timber.tag(LOG_TAG).w(error, "Pagination failed for $roomId (%d)", failures)
                        if (failures >= budget.maxFailuresPerRoom) {
                            return RoomResult(RoomSweepOutcome.FAILED, pages)
                        }
                    },
                )

                delay(budget.delayBetweenPages)
            }
            RoomResult(RoomSweepOutcome.PAGE_CAP, pages)
        } finally {
            // 200 rooms of un-closed Rust handles would be a slow leak, so this is not optional.
            room.close()
        }
    }

    private data class RoomResult(
        val outcome: RoomSweepOutcome,
        val pagesIssued: Int,
    )

    /** Outcome of one sweep iteration: the loop either carries on with [cursor], or ends on it. */
    private sealed interface SweepStep {
        val cursor: SearchBackfillCursor

        data class Continue(override val cursor: SearchBackfillCursor, val pagesIssued: Int) : SweepStep

        data class Stop(override val cursor: SearchBackfillCursor) : SweepStep
    }
}
