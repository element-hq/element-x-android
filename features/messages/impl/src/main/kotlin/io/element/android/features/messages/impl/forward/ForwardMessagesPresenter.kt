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

package io.element.android.features.messages.impl.forward

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomSummary
import io.element.android.libraries.matrix.api.room.RoomSummaryDetails
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ForwardMessagesPresenter @AssistedInject constructor(
    @Assisted eventId: String,
    private val room: MatrixRoom,
    private val matrixCoroutineScope: CoroutineScope,
    private val client: MatrixClient,
) : Presenter<ForwardMessagesState> {

    private val eventId: EventId = EventId(eventId)

    @AssistedFactory
    interface Factory {
        fun create(eventId: String): ForwardMessagesPresenter
    }

    @Composable
    override fun present(): ForwardMessagesState {
        var selectedRooms by remember { mutableStateOf(persistentListOf<RoomSummaryDetails>()) }
        var query by remember { mutableStateOf<String>("") }
        var isSearchActive by remember { mutableStateOf(false) }
        var results: SearchBarResultState<ImmutableList<RoomSummaryDetails>> by remember { mutableStateOf(SearchBarResultState.NotSearching()) }
        val forwardingActionState: MutableState<Async<ImmutableList<RoomId>>> = remember { mutableStateOf(Async.Uninitialized) }

        val summaries by client.roomSummaryDataSource.roomSummaries().collectAsState()

        LaunchedEffect(query, summaries) {
            val filteredSummaries = summaries.filterIsInstance<RoomSummary.Filled>()
                .map { it.details }
                .filter { it.name.contains(query, ignoreCase = true) }
                .distinctBy { it.roomId } // This should be removed once we're sure no duplicate Rooms can be received
                .toPersistentList()
            results = if (filteredSummaries.isNotEmpty()) {
                SearchBarResultState.Results(filteredSummaries)
            } else {
                SearchBarResultState.NoResults()
            }
        }

        val forwardingSucceeded by remember {
            derivedStateOf { forwardingActionState.value.dataOrNull() }
        }

        fun handleEvents(event: ForwardMessagesEvents) {
            when (event) {
                is ForwardMessagesEvents.SetSelectedRoom -> {
                    selectedRooms = persistentListOf(event.room)
                    // Restore for multi-selection
//                    val index = selectedRooms.indexOfFirst { it.roomId == event.room.roomId }
//                    selectedRooms = if (index >= 0) {
//                        selectedRooms.removeAt(index)
//                    } else {
//                        selectedRooms.add(event.room)
//                    }
                }
                ForwardMessagesEvents.RemoveSelectedRoom -> selectedRooms = persistentListOf()
                is ForwardMessagesEvents.UpdateQuery -> query = event.query
                ForwardMessagesEvents.ToggleSearchActive -> isSearchActive = !isSearchActive
                ForwardMessagesEvents.ForwardEvent -> {
                    isSearchActive = false
                    val roomIds = selectedRooms.map { it.roomId }.toPersistentList()
                    matrixCoroutineScope.forwardEvent(eventId, roomIds, forwardingActionState)
                }
                ForwardMessagesEvents.ClearError -> forwardingActionState.value = Async.Uninitialized
            }
        }

        return ForwardMessagesState(
            resultState = results,
            query = query,
            isSearchActive = isSearchActive,
            selectedRooms = selectedRooms,
            isForwarding = forwardingActionState.value.isLoading(),
            error = (forwardingActionState.value as? Async.Failure)?.exception,
            forwardingSucceeded = forwardingSucceeded,
            eventSink = { handleEvents(it) }
        )
    }

    private fun CoroutineScope.forwardEvent(
        eventId: EventId,
        roomIds: ImmutableList<RoomId>,
        isForwardMessagesState: MutableState<Async<ImmutableList<RoomId>>>,
    ) = launch {
        isForwardMessagesState.value = Async.Loading()
        room.forwardEvent(eventId, roomIds).fold(
            { isForwardMessagesState.value = Async.Success(roomIds) },
            { isForwardMessagesState.value = Async.Failure(it) }
        )
    }
}
