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
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeRoomListService(
    private val subscribeToVisibleRoomsLambda: (List<RoomId>) -> Unit = {},
    private val createRoomListLambda: (pageSize: Int) -> DynamicRoomList = { pageSize -> FakeDynamicRoomList(pageSize = pageSize) },
    override val allRooms: RoomList = createRoomListLambda(Int.MAX_VALUE),
) : RoomListService {
    private val roomListStateFlow = MutableStateFlow<RoomListService.State>(RoomListService.State.Idle)
    private val syncIndicatorStateFlow = MutableStateFlow<RoomListService.SyncIndicator>(RoomListService.SyncIndicator.Hide)

    suspend fun postState(state: RoomListService.State) {
        roomListStateFlow.emit(state)
    }

    suspend fun postSyncIndicator(value: RoomListService.SyncIndicator) {
        syncIndicatorStateFlow.emit(value)
    }

    override fun createRoomList(
        pageSize: Int,
        source: RoomList.Source,
        coroutineScope: CoroutineScope,
    ) = createRoomListLambda(pageSize)

    override suspend fun subscribeToVisibleRooms(roomIds: List<RoomId>) {
        subscribeToVisibleRoomsLambda(roomIds)
    }

    override val state: StateFlow<RoomListService.State> = roomListStateFlow

    override val syncIndicator: StateFlow<RoomListService.SyncIndicator> = syncIndicatorStateFlow
}
