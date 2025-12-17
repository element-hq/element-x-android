/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.roomlist

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomlist.DynamicRoomList
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeRoomListService(
    var subscribeToVisibleRoomsLambda: (List<RoomId>) -> Unit = {},
) : RoomListService {
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

    override fun createRoomList(
        pageSize: Int,
        initialFilter: RoomListFilter,
        source: RoomList.Source
    ): DynamicRoomList {
        return when (source) {
            RoomList.Source.All -> allRooms
        }
    }

    override suspend fun subscribeToVisibleRooms(roomIds: List<RoomId>) {
        subscribeToVisibleRoomsLambda(roomIds)
    }

    override val allRooms = SimplePagedRoomList(
        allRoomSummariesFlow,
        allRoomsLoadingStateFlow,
        MutableStateFlow(RoomListFilter.all())
    )

    override val state: StateFlow<RoomListService.State> = roomListStateFlow

    override val syncIndicator: StateFlow<RoomListService.SyncIndicator> = syncIndicatorStateFlow
}
