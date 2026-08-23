/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.search

import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.serialization.Serializable

/**
 * How a single room's backfill ended.
 *
 * Note what is deliberately missing: an outcome meaning "this room is fully indexed". The app cannot
 * know that. Back-paginating a room whose history is already in the local event-cache store
 * re-hydrates it from disk, the SDK drops those updates before the indexer sees them, and nothing is
 * indexed — yet pagination reports exactly the same success as a network fetch that indexed
 * everything. [REACHED_START] therefore means "there was nothing left to ask for", not "it is all
 * searchable".
 */
@Serializable
enum class RoomSweepOutcome {
    /** Pagination reported the start of the room. Says nothing about how much was indexed. */
    REACHED_START,

    /**
     * Stopped at the per-room page cap or time limit. More history exists.
     *
     * There is deliberately no age-horizon outcome. A "stop at N days old" rule needs the timestamp
     * of the oldest loaded event, and the only app-side source for that is the timeline item list —
     * the same signal that reads highest exactly when nothing was indexed. Cost is bounded by pages
     * instead, which is a number the app can actually trust.
     */
    PAGE_CAP,

    /** Pagination failed repeatedly for this room. */
    FAILED,

    /** The room was no longer joined by the time its turn came. */
    NOT_JOINED,
}

/**
 * Durable position of the sweep, so it survives process death and does not restart from zero.
 *
 * [queue] is frozen when a sweep generation starts. It is deliberately not recomputed as the sweep
 * runs: it is ordered by the room's latest event timestamp, which changes on every incoming message,
 * so recomputing mid-sweep would reshuffle rooms underneath the cursor and could starve a room
 * forever.
 */
@Serializable
data class SearchBackfillCursor(
    val generation: Int = 0,
    val queue: List<String> = emptyList(),
    val index: Int = 0,
    val pagesDone: Map<String, Int> = emptyMap(),
    val failures: Map<String, Int> = emptyMap(),
    val outcomes: Map<String, RoomSweepOutcome> = emptyMap(),
    val pagesIssued: Int = 0,
    val startedAt: Long = 0L,
    val finishedAt: Long? = null,
    val stoppedByBudget: Boolean = false,
) {
    val isDrained: Boolean get() = index >= queue.size

    /**
     * True when another execution is needed to make progress: rooms remain unvisited, or the
     * queue was empty when the generation started — typically because the room list had not
     * synced yet in a headless start — and nothing was swept at all.
     */
    val needsAnotherExecution: Boolean get() = !isDrained || queue.isEmpty()

    fun roomAt(position: Int): RoomId? = queue.getOrNull(position)?.let(::RoomId)
}

/**
 * What the UI is allowed to know about the sweep.
 *
 * There is no `Complete` member, and that absence is load-bearing rather than an oversight: draining
 * the queue proves the sweep ran out of work, not that the user's history is searchable. A state the
 * presenter could render as "indexing finished" would be a claim the app has no way to justify, so
 * the type makes it unrepresentable.
 */
sealed interface SearchBackfillStatus {
    data object NotStarted : SearchBackfillStatus

    data class Running(
        val roomsAttempted: Int,
        val roomsQueued: Int,
    ) : SearchBackfillStatus

    data class Stopped(
        val roomsAttempted: Int,
        val roomsCapped: Int,
        val roomsFailed: Int,
        val stoppedByBudget: Boolean,
    ) : SearchBackfillStatus
}
