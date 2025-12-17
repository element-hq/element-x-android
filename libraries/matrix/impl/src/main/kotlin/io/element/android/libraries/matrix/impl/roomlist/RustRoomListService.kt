/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
    private val sessionDispatcher: CoroutineDispatcher,
    private val roomListFactory: RoomListFactory,
    private val roomSyncSubscriber: RoomSyncSubscriber,
    sessionCoroutineScope: CoroutineScope,
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
        roomSyncSubscriber.batchSubscribe(roomIds)
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
