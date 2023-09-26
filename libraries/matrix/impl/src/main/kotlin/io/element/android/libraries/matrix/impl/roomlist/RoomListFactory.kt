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

package io.element.android.libraries.matrix.impl.roomlist

import io.element.android.libraries.matrix.api.roomlist.FilterableRoomList
import io.element.android.libraries.matrix.api.roomlist.PagedRoomList
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind
import org.matrix.rustcomponents.sdk.RoomListEntriesUpdate
import org.matrix.rustcomponents.sdk.RoomListLoadingState
import org.matrix.rustcomponents.sdk.RoomList as InnerRoomList
import org.matrix.rustcomponents.sdk.RoomListService as InnerRoomListService

internal class RoomListFactory(
    private val innerRoomListService: InnerRoomListService,
    private val sessionCoroutineScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val roomSummaryDetailsFactory: RoomSummaryDetailsFactory = RoomSummaryDetailsFactory(),
) {

    fun createPaged(innerProvider: suspend () -> InnerRoomList): PagedRoomList {
        val roomListFlows = createAndObserveRoomListFlows(innerProvider)
        return object : PagedRoomList {

            override suspend fun loadMore() {
                roomListFlows.dynamicEvents.emit(RoomListDynamicEvents.LoadMore)
            }

            override suspend fun reset() {
                roomListFlows.dynamicEvents.emit(RoomListDynamicEvents.Reset)
            }

            override val summaries = roomListFlows.summariesFlow
            override val loadingState = roomListFlows.loadingStateFlow
        }
    }

    fun createFilterable(innerProvider: suspend () -> InnerRoomList): FilterableRoomList {
        val roomListFlows = createAndObserveRoomListFlows(innerProvider)
        return object : FilterableRoomList {
            override suspend fun updateFilter(filter: FilterableRoomList.Filter) {
                val filterEvent = RoomListDynamicEvents.SetFilter(filter.toRustFilter())
                roomListFlows.dynamicEvents.emit(filterEvent)
            }

            override val summaries = roomListFlows.summariesFlow
            override val loadingState = roomListFlows.loadingStateFlow
        }
    }

    private fun createAndObserveRoomListFlows(innerProvider: suspend () -> InnerRoomList): RoomListFlows {
        val loadingStateFlow: MutableStateFlow<RoomList.LoadingState> = MutableStateFlow(RoomList.LoadingState.NotLoaded)
        val summariesFlow = MutableStateFlow<List<RoomSummary>>(emptyList())
        val processor = RoomSummaryListProcessor(summariesFlow, innerRoomListService, dispatcher, roomSummaryDetailsFactory)
        val dynamicEvents = MutableSharedFlow<RoomListDynamicEvents>()

        sessionCoroutineScope.launch(dispatcher) {
            val innerRoomList = innerProvider()
            innerRoomList
                .observeDynamicEntries(dynamicEvents, processor)
                .launchIn(this)

            innerRoomList
                .observeLoadingState(loadingStateFlow)
                .launchIn(this)
        }

        return RoomListFlows(summariesFlow, loadingStateFlow, dynamicEvents)
    }
}

private class RoomListFlows(
    val summariesFlow: MutableStateFlow<List<RoomSummary>>,
    val loadingStateFlow: MutableStateFlow<RoomList.LoadingState>,
    val dynamicEvents: MutableSharedFlow<RoomListDynamicEvents>
)

private fun InnerRoomList.observeDynamicEntries(roomListDynamicEvents: Flow<RoomListDynamicEvents>, processor: RoomSummaryListProcessor): Flow<List<RoomListEntriesUpdate>> {
    return entriesFlow(
        pageSize = RoomListService.DEFAULT_PAGE_SIZE,
        numberOfPages = RoomListService.DEFAULT_PAGES_TO_LOAD,
        roomListDynamicEvents = roomListDynamicEvents
    ).onEach { update ->
        processor.postUpdate(update)
    }
}

private fun InnerRoomList.observeLoadingState(stateFlow: MutableStateFlow<RoomList.LoadingState>): Flow<RoomList.LoadingState> {
    return loadingStateFlow()
        .map { it.toLoadingState() }
        .onEach {
            stateFlow.value = it
        }
}

private fun RoomListLoadingState.toLoadingState(): RoomList.LoadingState {
    return when (this) {
        is RoomListLoadingState.Loaded -> RoomList.LoadingState.Loaded(maximumNumberOfRooms?.toInt() ?: 0)
        RoomListLoadingState.NotLoaded -> RoomList.LoadingState.NotLoaded
    }
}

private fun FilterableRoomList.Filter.toRustFilter(): RoomListEntriesDynamicFilterKind {
    return when (this) {
        FilterableRoomList.Filter.None -> RoomListEntriesDynamicFilterKind.None
        is FilterableRoomList.Filter.NormalizedMatchRoomName -> RoomListEntriesDynamicFilterKind.NormalizedMatchRoomName(this.pattern)
    }
}

