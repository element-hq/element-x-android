/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.filters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import dev.zacsweers.metro.Inject
import io.element.android.features.home.impl.datasource.RoomListDataSource
import io.element.android.features.home.impl.filters.selection.FilterSelectionStrategy
import io.element.android.libraries.architecture.Presenter
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter as MatrixRoomListFilter

@Inject
class RoomListFiltersPresenter(
    private val roomListDataSource: RoomListDataSource,
    private val filterSelectionStrategy: FilterSelectionStrategy,
) : Presenter<RoomListFiltersState> {
    private val initialFilters = filterSelectionStrategy.filterSelectionStates.value.toImmutableList()

    @Composable
    override fun present(): RoomListFiltersState {
        fun handleEvent(event: RoomListFiltersEvent) {
            when (event) {
                RoomListFiltersEvent.ClearSelectedFilters -> {
                    filterSelectionStrategy.clear()
                }
                is RoomListFiltersEvent.ToggleFilter -> {
                    filterSelectionStrategy.toggle(event.filter)
                }
            }
        }

        val filters by produceState(initialValue = initialFilters) {
            filterSelectionStrategy.filterSelectionStates
                .map { filters ->
                    value = filters.toImmutableList()
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
                .collectLatest { filters ->
                    val result = MatrixRoomListFilter.All(filters)
                    roomListDataSource.updateFilter(result)
                }
        }

        return RoomListFiltersState(
            filterSelectionStates = filters,
            eventSink = ::handleEvent,
        )
    }
}
