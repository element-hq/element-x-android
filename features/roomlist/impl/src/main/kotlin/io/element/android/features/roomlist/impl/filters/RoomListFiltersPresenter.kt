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

package io.element.android.features.roomlist.impl.filters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import io.element.android.features.roomlist.impl.filters.selection.FilterSelectionStrategy
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter as MatrixRoomListFilter

class RoomListFiltersPresenter @Inject constructor(
    private val roomListService: RoomListService,
    private val filterSelectionStrategy: FilterSelectionStrategy,
) : Presenter<RoomListFiltersState> {
    @Composable
    override fun present(): RoomListFiltersState {
        fun handleEvents(event: RoomListFiltersEvents) {
            when (event) {
                RoomListFiltersEvents.ClearSelectedFilters -> {
                    Timber.d("Clear filters")
                    filterSelectionStrategy.clear()
                }
                is RoomListFiltersEvents.ToggleFilter -> {
                    Timber.d("Toggle filter: ${event.filter}")
                    filterSelectionStrategy.toggle(event.filter)
                }
            }
        }

        val filters by produceState(initialValue = persistentListOf()) {
            Timber.d("Update filter side effect received")
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
                        }.plus(MatrixRoomListFilter.NonLeft)
                    }
                .collect { filters ->
                    Timber.d("Emitting filter side effect")
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
