/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.filters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import io.element.android.features.roomlist.impl.filters.selection.FilterSelectionStrategy
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter as MatrixRoomListFilter

class RoomListFiltersPresenter @Inject constructor(
    private val roomListService: RoomListService,
    private val filterSelectionStrategy: FilterSelectionStrategy,
) : Presenter<RoomListFiltersState> {
    private val initialFilters = filterSelectionStrategy.filterSelectionStates.value.toPersistentList()

    @Composable
    override fun present(): RoomListFiltersState {
        fun handleEvents(event: RoomListFiltersEvents) {
            when (event) {
                RoomListFiltersEvents.ClearSelectedFilters -> {
                    filterSelectionStrategy.clear()
                }
                is RoomListFiltersEvents.ToggleFilter -> {
                    filterSelectionStrategy.toggle(event.filter)
                }
            }
        }

        val filters by produceState(initialValue = initialFilters) {
            filterSelectionStrategy.filterSelectionStates
                .map { filters ->
                    value = filters.toPersistentList()
                    filters.mapNotNull { filterState ->
                        if (!filterState.isSelected) {
                            return@mapNotNull null
                        }
                        when (filterState.filter) {
                            RoomListFilter.Rooms -> MatrixRoomListFilter.Category.Group
                            RoomListFilter.People -> MatrixRoomListFilter.Category.People
                            RoomListFilter.Unread -> MatrixRoomListFilter.Unread
                            RoomListFilter.Favourites -> MatrixRoomListFilter.Favorite
                            RoomListFilter.Invites -> MatrixRoomListFilter.Invite
                        }
                    }
                }
                .collect { filters ->
                    val result = MatrixRoomListFilter.All(filters)
                    roomListService.allRooms.updateFilter(result)
                }
        }

        return RoomListFiltersState(
            filterSelectionStates = filters,
            eventSink = ::handleEvents
        )
    }
}
