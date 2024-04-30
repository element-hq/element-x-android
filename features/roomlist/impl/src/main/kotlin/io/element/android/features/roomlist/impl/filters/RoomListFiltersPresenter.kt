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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.element.android.features.roomlist.impl.filters.selection.FilterSelectionStrategy
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import kotlinx.collections.immutable.toPersistentList
import javax.inject.Inject
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter as MatrixRoomListFilter

class RoomListFiltersPresenter @Inject constructor(
    private val roomListService: RoomListService,
    private val filterSelectionStrategy: FilterSelectionStrategy,
) : Presenter<RoomListFiltersState> {
    @Composable
    override fun present(): RoomListFiltersState {
        val filters by filterSelectionStrategy.filterSelectionStates.collectAsState()

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

        LaunchedEffect(filters) {
            val allRoomsFilter = MatrixRoomListFilter.All(
                filters
                    .filter { it.isSelected }
                    .map { roomListFilter ->
                        when (roomListFilter.filter) {
                            RoomListFilter.Rooms -> MatrixRoomListFilter.Category.Group
                            RoomListFilter.People -> MatrixRoomListFilter.Category.People
                            RoomListFilter.Unread -> MatrixRoomListFilter.Unread
                            RoomListFilter.Favourites -> MatrixRoomListFilter.Favorite
                            RoomListFilter.Invites -> MatrixRoomListFilter.Invite
                        }
                    }
            )
            roomListService.allRooms.updateFilter(allRoomsFilter)
        }

        return RoomListFiltersState(
            filterSelectionStates = filters.toPersistentList(),
            eventSink = ::handleEvents
        )
    }
}
