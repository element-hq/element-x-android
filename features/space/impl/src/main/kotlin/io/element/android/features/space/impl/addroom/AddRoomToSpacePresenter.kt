/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.addroom

import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList
import io.element.android.libraries.matrix.api.spaces.SpaceService
import io.element.android.libraries.matrix.ui.model.SelectRoomInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

@Inject
class AddRoomToSpacePresenter(
    private val spaceRoomList: SpaceRoomList,
    private val spaceService: SpaceService,
    private val dataSourceFactory: AddRoomToSpaceSearchDataSource.Factory,
) : Presenter<AddRoomToSpaceState> {
    @Composable
    override fun present(): AddRoomToSpaceState {
        var selectedRooms: ImmutableList<SelectRoomInfo> by remember { mutableStateOf(persistentListOf()) }
        var searchQuery = rememberTextFieldState()
        var isSearchActive by remember { mutableStateOf(false) }
        val saveAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }

        val coroutineScope = rememberCoroutineScope()
        val dataSource = remember { dataSourceFactory.create(coroutineScope) }

        // Update search query in data source
        LaunchedEffect(searchQuery.text) {
            dataSource.setSearchQuery(searchQuery.text.toString())
        }
        LaunchedEffect(isSearchActive) {
            dataSource.setIsActive(isSearchActive)
        }

        val suggestions by dataSource.suggestions.collectAsState(initial = persistentListOf())

        val filteredRooms by dataSource.roomInfoList.collectAsState(initial = persistentListOf())
        val searchResults by remember<State<SearchBarResultState<ImmutableList<SelectRoomInfo>>>> {
            derivedStateOf {
                when {
                    filteredRooms.isNotEmpty() -> SearchBarResultState.Results(filteredRooms)
                    isSearchActive && searchQuery.text.isNotEmpty() -> SearchBarResultState.NoResultsFound()
                    else -> SearchBarResultState.Initial()
                }
            }
        }

        fun handleEvent(event: AddRoomToSpaceEvent) {
            when (event) {
                is AddRoomToSpaceEvent.ToggleRoom -> {
                    selectedRooms = if (selectedRooms.any { it.roomId == event.room.roomId }) {
                        selectedRooms.filterNot { it.roomId == event.room.roomId }.toImmutableList()
                    } else {
                        (selectedRooms + event.room).toImmutableList()
                    }
                }
                is AddRoomToSpaceEvent.OnSearchActiveChanged -> {
                    isSearchActive = event.active
                    if (!event.active) {
                        searchQuery.clearText()
                    }
                }
                AddRoomToSpaceEvent.Save -> {
                    coroutineScope.addRoomsToSpace(
                        selectedRooms = selectedRooms,
                        addAction = saveAction,
                    )
                }
                AddRoomToSpaceEvent.ResetSaveAction -> {
                    saveAction.value = AsyncAction.Uninitialized
                }
            }
        }

        return AddRoomToSpaceState(
            searchQuery = searchQuery,
            isSearchActive = isSearchActive,
            searchResults = searchResults,
            selectedRooms = selectedRooms,
            suggestions = suggestions,
            saveAction = saveAction.value,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.addRoomsToSpace(
        selectedRooms: ImmutableList<SelectRoomInfo>,
        addAction: MutableState<AsyncAction<Unit>>,
    ) = launch {
        addAction.runUpdatingState {
            val results = selectedRooms.map { selectedRoom ->
                async {
                    spaceService.addChildToSpace(
                        spaceId = spaceRoomList.roomId,
                        childId = selectedRoom.roomId,
                    )
                }
            }.awaitAll()
            val anyFailure = results.any { it.isFailure }
            if (anyFailure) {
                Result.failure(Exception("Failed to add some rooms"))
            } else {
                Result.success(Unit)
            }
        }
    }
}
