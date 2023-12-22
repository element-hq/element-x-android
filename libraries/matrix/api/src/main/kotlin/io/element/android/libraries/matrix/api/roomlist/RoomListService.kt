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

package io.element.android.libraries.matrix.api.roomlist

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.StateFlow

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
     * returns a [DynamicRoomList] object of all rooms we want to display.
     * This will exclude some rooms like the invites, or spaces.
     */
    val allRooms: DynamicRoomList

    /**
     * returns a [RoomList] object of all invites.
     */
    val invites: RoomList

    /**
     * Will set the visible range of all rooms.
     * This is useful to load more data when the user scrolls down.
     */
    fun updateAllRoomsVisibleRange(range: IntRange)

    /**
     * The sync indicator as a flow.
     */
    val syncIndicator: StateFlow<SyncIndicator>

    /**
     * The state of the service as a flow.
     */
    val state: StateFlow<State>
}
