/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.roomdirectory

import kotlinx.coroutines.flow.Flow

interface RoomDirectoryList {
    /**
     * Starts a filtered search for the server.
     * If the filter is not provided it will search for all the rooms. You can specify a batch_size to control the number of rooms to fetch per request.
     * If the via_server is not provided it will search in the current homeserver by default.
     * This method will clear the current search results and start a new one
     */
    suspend fun filter(filter: String?, batchSize: Int, viaServerName: String?): Result<Unit>

    /**
     * Load more rooms from the current search results.
     */
    suspend fun loadMore(): Result<Unit>

    /**
     * The current search results as a state flow.
     */
    val state: Flow<SearchResult>

    data class SearchResult(
        val hasMoreToLoad: Boolean,
        val items: List<RoomDescription>,
    )
}
