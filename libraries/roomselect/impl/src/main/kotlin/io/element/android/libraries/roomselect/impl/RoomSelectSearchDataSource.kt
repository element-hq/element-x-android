/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.roomselect.impl

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.loadAllIncrementally
import io.element.android.libraries.matrix.ui.model.SelectRoomInfo
import io.element.android.libraries.matrix.ui.model.toSelectRoomInfo
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val PAGE_SIZE = 30

/**
 * DataSource for RoomSummaryDetails that can be filtered by a search query,
 * and which only includes rooms the user has joined.
 */
class RoomSelectSearchDataSource @Inject constructor(
    roomListService: RoomListService,
    coroutineDispatchers: CoroutineDispatchers,
) {
    private val roomList = roomListService.createRoomList(
        pageSize = PAGE_SIZE,
        initialFilter = RoomListFilter.all(),
        source = RoomList.Source.All,
    )

    val roomInfoList: Flow<PersistentList<SelectRoomInfo>> = roomList.filteredSummaries
        .map { roomSummaries ->
            roomSummaries
                .filter { it.info.currentUserMembership == CurrentUserMembership.JOINED }
                .distinctBy { it.roomId } // This should be removed once we're sure no duplicate Rooms can be received
                .map { roomSummary -> roomSummary.toSelectRoomInfo() }
                .toPersistentList()
        }
        .flowOn(coroutineDispatchers.computation)

    suspend fun load() = coroutineScope {
        roomList.loadAllIncrementally(this)
    }

    suspend fun setSearchQuery(searchQuery: String) = coroutineScope {
        val filter = if (searchQuery.isBlank()) {
            RoomListFilter.all()
        } else {
            RoomListFilter.NormalizedMatchRoomName(searchQuery)
        }
        roomList.updateFilter(filter)
    }
}
