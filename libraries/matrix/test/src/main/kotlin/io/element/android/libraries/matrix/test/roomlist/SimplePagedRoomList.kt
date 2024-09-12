/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.roomlist

import io.element.android.libraries.matrix.api.roomlist.DynamicRoomList
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate

data class SimplePagedRoomList(
    override val summaries: MutableStateFlow<List<RoomSummary>>,
    override val loadingState: StateFlow<RoomList.LoadingState>,
    override val currentFilter: MutableStateFlow<RoomListFilter>
) : DynamicRoomList {
    override val pageSize: Int = Int.MAX_VALUE
    override val loadedPages = MutableStateFlow(1)

    override val filteredSummaries: SharedFlow<List<RoomSummary>> = summaries

    override suspend fun loadMore() {
        // No-op
        loadedPages.getAndUpdate { it + 1 }
    }

    override suspend fun reset() {
        loadedPages.emit(1)
    }

    override suspend fun updateFilter(filter: RoomListFilter) {
        currentFilter.emit(filter)
    }

    override suspend fun rebuildSummaries() {
        // No-op
    }
}
