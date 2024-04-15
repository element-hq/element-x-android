/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix.api.roomlist

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * RoomList with dynamic filtering and loading.
 * This is useful for large lists of rooms.
 * It lets load rooms on demand and filter them.
 */
interface DynamicRoomList : RoomList {
    val currentFilter: StateFlow<RoomListFilter>
    val loadedPages: StateFlow<Int>
    val pageSize: Int

    val filteredSummaries: SharedFlow<List<RoomSummary>>

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

/**
 * Offers a way to load all the rooms incrementally.
 * It will load more room until all are loaded.
 * If total number of rooms increase, it will load more pages if needed.
 * The number of rooms is independent of the filter.
 */
fun DynamicRoomList.loadAllIncrementally(coroutineScope: CoroutineScope) {
    combine(
        loadedPages,
        loadingState,
    ) { loadedPages, loadingState ->
        loadedPages to loadingState
    }
        .onEach { (loadedPages, loadingState) ->
            when (loadingState) {
                is RoomList.LoadingState.Loaded -> {
                    if (pageSize * loadedPages < loadingState.numberOfRooms) {
                        loadMore()
                    }
                }
                RoomList.LoadingState.NotLoaded -> Unit
            }
        }
        .launchIn(coroutineScope)
}
