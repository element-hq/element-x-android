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
import io.element.android.libraries.matrix.api.core.RoomId
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
        val searchQuery = rememberTextFieldState()
        var isSearchActive by remember { mutableStateOf(false) }
        val saveAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }
        // Track whether any rooms were added (for conditional reset on Dismiss)
        var hasAddedRooms by remember { mutableStateOf(false) }

        val coroutineScope = rememberCoroutineScope()
        val dataSource = remember { dataSourceFactory.create(coroutineScope) }

        // Update search query in data source
        LaunchedEffect(searchQuery.text) {
            dataSource.setSearchQuery(searchQuery.text.toString())
        }

        val suggestions by dataSource.suggestions.collectAsState(initial = persistentListOf())

        val filteredRooms by dataSource.roomInfoList.collectAsState(initial = persistentListOf())
        val searchResults by remember {
            derivedStateOf {
                when {
                    filteredRooms.isNotEmpty() -> SearchBarResultState.Results(filteredRooms)
                    isSearchActive && searchQuery.text.isNotEmpty() -> SearchBarResultState.NoResultsFound<ImmutableList<SelectRoomInfo>>()
                    else -> SearchBarResultState.Initial<ImmutableList<SelectRoomInfo>>()
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
                        dataSource = dataSource,
                        saveAction = saveAction,
                        onPartialSuccess = { successfullyAdded ->
                            if (successfullyAdded.isNotEmpty()) {
                                hasAddedRooms = true
                            }
                            selectedRooms = selectedRooms.filterNot { it.roomId in successfullyAdded }.toImmutableList()
                        },
                    )
                }
                AddRoomToSpaceEvent.ResetSaveAction -> {
                    saveAction.value = AsyncAction.Uninitialized
                }
                AddRoomToSpaceEvent.Dismiss -> {
                    if (hasAddedRooms) {
                        coroutineScope.launch { spaceRoomList.reset() }
                    }
                }
                is AddRoomToSpaceEvent.UpdateSearchVisibleRange -> coroutineScope.launch {
                    dataSource.updateVisibleRange(event.range)
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
        dataSource: AddRoomToSpaceSearchDataSource,
        saveAction: MutableState<AsyncAction<Unit>>,
        onPartialSuccess: (Set<RoomId>) -> Unit,
    ) = launch {
        saveAction.runUpdatingState {
            val spaceId = spaceRoomList.spaceId
            val successfullyAdded = mutableSetOf<RoomId>()
            val results = selectedRooms.map { room ->
                async {
                    spaceService.addChildToSpace(
                        spaceId = spaceId,
                        childId = room.roomId,
                    ).onSuccess { successfullyAdded.add(room.roomId) }
                }
            }.awaitAll()
            val anyFailure = results.any { it.isFailure }
            if (anyFailure) {
                // On partial success, mark added rooms in data source and update selection
                dataSource.markAsAdded(successfullyAdded)
                onPartialSuccess(successfullyAdded)
                Result.failure(Exception("Failed to add some rooms"))
            } else {
                // On full success, refresh the space room list
                spaceRoomList.reset()
                Result.success(Unit)
            }
        }
    }
}
