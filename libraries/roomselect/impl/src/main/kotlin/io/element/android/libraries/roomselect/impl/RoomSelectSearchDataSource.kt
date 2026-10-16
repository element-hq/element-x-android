/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.roomselect.impl

import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.recent.getRecentlyVisitedRoomInfoFlow
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.updateVisibleRange
import io.element.android.libraries.matrix.ui.model.SelectRoomInfo
import io.element.android.libraries.matrix.ui.model.toSelectRoomInfo
import io.element.android.libraries.roomselect.api.RoomSelectMode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList

private const val PAGE_SIZE = 30
private const val MAX_SUGGESTIONS_COUNT = 5

/**
 * DataSource for RoomSummaryDetails that can be filtered by a search query,
 * and which only includes rooms the user can forward or share content to.
 *
 * All the filtering is done on the computation dispatcher, so that the presenter has nothing left to derive.
 */
@AssistedInject
class RoomSelectSearchDataSource(
    @Assisted coroutineScope: CoroutineScope,
    @Assisted private val client: MatrixClient,
    @Assisted private val mode: RoomSelectMode,
    coroutineDispatchers: CoroutineDispatchers,
) {
    @AssistedFactory
    interface Factory {
        /**
         * @param coroutineScope the scope the underlying room list lives in.
         * @param client the client of the session to get the rooms from.
         * @param mode the mode of the screen, which drives the room the content comes from and whether rooms are suggested.
         */
        fun create(
            coroutineScope: CoroutineScope,
            client: MatrixClient,
            mode: RoomSelectMode,
        ): RoomSelectSearchDataSource
    }

    private val roomList = client.roomListService.createRoomList(
        pageSize = PAGE_SIZE,
        source = RoomList.Source.All,
        coroutineScope = coroutineScope
    )

    // The room the content is forwarded from cannot be a destination. There is no such room when sharing.
    private val excludedRoomId = (mode as? RoomSelectMode.Forward)?.fromRoomId

    private val filterRoomPredicate: (RoomInfo) -> Boolean = { info ->
        info.currentUserMembership == CurrentUserMembership.JOINED &&
            !info.isSpace &&
            info.id != excludedRoomId
    }

    /**
     * The most recently visited rooms. They are only loaded once, and kept for as long as this data source lives.
     */
    private val loadedSuggestions: Flow<ImmutableList<SelectRoomInfo>> = if (mode is RoomSelectMode.Forward) {
        flow {
            emit(
                client.getRecentlyVisitedRoomInfoFlow(filterRoomPredicate)
                    .take(MAX_SUGGESTIONS_COUNT)
                    .toList()
                    .map { it.toSelectRoomInfo() }
                    .toImmutableList()
            )
        }
            .flowOn(coroutineDispatchers.computation)
            .stateIn(coroutineScope, SharingStarted.Lazily, persistentListOf())
    } else {
        // Rooms are only suggested when forwarding
        flowOf(persistentListOf())
    }

    /**
     * Every list the screen can display, emitted together so that the rendered state is always consistent.
     * Emitting them separately rendered a frame where a suggested room was also part of the room list.
     */
    val rooms: Flow<RoomSelectRooms> = combine(
        roomList.summaries,
        loadedSuggestions,
    ) { roomSummaries, suggestions ->
        val allRooms = roomSummaries
            .filter { filterRoomPredicate(it.info) }
            .distinctBy { it.roomId } // This should be removed once we're sure no duplicate Rooms can be received
            .map { roomSummary -> roomSummary.toSelectRoomInfo() }
            .toImmutableList()
        val suggestedRoomIds = suggestions.mapTo(mutableSetOf()) { it.roomId }
        RoomSelectRooms(
            suggestions = suggestions,
            allRooms = allRooms,
            roomsWithoutSuggestions = allRooms.filterNot { it.roomId in suggestedRoomIds }.toImmutableList(),
        )
    }.flowOn(coroutineDispatchers.computation)

    suspend fun updateVisibleRange(visibleRange: IntRange) {
        roomList.updateVisibleRange(visibleRange)
    }

    suspend fun setSearchQuery(searchQuery: String) {
        val filter = if (searchQuery.isBlank()) {
            RoomListFilter.all()
        } else {
            RoomListFilter.NormalizedMatchRoomName(searchQuery)
        }
        roomList.updateFilter(filter)
    }
}

data class RoomSelectRooms(
    val suggestions: ImmutableList<SelectRoomInfo>,
    val allRooms: ImmutableList<SelectRoomInfo>,
    val roomsWithoutSuggestions: ImmutableList<SelectRoomInfo>,
) {
    companion object {
        val Empty = RoomSelectRooms(
            suggestions = persistentListOf(),
            allRooms = persistentListOf(),
            roomsWithoutSuggestions = persistentListOf(),
        )
    }
}
