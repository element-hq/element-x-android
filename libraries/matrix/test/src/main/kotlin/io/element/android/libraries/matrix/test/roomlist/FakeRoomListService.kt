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

package io.element.android.libraries.matrix.test.roomlist

import io.element.android.libraries.matrix.api.roomlist.DynamicRoomList
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeRoomListService : RoomListService {
    private val allRoomSummariesFlow = MutableStateFlow<List<RoomSummary>>(emptyList())
    private val allRoomsLoadingStateFlow = MutableStateFlow<RoomList.LoadingState>(RoomList.LoadingState.NotLoaded)
    private val roomListStateFlow = MutableStateFlow<RoomListService.State>(RoomListService.State.Idle)
    private val syncIndicatorStateFlow = MutableStateFlow<RoomListService.SyncIndicator>(RoomListService.SyncIndicator.Hide)

    suspend fun postAllRooms(roomSummaries: List<RoomSummary>) {
        allRoomSummariesFlow.emit(roomSummaries)
    }

    suspend fun postAllRoomsLoadingState(loadingState: RoomList.LoadingState) {
        allRoomsLoadingStateFlow.emit(loadingState)
    }

    suspend fun postState(state: RoomListService.State) {
        roomListStateFlow.emit(state)
    }

    suspend fun postSyncIndicator(value: RoomListService.SyncIndicator) {
        syncIndicatorStateFlow.emit(value)
    }

    var latestSlidingSyncRange: IntRange? = null
        private set

    override fun createRoomList(
        pageSize: Int,
        initialFilter: RoomListFilter,
        source: RoomList.Source
    ): DynamicRoomList {
        return when (source) {
            RoomList.Source.All -> allRooms
        }
    }

    override val allRooms = SimplePagedRoomList(
        allRoomSummariesFlow,
        allRoomsLoadingStateFlow,
        MutableStateFlow(RoomListFilter.all())
    )

    override fun updateAllRoomsVisibleRange(range: IntRange) {
        latestSlidingSyncRange = range
    }

    override val state: StateFlow<RoomListService.State> = roomListStateFlow

    override val syncIndicator: StateFlow<RoomListService.SyncIndicator> = syncIndicatorStateFlow
}
