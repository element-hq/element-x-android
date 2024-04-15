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

import io.element.android.libraries.matrix.api.roomlist.DynamicRoomList
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind
import org.matrix.rustcomponents.sdk.RoomListLoadingState
import org.matrix.rustcomponents.sdk.RoomListService
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import org.matrix.rustcomponents.sdk.RoomList as InnerRoomList

internal class RoomListFactory(
    private val innerRoomListService: RoomListService,
    private val sessionCoroutineScope: CoroutineScope,
    private val roomSummaryDetailsFactory: RoomSummaryDetailsFactory = RoomSummaryDetailsFactory(),
) {
    /**
     * Creates a room list that can be used to load more rooms and filter them dynamically.
     */
    fun createRoomList(
        pageSize: Int,
        coroutineScope: CoroutineScope = sessionCoroutineScope,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        initialFilter: RoomListFilter = RoomListFilter.all(),
        innerProvider: suspend () -> InnerRoomList
    ): DynamicRoomList {
        val loadingStateFlow: MutableStateFlow<RoomList.LoadingState> = MutableStateFlow(RoomList.LoadingState.NotLoaded)
        val filteredSummariesFlow = MutableSharedFlow<List<RoomSummary>>(replay = 1, extraBufferCapacity = 1)
        val summariesFlow = MutableSharedFlow<List<RoomSummary>>(replay = 1, extraBufferCapacity = 1)
        val processor = RoomSummaryListProcessor(summariesFlow, innerRoomListService, coroutineContext, roomSummaryDetailsFactory)
        // Makes sure we don't miss any events
        val dynamicEvents = MutableSharedFlow<RoomListDynamicEvents>(replay = 100)
        val currentFilter = MutableStateFlow(initialFilter)
        val loadedPages = MutableStateFlow(1)
        var innerRoomList: InnerRoomList? = null

        coroutineScope.launch(coroutineContext) {
            innerRoomList = innerProvider()
            innerRoomList?.let { innerRoomList ->
                innerRoomList.entriesFlow(
                    pageSize = pageSize,
                    roomListDynamicEvents = dynamicEvents,
                    initialFilterKind = RoomListEntriesDynamicFilterKind.NonLeft
                ).onEach { update ->
                    processor.postUpdate(update)
                }.launchIn(this)

                innerRoomList.loadingStateFlow()
                    .map { it.toLoadingState() }
                    .onEach {
                        loadingStateFlow.value = it
                    }
                    .launchIn(this)

                combine(
                    currentFilter,
                    summariesFlow
                ) { filter, summaries ->
                    summaries.filter(filter)
                }.onEach {
                    filteredSummariesFlow.emit(it)
                }.launchIn(this)
            }
        }.invokeOnCompletion {
            innerRoomList?.destroy()
        }
        return RustDynamicRoomList(
            summaries = summariesFlow,
            filteredSummaries = filteredSummariesFlow,
            loadingState = loadingStateFlow,
            currentFilter = currentFilter,
            loadedPages = loadedPages,
            dynamicEvents = dynamicEvents,
            processor = processor,
            pageSize = pageSize,
        )
    }
}

private class RustDynamicRoomList(
    override val summaries: MutableSharedFlow<List<RoomSummary>>,
    override val filteredSummaries: SharedFlow<List<RoomSummary>>,
    override val loadingState: MutableStateFlow<RoomList.LoadingState>,
    override val currentFilter: MutableStateFlow<RoomListFilter>,
    override val loadedPages: MutableStateFlow<Int>,
    private val dynamicEvents: MutableSharedFlow<RoomListDynamicEvents>,
    private val processor: RoomSummaryListProcessor,
    override val pageSize: Int,
) : DynamicRoomList {
    override suspend fun rebuildSummaries() {
        processor.rebuildRoomSummaries()
    }

    override suspend fun updateFilter(filter: RoomListFilter) {
        currentFilter.emit(filter)
    }

    override suspend fun loadMore() {
        dynamicEvents.emit(RoomListDynamicEvents.LoadMore)
        loadedPages.getAndUpdate { it + 1 }
    }

    override suspend fun reset() {
        dynamicEvents.emit(RoomListDynamicEvents.Reset)
        loadedPages.emit(1)
    }
}

private fun RoomListLoadingState.toLoadingState(): RoomList.LoadingState {
    return when (this) {
        is RoomListLoadingState.Loaded -> RoomList.LoadingState.Loaded(maximumNumberOfRooms?.toInt() ?: 0)
        RoomListLoadingState.NotLoaded -> RoomList.LoadingState.NotLoaded
    }
}
