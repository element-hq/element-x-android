/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.roomlist.impl.search

import io.element.android.features.roomlist.impl.datasource.RoomListRoomSummaryFactory
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.api.roomlist.loadAllIncrementally
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val PAGE_SIZE = 30

class RoomListSearchDataSource @Inject constructor(
    roomListService: RoomListService,
    coroutineDispatchers: CoroutineDispatchers,
    private val roomSummaryFactory: RoomListRoomSummaryFactory,
) {
    private val roomList = roomListService.createRoomList(
        pageSize = PAGE_SIZE,
        initialFilter = RoomListFilter.None,
        source = RoomList.Source.All,
    )

    val roomSummaries: Flow<PersistentList<RoomListRoomSummary>> = roomList.filteredSummaries
        .map { roomSummaries ->
            roomSummaries
                .filterIsInstance<RoomSummary.Filled>()
                .map(roomSummaryFactory::create)
                .toPersistentList()
        }
        .flowOn(coroutineDispatchers.computation)

    suspend fun setIsActive(isActive: Boolean) = coroutineScope {
        if (isActive) {
            roomList.loadAllIncrementally(this)
        } else {
            roomList.reset()
        }
    }

    suspend fun setSearchQuery(searchQuery: String) = coroutineScope {
        val filter = if (searchQuery.isBlank()) {
            RoomListFilter.None
        } else {
            RoomListFilter.NormalizedMatchRoomName(searchQuery)
        }
        roomList.updateFilter(filter)
    }
}
