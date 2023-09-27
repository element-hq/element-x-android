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
        return createRoomList(innerProvider)
    }

    fun createFilterable(innerProvider: suspend () -> InnerRoomList): FilterableRoomList {
        return createRoomList(innerProvider)
    }

    private fun createRoomList(innerRoomListProvider: suspend () -> InnerRoomList): RustRoomList {
        val loadingStateFlow: MutableStateFlow<RoomList.LoadingState> = MutableStateFlow(RoomList.LoadingState.NotLoaded)
        val summariesFlow = MutableStateFlow<List<RoomSummary>>(emptyList())
        val processor = RoomSummaryListProcessor(summariesFlow, innerRoomListService, dispatcher, roomSummaryDetailsFactory)
        val dynamicEvents = MutableSharedFlow<RoomListDynamicEvents>()

        var innerRoomList: InnerRoomList? = null
        sessionCoroutineScope.launch(dispatcher) {
            innerRoomList = innerRoomListProvider()
            innerRoomList?.let { innerRoomList ->
                innerRoomList
                    .observeDynamicEntries(dynamicEvents, processor)
                    .launchIn(this)

                innerRoomList
                    .observeLoadingState(loadingStateFlow)
                    .launchIn(this)
            }
        }.invokeOnCompletion {
            innerRoomList?.destroy()
        }
        return RustRoomList(summariesFlow, loadingStateFlow, dynamicEvents)
    }
}

private class RustRoomList(
    override val summaries: MutableStateFlow<List<RoomSummary>>,
    override val loadingState: MutableStateFlow<RoomList.LoadingState>,
    private val dynamicEvents: MutableSharedFlow<RoomListDynamicEvents>
) : PagedRoomList, FilterableRoomList {

    override suspend fun updateFilter(filter: FilterableRoomList.Filter) {
        val filterEvent = RoomListDynamicEvents.SetFilter(filter.toRustFilter())
        dynamicEvents.emit(filterEvent)
    }

    override suspend fun loadMore() {
        dynamicEvents.emit(RoomListDynamicEvents.LoadMore)
    }

    override suspend fun reset() {
        dynamicEvents.emit(RoomListDynamicEvents.Reset)
    }
}

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

