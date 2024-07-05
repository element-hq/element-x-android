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

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomlist.DynamicRoomList
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.loadAllIncrementally
import io.element.android.libraries.matrix.impl.room.RoomSyncSubscriber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import org.matrix.rustcomponents.sdk.RoomListServiceState
import org.matrix.rustcomponents.sdk.RoomListServiceSyncIndicator
import timber.log.Timber
import org.matrix.rustcomponents.sdk.RoomListService as InnerRustRoomListService

private const val DEFAULT_PAGE_SIZE = 20

internal class RustRoomListService(
    private val innerRoomListService: InnerRustRoomListService,
    private val sessionCoroutineScope: CoroutineScope,
    private val sessionDispatcher: CoroutineDispatcher,
    private val roomListFactory: RoomListFactory,
    private val roomSyncSubscriber: RoomSyncSubscriber,
) : RoomListService {
    override fun createRoomList(
        pageSize: Int,
        initialFilter: RoomListFilter,
        source: RoomList.Source
    ): DynamicRoomList {
        return roomListFactory.createRoomList(
            pageSize = pageSize,
            initialFilter = initialFilter,
            coroutineContext = sessionDispatcher,
        ) {
            when (source) {
                RoomList.Source.All -> innerRoomListService.allRooms()
            }
        }
    }

    override suspend fun subscribeToVisibleRooms(roomIds: List<RoomId>) {
        val toSubscribe = roomIds.filterNot { roomSyncSubscriber.isSubscribedTo(it) }
        if (toSubscribe.isNotEmpty()) {
            Timber.d("Subscribe to ${toSubscribe.size} rooms: $toSubscribe")
            roomSyncSubscriber.batchSubscribe(toSubscribe)
        }
    }

    override val allRooms: DynamicRoomList = roomListFactory.createRoomList(
        pageSize = DEFAULT_PAGE_SIZE,
        coroutineContext = sessionDispatcher,
    ) {
        innerRoomListService.allRooms()
    }

    init {
        allRooms.loadAllIncrementally(sessionCoroutineScope)
    }

    override val syncIndicator: StateFlow<RoomListService.SyncIndicator> =
        innerRoomListService.syncIndicator()
            .map { it.toSyncIndicator() }
            .onEach { syncIndicator ->
                Timber.d("SyncIndicator = $syncIndicator")
            }
            .distinctUntilChanged()
            .stateIn(sessionCoroutineScope, SharingStarted.Eagerly, RoomListService.SyncIndicator.Hide)

    override val state: StateFlow<RoomListService.State> =
        innerRoomListService.stateFlow()
            .map { it.toRoomListState() }
            .onEach { state ->
                Timber.d("RoomList state=$state")
            }
            .distinctUntilChanged()
            .stateIn(sessionCoroutineScope, SharingStarted.Eagerly, RoomListService.State.Idle)
}

private fun RoomListServiceState.toRoomListState(): RoomListService.State {
    return when (this) {
        RoomListServiceState.INITIAL,
        RoomListServiceState.RECOVERING,
        RoomListServiceState.SETTING_UP -> RoomListService.State.Idle
        RoomListServiceState.RUNNING -> RoomListService.State.Running
        RoomListServiceState.ERROR -> RoomListService.State.Error
        RoomListServiceState.TERMINATED -> RoomListService.State.Terminated
    }
}

private fun RoomListServiceSyncIndicator.toSyncIndicator(): RoomListService.SyncIndicator {
    return when (this) {
        RoomListServiceSyncIndicator.SHOW -> RoomListService.SyncIndicator.Show
        RoomListServiceSyncIndicator.HIDE -> RoomListService.SyncIndicator.Hide
    }
}
