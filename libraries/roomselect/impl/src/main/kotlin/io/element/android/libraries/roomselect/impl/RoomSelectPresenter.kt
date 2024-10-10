/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.roomselect.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.ui.model.SelectRoomInfo
import io.element.android.libraries.roomselect.api.RoomSelectMode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class RoomSelectPresenter @AssistedInject constructor(
    @Assisted private val mode: RoomSelectMode,
    private val dataSource: RoomSelectSearchDataSource,
) : Presenter<RoomSelectState> {
    @AssistedFactory
    interface Factory {
        fun create(mode: RoomSelectMode): RoomSelectPresenter
    }

    @Composable
    override fun present(): RoomSelectState {
        var selectedRooms by remember { mutableStateOf(persistentListOf<SelectRoomInfo>()) }
        var searchQuery by remember { mutableStateOf("") }
        var isSearchActive by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            dataSource.load()
        }

        LaunchedEffect(searchQuery) {
            dataSource.setSearchQuery(searchQuery)
        }

        val roomSummaryDetailsList by dataSource.roomInfoList.collectAsState(initial = persistentListOf())

        val searchResults by remember<State<SearchBarResultState<ImmutableList<SelectRoomInfo>>>> {
            derivedStateOf {
                when {
                    roomSummaryDetailsList.isNotEmpty() -> SearchBarResultState.Results(roomSummaryDetailsList.toImmutableList())
                    isSearchActive -> SearchBarResultState.NoResultsFound()
                    else -> SearchBarResultState.Initial()
                }
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
                is RoomSelectEvents.UpdateQuery -> searchQuery = event.query
                RoomSelectEvents.ToggleSearchActive -> isSearchActive = !isSearchActive
            }
        }

        return RoomSelectState(
            mode = mode,
            resultState = searchResults,
            query = searchQuery,
            isSearchActive = isSearchActive,
            selectedRooms = selectedRooms,
            eventSink = { handleEvents(it) }
        )
    }
}
