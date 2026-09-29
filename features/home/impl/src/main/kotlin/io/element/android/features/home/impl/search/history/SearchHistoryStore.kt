/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search.history

import kotlinx.coroutines.flow.Flow

/**
 * Stores the global search history for the current session.
 *
 * The history is persisted in an encrypted file and capped to [MAX_SEARCH_HISTORY_SIZE] entries.
 * The most recent entry is always at the front of the list; when the cap is reached the oldest
 * entries are dropped.
 */
interface SearchHistoryStore {
    /**
     * Emits the current search history, most recent entry first, and re-emits whenever it changes.
     */
    val history: Flow<List<SearchHistoryResult>>

    /**
     * Add a new [result] at the front of the history.
     *
     * If an equal entry already exists it is moved to the front instead of being duplicated. When
     * the history exceeds [MAX_SEARCH_HISTORY_SIZE] entries the oldest ones are dropped.
     */
    suspend fun add(result: SearchHistoryResult)

    /**
     * Remove every entry from the history.
     */
    suspend fun clear()
}

/**
 * Maximum number of entries kept in the search history.
 */
const val MAX_SEARCH_HISTORY_SIZE = 25
