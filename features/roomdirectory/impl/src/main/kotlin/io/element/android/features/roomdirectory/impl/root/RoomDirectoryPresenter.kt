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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.features.roomdirectory.impl.root.datasource.RoomDirectoryDataSource
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class RoomDirectoryPresenter @Inject constructor(
    private val client: MatrixClient,
    private val dataSource: RoomDirectoryDataSource,
) : Presenter<RoomDirectoryState> {

    @Composable
    override fun present(): RoomDirectoryState {

        var searchQuery by rememberSaveable {
            mutableStateOf("")
        }
        var isSearchActive by rememberSaveable {
            mutableStateOf(false)
        }

        val roomSummaries by dataSource.all.collectAsState()

        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(searchQuery) {
            dataSource.updateSearchQuery(searchQuery)
        }

        fun handleEvents(event: RoomDirectoryEvents) {
            when (event) {
                is RoomDirectoryEvents.JoinRoom -> {
                    coroutineScope.joinRoom(event.roomId)
                }
                RoomDirectoryEvents.LoadMore -> {
                    coroutineScope.launch {
                        dataSource.loadMore()
                    }
                }
                is RoomDirectoryEvents.Search -> {
                    searchQuery = event.query
                }
                is RoomDirectoryEvents.SearchActiveChange -> {
                    isSearchActive = event.isActive
                    if (!isSearchActive) {
                        searchQuery = ""
                    }
                }
            }
        }

        return RoomDirectoryState(
            query = searchQuery,
            isSearchActive = isSearchActive,
            roomSummaries = roomSummaries,
            searchResults = SearchBarResultState.Initial(),
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.joinRoom(roomId: RoomId) = launch {
        client.getRoom(roomId)?.join()
    }
}
