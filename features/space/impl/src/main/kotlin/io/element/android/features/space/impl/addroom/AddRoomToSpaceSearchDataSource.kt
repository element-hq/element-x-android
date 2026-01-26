/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.addroom

import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.isDm
import io.element.android.libraries.matrix.api.room.recent.getRecentlyVisitedRoomInfoFlow
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.loadAllIncrementally
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList
import io.element.android.libraries.matrix.ui.model.SelectRoomInfo
import io.element.android.libraries.matrix.ui.model.toSelectRoomInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList

private const val PAGE_SIZE = 30
private const val MAX_SUGGESTIONS_COUNT = 5

/**
 * DataSource for rooms that can be added to a space.
 * Filters out DMs, spaces, rooms already in the space, and only includes rooms the user has joined.
 */
@AssistedInject
class AddRoomToSpaceSearchDataSource(
    @Assisted coroutineScope: CoroutineScope,
    roomListService: RoomListService,
    spaceRoomList: SpaceRoomList,
    private val matrixClient: MatrixClient,
    private val coroutineDispatchers: CoroutineDispatchers,
) {
    @AssistedFactory
    interface Factory {
        fun create(coroutineScope: CoroutineScope): AddRoomToSpaceSearchDataSource
    }

    private val roomList = roomListService.createRoomList(
        pageSize = PAGE_SIZE,
        initialFilter = RoomListFilter.all(),
        source = RoomList.Source.All,
        coroutineScope = coroutineScope,
    )

    private val spaceChildrenFlow = spaceRoomList.spaceRoomsFlow.map { spaceChildren ->
        spaceChildren.map { it.roomId }.toSet()
    }

    private val filterRoomPredicate: (RoomInfo, Set<RoomId>) -> Boolean = { info, childIds ->
        !info.isSpace &&
            !info.isDm &&
            info.currentUserMembership == CurrentUserMembership.JOINED &&
            info.id !in childIds
    }

    val roomInfoList: Flow<ImmutableList<SelectRoomInfo>> = combine(
        roomList.filteredSummaries,
        spaceChildrenFlow,
    ) { roomSummaries, childIds ->
        roomSummaries
            .filter { filterRoomPredicate(it.info, childIds) }
            .map { it.toSelectRoomInfo() }
            .toImmutableList()
    }.flowOn(coroutineDispatchers.computation)

    val suggestions: Flow<ImmutableList<SelectRoomInfo>> = spaceChildrenFlow.map { childIds ->
        matrixClient
            .getRecentlyVisitedRoomInfoFlow { filterRoomPredicate(it, childIds) }
            .take(MAX_SUGGESTIONS_COUNT)
            .map { it.toSelectRoomInfo() }
            .toList()
            .toImmutableList()
    }.flowOn(coroutineDispatchers.computation)

    suspend fun setIsActive(isActive: Boolean) = coroutineScope {
        if (isActive) {
            roomList.loadAllIncrementally(this)
        } else {
            roomList.reset()
        }
    }

    suspend fun setSearchQuery(searchQuery: String) {
        val filter = if (searchQuery.isBlank()) {
            RoomListFilter.None
        } else {
            RoomListFilter.NormalizedMatchRoomName(searchQuery)
        }
        roomList.updateFilter(filter)
    }
}
