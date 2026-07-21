/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search.backfill

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Cost limits for one execution of the backfill.
 *
 * Budgets are denominated in **pages and rooms, never events**. `Timeline.paginate` returns only
 * "did we reach the start", so the app has no trustworthy event count: inferring one from the
 * timeline item count would report its largest numbers precisely in the case where nothing was
 * indexed at all (history re-hydrated from the local store), which is exactly backwards.
 *
 * The numbers below are deliberately conservative starting points, not measurements. Each page is a
 * network round trip of up to 50 events, so one execution costs at most [maxPagesPerExecution]
 * requests. They should be re-tuned against a real device measurement of index growth per page.
 */
internal data class SearchBackfillBudget(
    /**
     * Pages for a single room before moving on, so one huge room cannot starve the rest.
     *
     * Deliberately looser than [maxRoomDuration], which is the effective per-room bound
     * (~180 pages at [delayBetweenPages]). Every generation walks a room from its live end, and
     * pages served from the already-cached history are charged here while indexing nothing — a
     * tight page cap therefore walls each room at `cap × cached-chunk-size` events forever, which
     * at the previous value of 20 meant nothing older than ~2560 events could ever be indexed.
     */
    val maxPagesPerRoom: Int = 200,
    /** Pages across the whole execution. The dominant cost control. */
    val maxPagesPerExecution: Int = 300,
    /** Wall-clock ceiling, kept well inside WorkManager's ~10 minute kill. */
    val executionDeadline: Duration = 5.minutes,
    /** Time spent on any one room, so a slow server cannot consume the whole execution. */
    val maxRoomDuration: Duration = 45.seconds,
    /** Consecutive failures before a room is abandoned for this generation. */
    val maxFailuresPerRoom: Int = 2,
    /** Breathing room between requests so the sweep does not monopolise the network. */
    val delayBetweenPages: Duration = 250.milliseconds,
    /** Longer pause between rooms; the sweep is never in a hurry. */
    val delayBetweenRooms: Duration = 1_000.milliseconds,
)
