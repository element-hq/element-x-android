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

package io.element.android.features.roomlist.impl.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.features.roomlist.impl.datasource.RoomListRoomSummaryFactory
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.api.roomlist.loadAllIncrementally
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val PAGE_SIZE = 50

class RoomListSearchPresenter @Inject constructor(
    private val roomListService: RoomListService,
    private val roomSummaryFactory: RoomListRoomSummaryFactory,
    private val coroutineDispatchers: CoroutineDispatchers,
) : Presenter<RoomListSearchState> {

    @Composable
    override fun present(): RoomListSearchState {

        var isSearchActive by rememberSaveable {
            mutableStateOf(false)
        }
        var searchQuery by rememberSaveable {
            mutableStateOf("")
        }
        val coroutineScope = rememberCoroutineScope()

        val roomList = remember {
            roomListService.createRoomList(
                coroutineScope = coroutineScope,
                pageSize = PAGE_SIZE,
                initialFilter = RoomListFilter.all(RoomListFilter.None),
                source = RoomList.Source.All,
            )
        }

        LaunchedEffect(Unit) {
            roomList.loadAllIncrementally(this)
        }
        LaunchedEffect(key1 = searchQuery) {
            val filter = if (searchQuery.isBlank()) {
                RoomListFilter.all(RoomListFilter.None)
            } else {
                RoomListFilter.all(RoomListFilter.NonLeft, RoomListFilter.NormalizedMatchRoomName(searchQuery))
            }
            roomList.updateFilter(filter)
        }

        fun handleEvents(event: RoomListSearchEvents) {
            when (event) {
                RoomListSearchEvents.ClearQuery -> {
                    searchQuery = ""
                }
                is RoomListSearchEvents.QueryChanged -> {
                    searchQuery = event.query
                }
                RoomListSearchEvents.ToggleSearchVisibility -> {
                    isSearchActive = !isSearchActive
                    searchQuery = ""
                }
            }
        }

        val searchResults by roomList
            .rememberMappedSummaries()
            .collectAsState(initial = persistentListOf())

        return RoomListSearchState(
            isSearchActive = isSearchActive,
            query = searchQuery,
            results = searchResults,
            eventSink = ::handleEvents
        )
    }

    @Composable
    private fun RoomList.rememberMappedSummaries() = remember {
        summaries
            .map { roomSummaries ->
                roomSummaries
                    .filterIsInstance<RoomSummary.Filled>()
                    .map(roomSummaryFactory::create)
                    .toPersistentList()
            }
            .flowOn(coroutineDispatchers.computation)
    }
}
