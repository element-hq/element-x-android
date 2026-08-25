/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.threads

import kotlinx.coroutines.flow.Flow

/**
 * Exposes the paginated list of threads of a single room, kept up to date by the SDK.
 *
 * The list grows through explicit [paginate] calls; [destroy] must be called to release the underlying SDK resources.
 */
interface ThreadsListService {
    /**
     * The threads loaded so far, starting empty and re-emitted in full whenever the SDK sends an update.
     * The underlying subscription is started on the first call and shared by later ones.
     */
    fun subscribeToItemUpdates(): Flow<List<ThreadListItem>>

    /** The pagination state of the list, emitting the current value immediately so callers know whether more threads can be loaded. */
    fun subscribeToPaginationUpdates(): Flow<ThreadListPaginationStatus>

    /** Loads the next page of threads, which will be reflected in [subscribeToItemUpdates]. */
    suspend fun paginate(): Result<Unit>

    /** Discards the loaded threads and starts the list again from the most recent one. */
    suspend fun reset(): Result<Unit>

    /** Releases the SDK resources and stops the item subscription; the service is unusable afterwards. */
    fun destroy()
}
