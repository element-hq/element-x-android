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
import io.element.android.libraries.matrix.api.roomlist.updateVisibleRange
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList
import io.element.android.libraries.matrix.ui.model.SelectRoomInfo
import io.element.android.libraries.matrix.ui.model.toSelectRoomInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
    coroutineDispatchers: CoroutineDispatchers,
) {
    @AssistedFactory
    interface Factory {
        fun create(coroutineScope: CoroutineScope): AddRoomToSpaceSearchDataSource
    }

    private val roomList = roomListService.createRoomList(
        pageSize = PAGE_SIZE,
        source = RoomList.Source.All,
        coroutineScope = coroutineScope,
    )

    private val spaceChildrenFlow = spaceRoomList.spaceRoomsFlow.map { rooms ->
        rooms.map { it.roomId }.toSet()
    }

    // Track locally added rooms for partial success handling.
    // These rooms will be filtered out from search results and suggestions.
    private val addedRoomIdsFlow = MutableStateFlow<Set<RoomId>>(emptySet())

    /**
     * Marks rooms as added to the space (for partial success handling).
     */
    fun markAsAdded(roomIds: Set<RoomId>) {
        addedRoomIdsFlow.value += roomIds
    }

    private val filterRoomPredicate: (RoomInfo, Set<RoomId>, Set<RoomId>) -> Boolean = { info, childIds, addedIds ->
        !info.isSpace &&
            !info.isDm &&
            info.currentUserMembership == CurrentUserMembership.JOINED &&
            info.id !in childIds &&
            info.id !in addedIds
    }

    val roomInfoList: Flow<ImmutableList<SelectRoomInfo>> = combine(
        roomList.summaries,
        spaceChildrenFlow,
        addedRoomIdsFlow,
    ) { roomSummaries, childIds, addedIds ->
        roomSummaries
            .filter { filterRoomPredicate(it.info, childIds, addedIds) }
            .map { it.info.toSelectRoomInfo() }
            .toImmutableList()
    }.flowOn(coroutineDispatchers.computation)

    val suggestions: Flow<ImmutableList<SelectRoomInfo>> = combine(
        spaceChildrenFlow,
        addedRoomIdsFlow,
    ) { childIds, addedIds ->
        matrixClient
            .getRecentlyVisitedRoomInfoFlow { filterRoomPredicate(it, childIds, addedIds) }
            .take(MAX_SUGGESTIONS_COUNT)
            .toList()
            .map { it.toSelectRoomInfo() }
            .toImmutableList()
    }.flowOn(coroutineDispatchers.computation)

    suspend fun updateVisibleRange(visibleRange: IntRange) {
        roomList.updateVisibleRange(visibleRange)
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
