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

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.room.RoomSummary
import io.element.android.libraries.matrix.api.room.RoomSummaryDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.RoomListException
import org.matrix.rustcomponents.sdk.RoomListInput
import org.matrix.rustcomponents.sdk.RoomListRange
import org.matrix.rustcomponents.sdk.RoomListService
import timber.log.Timber

internal class RustRoomSummaryDataSource(
    private val roomListService: RoomListService,
    private val sessionCoroutineScope: CoroutineScope,
    coroutineDispatchers: CoroutineDispatchers,
    roomSummaryDetailsFactory: RoomSummaryDetailsFactory = RoomSummaryDetailsFactory(),
) : RoomSummaryDataSource {

    private val allRooms = MutableStateFlow<List<RoomSummary>>(emptyList())
    private val inviteRooms = MutableStateFlow<List<RoomSummary>>(emptyList())

    private val loadingState = MutableStateFlow(RoomSummaryDataSource.LoadingState.NotLoaded)
    private val allRoomsListProcessor = RoomSummaryListProcessor(allRooms, roomListService, roomSummaryDetailsFactory)

    init {
        sessionCoroutineScope.launch(coroutineDispatchers.computation) {
            roomListService.allRooms().entriesFlow { roomListEntries ->
                allRoomsListProcessor.postEntries(roomListEntries)
            }.onEach { update ->
                allRoomsListProcessor.postUpdate(update)
            }.launchIn(this)
        }
    }

    override fun allRooms(): StateFlow<List<RoomSummary>> {
        return allRooms
    }

    override fun inviteRooms(): StateFlow<List<RoomSummary>> {
        return inviteRooms
    }

    override fun loadingState(): StateFlow<RoomSummaryDataSource.LoadingState> {
        return loadingState
    }

    override fun updateRoomListVisibleRange(range: IntRange) {
        Timber.v("setVisibleRange=$range")
        sessionCoroutineScope.launch {
            try {
                val ranges = listOf(RoomListRange(range.first.toUInt(), range.last.toUInt()))
                roomListService.applyInput(
                    RoomListInput.Viewport(ranges)
                )
            } catch (exception: RoomListException) {
                Timber.e(exception, "Failed updating visible range")
            }
        }
    }
}
