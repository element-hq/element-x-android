/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.roomlist

/**
 * RoomList with dynamic filtering and loading.
 * This is useful for large lists of rooms.
 * It lets load rooms on demand and filter them.
 */
interface DynamicRoomList : RoomList {
    val pageSize: Int

    /**
     * Load more rooms into the list if possible.
     */
    suspend fun loadMore()

    /**
     * Reset the list to its initial size.
     */
    suspend fun reset()

    /**
     * Update the filter to apply to the list.
     * @param filter the filter to apply.
     */
    suspend fun updateFilter(filter: RoomListFilter)
}

suspend fun DynamicRoomList.updateVisibleRange(
    visibleRange: IntRange,
    paginationThreshold: Int = pageSize * 3
) {
    val loadedCount = summaries.replayCache.firstOrNull().orEmpty().count()
    val threshold = loadedCount - paginationThreshold
    if (visibleRange.last >= threshold) {
        loadMore()
    }
}
