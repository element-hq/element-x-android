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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.Presenter
import kotlinx.collections.immutable.toPersistentList
import javax.inject.Inject

class RoomListFiltersPresenter @Inject constructor() : Presenter<RoomListFiltersState> {

    @Composable
    override fun present(): RoomListFiltersState {
        var unselectedFilters: Set<RoomListFilter> by rememberSaveable {
            mutableStateOf(RoomListFilter.entries.toSet())
        }
        var selectedFilters: Set<RoomListFilter> by rememberSaveable {
            mutableStateOf(emptySet())
        }

        fun updateFilters(newSelectedFilters: Set<RoomListFilter>) {
            selectedFilters = newSelectedFilters
            unselectedFilters = RoomListFilter.entries.toSet() -
                selectedFilters -
                selectedFilters.mapNotNull { it.oppositeFilter }.toSet()
        }

        fun handleEvents(event: RoomListFiltersEvents) {
            when (event) {
                is RoomListFiltersEvents.ToggleFilter -> {
                    val newSelectedFilters = if (selectedFilters.contains(event.filter)) {
                        selectedFilters - event.filter
                    } else {
                        selectedFilters + event.filter
                    }
                    updateFilters(newSelectedFilters)
                }
                RoomListFiltersEvents.ClearSelectedFilters -> {
                    updateFilters(newSelectedFilters = emptySet())
                }
            }
        }

        return RoomListFiltersState(
            unselectedFilters = unselectedFilters.toPersistentList(),
            selectedFilters = selectedFilters.toPersistentList(),
            eventSink = ::handleEvents
        )
    }
}

