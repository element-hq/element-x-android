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
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind
import org.matrix.rustcomponents.sdk.RoomListLoadingState
import org.matrix.rustcomponents.sdk.RoomList as InnerRoomList
import org.matrix.rustcomponents.sdk.RoomListService as InnerRoomListService

internal class RoomListFactory(
    private val innerRoomListService: InnerRoomListService,
    private val coroutineScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val roomSummaryDetailsFactory: RoomSummaryDetailsFactory = RoomSummaryDetailsFactory(),
) {

    /**
     * Creates a room list that will load all rooms in a single page.
     * It mimics the usage of the old api.
     */
    fun createRoomList(
        innerProvider: suspend () -> InnerRoomList,
    ): RoomList {
        return createRustRoomList(
            pageSize = Int.MAX_VALUE,
            numberOfPages = 1,
            initialFilterKind = RoomListEntriesDynamicFilterKind.AllNonLeft,
            innerRoomListProvider = innerProvider
        )
    }

    /**
     * Creates a room list that can be used to load more rooms and filter them dynamically.
     */
    fun createDynamicRoomList(
        pageSize: Int = DynamicRoomList.DEFAULT_PAGE_SIZE,
        pagesToLoad: Int = DynamicRoomList.DEFAULT_PAGES_TO_LOAD,
        initialFilter: DynamicRoomList.Filter = DynamicRoomList.Filter.None,
        innerProvider: suspend () -> InnerRoomList
    ): DynamicRoomList {
        return createRustRoomList(
            pageSize = pageSize,
            numberOfPages = pagesToLoad,
            initialFilterKind = initialFilter.toRustFilter(),
            innerRoomListProvider = innerProvider
        )
    }

    private fun createRustRoomList(
        pageSize: Int,
        numberOfPages: Int,
        initialFilterKind: RoomListEntriesDynamicFilterKind,
        innerRoomListProvider: suspend () -> InnerRoomList
    ): RustDynamicRoomList {
        val loadingStateFlow: MutableStateFlow<RoomList.LoadingState> = MutableStateFlow(RoomList.LoadingState.NotLoaded)
        val summariesFlow = MutableStateFlow<List<RoomSummary>>(emptyList())
        val processor = RoomSummaryListProcessor(summariesFlow, innerRoomListService, dispatcher, roomSummaryDetailsFactory)
        val dynamicEvents = MutableSharedFlow<RoomListDynamicEvents>()

        var innerRoomList: InnerRoomList? = null
        coroutineScope.launch(dispatcher) {
            innerRoomList = innerRoomListProvider()
            innerRoomList?.let { innerRoomList ->
                innerRoomList.entriesFlow(
                    pageSize = pageSize,
                    numberOfPages = numberOfPages,
                    initialFilterKind = initialFilterKind,
                    roomListDynamicEvents = dynamicEvents
                ).onEach { update ->
                    processor.postUpdate(update)
                }.launchIn(this)

                innerRoomList.loadingStateFlow()
                    .map { it.toLoadingState() }
                    .onEach {
                        loadingStateFlow.value = it
                    }
                    .launchIn(this)
            }
        }.invokeOnCompletion {
            innerRoomList?.destroy()
        }
        return RustDynamicRoomList(summariesFlow, loadingStateFlow, dynamicEvents, processor)
    }
}

private class RustDynamicRoomList(
    override val summaries: MutableStateFlow<List<RoomSummary>>,
    override val loadingState: MutableStateFlow<RoomList.LoadingState>,
    private val dynamicEvents: MutableSharedFlow<RoomListDynamicEvents>,
    private val processor: RoomSummaryListProcessor,
) : DynamicRoomList {

    override suspend fun rebuildSummaries() {
        processor.rebuildRoomSummaries()
    }

    override suspend fun updateFilter(filter: DynamicRoomList.Filter) {
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

private fun RoomListLoadingState.toLoadingState(): RoomList.LoadingState {
    return when (this) {
        is RoomListLoadingState.Loaded -> RoomList.LoadingState.Loaded(maximumNumberOfRooms?.toInt() ?: 0)
        RoomListLoadingState.NotLoaded -> RoomList.LoadingState.NotLoaded
    }
}

private fun DynamicRoomList.Filter.toRustFilter(): RoomListEntriesDynamicFilterKind {
    return when (this) {
        DynamicRoomList.Filter.All -> RoomListEntriesDynamicFilterKind.All
        is DynamicRoomList.Filter.NormalizedMatchRoomName -> RoomListEntriesDynamicFilterKind.NormalizedMatchRoomName(this.pattern)
        DynamicRoomList.Filter.None -> RoomListEntriesDynamicFilterKind.None
    }
}

