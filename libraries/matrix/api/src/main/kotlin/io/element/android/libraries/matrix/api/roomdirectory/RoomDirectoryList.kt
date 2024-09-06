/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.roomdirectory

import kotlinx.coroutines.flow.Flow

interface RoomDirectoryList {
    suspend fun filter(filter: String?, batchSize: Int): Result<Unit>
    suspend fun loadMore(): Result<Unit>
    val state: Flow<State>

    data class State(
        val hasMoreToLoad: Boolean,
        val items: List<RoomDescription>,
    )
}
