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

package io.element.android.libraries.matrix.test.roomlist

import io.element.android.libraries.matrix.api.roomlist.DynamicRoomList
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate

data class SimplePagedRoomList(
    override val summaries: MutableStateFlow<List<RoomSummary>>,
    override val loadingState: StateFlow<RoomList.LoadingState>,
    override val currentFilter: MutableStateFlow<RoomListFilter>
) : DynamicRoomList {
    override val pageSize: Int = Int.MAX_VALUE
    override val loadedPages = MutableStateFlow(1)

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
