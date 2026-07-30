/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.search

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow

/**
 * A single, stateful message search cursor: one query at a time, with paginated results.
 *
 * Obtain an instance via [MessageSearchService.createMessageSearch]. The instance is tied to the
 * [kotlinx.coroutines.CoroutineScope] it was created with; cancelling that scope releases the
 * underlying SDK resources, so there is no `close()` to call.
 */
interface MessageSearch {
    /**
     * The current result set for the active query, in the order supplied by the SDK.
     */
    val results: StateFlow<ImmutableList<MessageSearchResult>>

    /**
     * Whether a page is currently loading, and whether the end has been reached.
     */
    val paginationState: StateFlow<MessageSearchPaginationState>

    /**
     * Set (or update) the search query. Clears the current results, restarts pagination from
     * scratch and loads the first page. Call [paginate] to load any further pages.
     */
    suspend fun setQuery(query: String): Result<Unit>

    /**
     * Load the next page of results. No-ops if a page is already loading or the end was reached.
     */
    suspend fun paginate(): Result<Unit>
}
