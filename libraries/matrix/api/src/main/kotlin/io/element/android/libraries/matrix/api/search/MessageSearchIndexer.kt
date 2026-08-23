/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.search

import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.Flow

/**
 * Control surface for the message search history sweep, for UI that lets the user drive it
 * explicitly instead of waiting for the constrained background run.
 *
 * A user-initiated sweep is the same worker walking the same durable cursor as the background one —
 * only its scheduling differs (immediate, relaxed constraints, visible progress). Because both share
 * one cursor, starting a user sweep resumes whatever the background sweep had already covered.
 */
interface MessageSearchIndexer {
    /**
     * The durable sweep cursor for [sessionId], live. Null until a sweep has ever recorded progress.
     * `index`/`queue.size` are the "room x of y" a progress bar should render.
     */
    fun cursorFlow(sessionId: SessionId): Flow<SearchBackfillCursor?>

    /**
     * Live activity of the sweep, asymmetric by design: [MessageSearchSweepActivity.RUNNING] when
     * *any* sweep executes (an executing background sweep is indexing all the same), but
     * [MessageSearchSweepActivity.WAITING] only for a sweep the user asked for — the background one
     * is enqueued on every client start, so counting it would read as "waiting" forever.
     */
    fun userSweepActivityFlow(sessionId: SessionId): Flow<MessageSearchSweepActivity>

    /**
     * Enqueues an immediate sweep with relaxed constraints, replacing any waiting background sweep.
     * Requires the search index to be attached ([io.element.android.libraries.matrix.api.MatrixClient.isMessageSearchAvailable]);
     * without it the worker no-ops.
     */
    suspend fun startUserInitiatedSweep(sessionId: SessionId)

    /** Cancels any backfill work for [sessionId], user-initiated or background. */
    fun cancelSweep(sessionId: SessionId)
}

/** Coarse, observable state of the user's sweep request. */
enum class MessageSearchSweepActivity {
    NONE,

    /** Enqueued but not executing — typically waiting for a network connection. */
    WAITING,

    RUNNING,
}
