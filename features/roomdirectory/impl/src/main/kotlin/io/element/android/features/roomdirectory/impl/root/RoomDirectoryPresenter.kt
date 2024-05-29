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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.features.roomdirectory.impl.root.model.RoomDirectoryListState
import io.element.android.features.roomdirectory.impl.root.model.toFeatureModel
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryList
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryService
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomDirectoryPresenter @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
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
        LaunchedEffect(searchQuery) {
            if (searchQuery == null) return@LaunchedEffect
            // cancel load more right away
            loadingMore = false
            // debounce search query
            delay(300)
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
            }
        }

        return RoomDirectoryState(
            query = searchQuery.orEmpty(),
            roomDescriptions = listState.items,
            displayLoadMoreIndicator = listState.hasMoreToLoad,
            eventSink = ::handleEvents
        )
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
