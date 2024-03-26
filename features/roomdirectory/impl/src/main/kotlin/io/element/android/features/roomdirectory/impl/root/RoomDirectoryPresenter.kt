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

package io.element.android.features.roomdirectory.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.features.roomdirectory.impl.root.model.toUiModel
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryList
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryService
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class RoomDirectoryPresenter @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val matrixClient: MatrixClient,
    roomDirectoryService: RoomDirectoryService,
) : Presenter<RoomDirectoryState> {

    private val roomDirectoryList = roomDirectoryService.createRoomDirectoryList()

    @Composable
    override fun present(): RoomDirectoryState {

        var searchQuery by rememberSaveable {
            mutableStateOf("")
        }
        val allRooms by roomDirectoryList.collectItemsAsState()
        val hasMoreToLoad by produceState(initialValue = true, allRooms) {
            value = roomDirectoryList.hasMoreToLoad()
        }
        val joinRoomAction: MutableState<AsyncAction<RoomId>> = remember {
            mutableStateOf(AsyncAction.Uninitialized)
        }
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(searchQuery) {
            roomDirectoryList.filter(searchQuery, 20)
        }
        fun handleEvents(event: RoomDirectoryEvents) {
            when (event) {
                RoomDirectoryEvents.LoadMore -> {
                    coroutineScope.launch {
                        roomDirectoryList.loadMore()
                    }
                }
                is RoomDirectoryEvents.Search -> {
                    searchQuery = event.query
                }
                is RoomDirectoryEvents.JoinRoom -> {
                    coroutineScope.joinRoom(joinRoomAction, event.roomId)
                }
                RoomDirectoryEvents.JoinRoomDismissError -> {
                    joinRoomAction.value = AsyncAction.Uninitialized
                }
            }
        }

        return RoomDirectoryState(
            query = searchQuery,
            roomDescriptions = allRooms,
            displayLoadMoreIndicator = hasMoreToLoad,
            joinRoomAction = joinRoomAction.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.joinRoom(state: MutableState<AsyncAction<RoomId>>, roomId: RoomId) = launch {
        state.runUpdatingState {
            matrixClient.joinRoom(roomId)
        }
    }

    @Composable
    private fun RoomDirectoryList.collectItemsAsState() = remember {
        items.map { list ->
            list
                .map { roomDescription -> roomDescription.toUiModel() }
                .toImmutableList()
        }.flowOn(dispatchers.computation)
    }.collectAsState(persistentListOf())
}
