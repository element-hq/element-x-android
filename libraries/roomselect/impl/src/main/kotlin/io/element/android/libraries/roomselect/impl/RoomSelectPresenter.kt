/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.roomselect.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.api.roomlist.RoomSummaryDetails
import io.element.android.libraries.roomselect.api.RoomSelectMode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

class RoomSelectPresenter @AssistedInject constructor(
    @Assisted private val mode: RoomSelectMode,
    private val client: MatrixClient,
) : Presenter<RoomSelectState> {
    @AssistedFactory
    interface Factory {
        fun create(mode: RoomSelectMode): RoomSelectPresenter
    }

    @Composable
    override fun present(): RoomSelectState {
        var selectedRooms by remember { mutableStateOf(persistentListOf<RoomSummaryDetails>()) }
        var query by remember { mutableStateOf("") }
        var isSearchActive by remember { mutableStateOf(false) }
        var results: SearchBarResultState<ImmutableList<RoomSummaryDetails>> by remember { mutableStateOf(SearchBarResultState.Initial()) }

        val summaries by client.roomListService.allRooms.summaries.collectAsState(initial = emptyList())

        LaunchedEffect(query, summaries) {
            val filteredSummaries = summaries.filterIsInstance<RoomSummary.Filled>()
                .map { it.details }
                .filter { it.name.contains(query, ignoreCase = true) }
                .distinctBy { it.roomId } // This should be removed once we're sure no duplicate Rooms can be received
                .toPersistentList()
            results = if (filteredSummaries.isNotEmpty()) {
                SearchBarResultState.Results(filteredSummaries)
            } else {
                SearchBarResultState.NoResultsFound()
            }
        }

        fun handleEvents(event: RoomSelectEvents) {
            when (event) {
                is RoomSelectEvents.SetSelectedRoom -> {
                    selectedRooms = persistentListOf(event.room)
                    // Restore for multi-selection
//                    val index = selectedRooms.indexOfFirst { it.roomId == event.room.roomId }
//                    selectedRooms = if (index >= 0) {
//                        selectedRooms.removeAt(index)
//                    } else {
//                        selectedRooms.add(event.room)
//                    }
                }
                RoomSelectEvents.RemoveSelectedRoom -> selectedRooms = persistentListOf()
                is RoomSelectEvents.UpdateQuery -> query = event.query
                RoomSelectEvents.ToggleSearchActive -> isSearchActive = !isSearchActive
            }
        }

        return RoomSelectState(
            mode = mode,
            resultState = results,
            query = query,
            isSearchActive = isSearchActive,
            selectedRooms = selectedRooms,
            eventSink = { handleEvents(it) }
        )
    }
}
