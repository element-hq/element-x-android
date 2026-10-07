/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.search

import io.element.android.libraries.matrix.api.SdkPendingTask

/**
 * Service interface for managing search backfill tasks in the Matrix SDK.
 * This service allows starting, checking the status of, and canceling search backfill tasks.
 */
interface SearchBackfillService {
    /**
     * Enqueues a periodic search backfill task based on the provided strategy.
     * The strategy parameter determines how the backfill should be performed (e.g., in the foreground or background).
     */
    suspend fun schedulePeriodicSearchBackfill(strategy: SearchBackfillStrategy): Result<Unit>

    /**
     * Cancels any currently scheduled periodic search backfill tasks.
     */
    fun cancelPeriodicSearchBackfill()

    /**
     * Starts a background task to backfill messages for rooms that have been joined but not fully backfilled yet.
     * The strategy parameter determines how the backfill should be performed (e.g., in the foreground or background).
     *
     * @param strategy the strategy to use for backfilling messages.
     * @return a [Result] indicating the success or failure of enqueuing the backfill task.
     */
    fun startSearchBackfill(strategy: SearchBackfillStrategy): Result<SdkPendingTask>

    /**
     * Checks if a search backfill task is currently running.
     *
     * @return `true` if a search backfill task is running, `false` otherwise.
     */
    fun isSearchBackfillRunning(): Boolean

    /**
     * Cancels the currently running search backfill task, if any.
     *
     * @return a [Result] indicating the success or failure of the cancellation operation.
     */
    fun cancelSearchBackfill(): Result<Unit>
}
