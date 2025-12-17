/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.roomlist

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance

/**
 * Entry point for the room list api.
 * This service will provide different sets of rooms (all, invites, etc.).
 * It requires the SyncService to be started to receive updates.
 */
interface RoomListService {
    @Immutable
    sealed interface State {
        data object Idle : State
        data object Running : State
        data object Error : State
        data object Terminated : State
    }

    @Immutable
    sealed interface SyncIndicator {
        data object Show : SyncIndicator
        data object Hide : SyncIndicator
    }

    /**
     * Creates a room list that can be used to load more rooms and filter them dynamically.
     * @param pageSize the number of rooms to load at once.
     * @param initialFilter the initial filter to apply to the rooms.
     * @param source the source of the rooms, either all rooms or invites.
     */
    fun createRoomList(
        pageSize: Int,
        initialFilter: RoomListFilter,
        source: RoomList.Source,
    ): DynamicRoomList

    /**
     * Subscribes to sync requests for the visible rooms.
     * @param roomIds the list of visible room ids to subscribe to.
     */
    suspend fun subscribeToVisibleRooms(roomIds: List<RoomId>)

    /**
     * Returns a [DynamicRoomList] object of all rooms we want to display.
     * If you want to get a filtered room list, consider using [createRoomList].
     */
    val allRooms: DynamicRoomList

    /**
     * The sync indicator as a flow.
     */
    val syncIndicator: StateFlow<SyncIndicator>

    /**
     * The state of the service as a flow.
     */
    val state: StateFlow<State>
}

fun RoomList.loadedStateFlow(): Flow<RoomList.LoadingState.Loaded> {
    return loadingState.filterIsInstance()
}
