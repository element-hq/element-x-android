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

package io.element.android.libraries.matrix.test.room

import io.element.android.libraries.matrix.api.room.RoomSummary
import io.element.android.libraries.matrix.api.room.RoomSummaryDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeRoomSummaryDataSource : RoomSummaryDataSource {

    private val allRoomSummariesFlow = MutableStateFlow<List<RoomSummary>>(emptyList())
    private val inviteRoomSummariesFlow = MutableStateFlow<List<RoomSummary>>(emptyList())
    private val allRoomsLoadingStateFlow = MutableStateFlow<RoomSummaryDataSource.LoadingState>(RoomSummaryDataSource.LoadingState.NotLoaded)

    suspend fun postAllRooms(roomSummaries: List<RoomSummary>) {
        allRoomSummariesFlow.emit(roomSummaries)
    }

    suspend fun postInviteRooms(roomSummaries: List<RoomSummary>) {
        inviteRoomSummariesFlow.emit(roomSummaries)
    }

    suspend fun postLoadingState(loadingState: RoomSummaryDataSource.LoadingState) {
        allRoomsLoadingStateFlow.emit(loadingState)
    }

    override fun allRoomsLoadingState(): StateFlow<RoomSummaryDataSource.LoadingState> {
        return allRoomsLoadingStateFlow
    }

    override fun allRooms(): StateFlow<List<RoomSummary>> {
        return allRoomSummariesFlow
    }

    override fun inviteRooms(): StateFlow<List<RoomSummary>> {
        return inviteRoomSummariesFlow
    }

    var latestSlidingSyncRange: IntRange? = null
        private set

    override fun updateAllRoomsVisibleRange(range: IntRange) {
        latestSlidingSyncRange = range
    }
}
