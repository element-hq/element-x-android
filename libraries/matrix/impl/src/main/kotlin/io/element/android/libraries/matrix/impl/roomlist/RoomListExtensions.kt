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

import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.matrix.impl.util.cancelAndDestroy
import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind
import org.matrix.rustcomponents.sdk.RoomListEntriesListener
import org.matrix.rustcomponents.sdk.RoomListEntriesUpdate
import org.matrix.rustcomponents.sdk.RoomListException
import org.matrix.rustcomponents.sdk.RoomListInterface
import org.matrix.rustcomponents.sdk.RoomListItem
import org.matrix.rustcomponents.sdk.RoomListLoadingState
import org.matrix.rustcomponents.sdk.RoomListLoadingStateListener
import org.matrix.rustcomponents.sdk.RoomListServiceInterface
import org.matrix.rustcomponents.sdk.RoomListServiceState
import org.matrix.rustcomponents.sdk.RoomListServiceStateListener
import org.matrix.rustcomponents.sdk.RoomListServiceSyncIndicator
import org.matrix.rustcomponents.sdk.RoomListServiceSyncIndicatorListener
import timber.log.Timber

private const val SYNC_INDICATOR_DELAY_BEFORE_SHOWING = 1000u
private const val SYNC_INDICATOR_DELAY_BEFORE_HIDING = 0u

fun RoomListInterface.loadingStateFlow(): Flow<RoomListLoadingState> =
    mxCallbackFlow {
        val listener = object : RoomListLoadingStateListener {
            override fun onUpdate(state: RoomListLoadingState) {
                trySendBlocking(state)
            }
        }
        val result = loadingState(listener)
        try {
            send(result.state)
        } catch (exception: Exception) {
            Timber.d("loadingStateFlow() initialState failed.")
        }
        result.stateStream
    }.catch {
        Timber.d(it, "loadingStateFlow() failed")
    }.buffer(Channel.UNLIMITED)

internal fun RoomListInterface.entriesFlow(
    pageSize: Int,
    roomListDynamicEvents: Flow<RoomListDynamicEvents>,
    initialFilterKind: RoomListEntriesDynamicFilterKind
): Flow<List<RoomListEntriesUpdate>> =
    callbackFlow {
        val listener = object : RoomListEntriesListener {
            override fun onUpdate(roomEntriesUpdate: List<RoomListEntriesUpdate>) {
                trySendBlocking(roomEntriesUpdate)
            }
        }
        val result = entriesWithDynamicAdapters(pageSize.toUInt(), listener)
        val controller = result.controller
        controller.setFilter(initialFilterKind)
        roomListDynamicEvents.onEach { controllerEvents ->
            when (controllerEvents) {
                is RoomListDynamicEvents.SetFilter -> {
                    controller.setFilter(controllerEvents.filter)
                }
                is RoomListDynamicEvents.LoadMore -> {
                    controller.addOnePage()
                }
                is RoomListDynamicEvents.Reset -> {
                    controller.resetToOnePage()
                }
            }
        }.launchIn(this)
        awaitClose {
            result.entriesStream.cancelAndDestroy()
            result.destroy()
        }
    }.catch {
        Timber.d(it, "entriesFlow() failed")
    }.buffer(Channel.UNLIMITED)

internal fun RoomListServiceInterface.stateFlow(): Flow<RoomListServiceState> =
    mxCallbackFlow {
        val listener = object : RoomListServiceStateListener {
            override fun onUpdate(state: RoomListServiceState) {
                trySendBlocking(state)
            }
        }
        tryOrNull {
            state(listener)
        }
    }.buffer(Channel.UNLIMITED)

internal fun RoomListServiceInterface.syncIndicator(): Flow<RoomListServiceSyncIndicator> =
    mxCallbackFlow {
        val listener = object : RoomListServiceSyncIndicatorListener {
            override fun onUpdate(syncIndicator: RoomListServiceSyncIndicator) {
                trySendBlocking(syncIndicator)
            }
        }
        tryOrNull {
            syncIndicator(
                SYNC_INDICATOR_DELAY_BEFORE_SHOWING,
                SYNC_INDICATOR_DELAY_BEFORE_HIDING,
                listener,
            )
        }
    }.buffer(Channel.UNLIMITED)

internal suspend fun RoomListServiceInterface.roomOrNull(roomId: String): RoomListItem? {
    return try {
        room(roomId)
    } catch (exception: RoomListException) {
        Timber.d(exception, "Failed finding room with id=$roomId.")
        return null
    }
}
