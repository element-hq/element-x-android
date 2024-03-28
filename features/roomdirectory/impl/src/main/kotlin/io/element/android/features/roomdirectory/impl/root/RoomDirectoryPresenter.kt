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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.features.roomdirectory.impl.root.di.JoinRoom
import io.element.android.features.roomdirectory.impl.root.model.RoomDirectoryListState
import io.element.android.features.roomdirectory.impl.root.model.toFeatureModel
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryList
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryService
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class RoomDirectoryPresenter @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val joinRoom: JoinRoom,
    private val roomDirectoryService: RoomDirectoryService,
) : Presenter<RoomDirectoryState> {
    @Composable
    override fun present(): RoomDirectoryState {
        var loadingMore by remember {
            mutableStateOf(false)
        }
        var searchQuery by rememberSaveable {
            mutableStateOf<String?>(null)
        }
        val coroutineScope = rememberCoroutineScope()
        val roomDirectoryList = remember {
            roomDirectoryService.createRoomDirectoryList(coroutineScope)
        }
        val listState by roomDirectoryList.collectState()
        val joinRoomAction: MutableState<AsyncAction<RoomId>> = remember {
            mutableStateOf(AsyncAction.Uninitialized)
        }
        LaunchedEffect(searchQuery) {
            if (searchQuery == null) return@LaunchedEffect
            // debounce search query
            delay(300)
            // cancel load more right away
            loadingMore = false
            roomDirectoryList.filter(searchQuery, 20)
        }
        LaunchedEffect(loadingMore) {
            if (loadingMore) {
                roomDirectoryList.loadMore()
                loadingMore = false
            }
        }
        fun handleEvents(event: RoomDirectoryEvents) {
            when (event) {
                RoomDirectoryEvents.LoadMore -> {
                    loadingMore = true
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
            query = searchQuery.orEmpty(),
            roomDescriptions = listState.items,
            displayLoadMoreIndicator = listState.hasMoreToLoad,
            joinRoomAction = joinRoomAction.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.joinRoom(state: MutableState<AsyncAction<RoomId>>, roomId: RoomId) = launch {
        state.runUpdatingState {
            joinRoom(roomId)
        }
    }

    @Composable
    private fun RoomDirectoryList.collectState() = remember {
        state.map {
            val items = it.items
                .map { roomDescription -> roomDescription.toFeatureModel() }
                .toImmutableList()
            RoomDirectoryListState(items = items, hasMoreToLoad = it.hasMoreToLoad)
        }.flowOn(dispatchers.computation)
    }.collectAsState(RoomDirectoryListState.Default)
}
