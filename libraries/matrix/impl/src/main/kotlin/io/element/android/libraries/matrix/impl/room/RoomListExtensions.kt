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

import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import org.matrix.rustcomponents.sdk.RoomList
import org.matrix.rustcomponents.sdk.RoomListEntriesListener
import org.matrix.rustcomponents.sdk.RoomListEntriesUpdate
import org.matrix.rustcomponents.sdk.RoomListEntry
import org.matrix.rustcomponents.sdk.RoomListException
import org.matrix.rustcomponents.sdk.RoomListItem
import org.matrix.rustcomponents.sdk.RoomListLoadingState
import org.matrix.rustcomponents.sdk.RoomListLoadingStateListener
import org.matrix.rustcomponents.sdk.RoomListService
import org.matrix.rustcomponents.sdk.RoomListServiceState
import org.matrix.rustcomponents.sdk.RoomListServiceStateListener
import timber.log.Timber

fun RoomList.loadingStateFlow(): Flow<RoomListLoadingState> =
    mxCallbackFlow {
        val listener = object : RoomListLoadingStateListener {
            override fun onUpdate(state: RoomListLoadingState) {
                trySendBlocking(state)
            }
        }
        val result = loadingState(listener)
        send(result.state)
        result.stateStream
    }.buffer(Channel.UNLIMITED)

fun RoomList.entriesFlow(onInitialList: suspend (List<RoomListEntry>) -> Unit): Flow<List<RoomListEntriesUpdate>> =
    mxCallbackFlow {
        val listener = object : RoomListEntriesListener {
            override fun onUpdate(roomEntriesUpdate: List<RoomListEntriesUpdate>) {
                trySendBlocking(roomEntriesUpdate)
            }
        }
        val result = entries(listener)
        onInitialList(result.entries)
        result.entriesStream
    }.buffer(Channel.UNLIMITED)

fun RoomListService.roomOrNull(roomId: String): RoomListItem? {
    return try {
        room(roomId)
    } catch (exception: RoomListException) {
        Timber.d(exception, "Failed finding room with id=$roomId.")
        return null
    }
}

fun RoomListService.stateFlow(): Flow<RoomListServiceState> =
    mxCallbackFlow {
        val listener = object : RoomListServiceStateListener {
            override fun onUpdate(state: RoomListServiceState) {
                trySendBlocking(state)
            }
        }
        state(listener)
    }.buffer(Channel.UNLIMITED)
