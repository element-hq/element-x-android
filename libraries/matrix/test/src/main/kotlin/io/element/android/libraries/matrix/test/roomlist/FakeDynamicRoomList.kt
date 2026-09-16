/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.roomlist

import io.element.android.libraries.matrix.api.roomlist.DynamicRoomList
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.coroutines.flow.MutableStateFlow

class FakeDynamicRoomList(
    override val summaries: MutableStateFlow<List<RoomSummary>> = MutableStateFlow(emptyList()),
    override val loadingState: MutableStateFlow<RoomList.LoadingState> = MutableStateFlow(RoomList.LoadingState.NotLoaded),
    override val pageSize: Int = Int.MAX_VALUE,
    val currentFilter: MutableStateFlow<RoomListFilter> = MutableStateFlow(RoomListFilter.None),
    private val loadMoreLambda: () -> Unit = {},
    private val resetLambda: () -> Unit = {},
    private val updateFilterLambda: (RoomListFilter) -> Unit = { filter -> currentFilter.value = filter },
    private val rebuildSummariesLambda: () -> Unit = {},
) : DynamicRoomList {
    override suspend fun loadMore() {
        loadMoreLambda()
    }

    override suspend fun reset() {
        resetLambda()
    }

    override suspend fun updateFilter(filter: RoomListFilter) {
        updateFilterLambda(filter)
    }

    override suspend fun rebuildSummaries() {
        rebuildSummariesLambda()
    }
}
