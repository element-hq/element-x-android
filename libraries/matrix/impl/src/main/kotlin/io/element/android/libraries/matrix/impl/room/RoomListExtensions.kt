package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import org.matrix.rustcomponents.sdk.RoomList
import org.matrix.rustcomponents.sdk.RoomListEntriesListener
import org.matrix.rustcomponents.sdk.RoomListEntriesUpdate
import org.matrix.rustcomponents.sdk.RoomListEntry
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
    }

fun RoomList.entriesFlow(onInitialList: suspend (List<RoomListEntry>) -> Unit): Flow<RoomListEntriesUpdate> =
    mxCallbackFlow {
        val listener = object : RoomListEntriesListener {
            override fun onUpdate(roomEntriesUpdate: RoomListEntriesUpdate) {
                trySendBlocking(roomEntriesUpdate)
            }
        }
        val result = entries(listener)
        onInitialList(result.entries)
        result.entriesStream
    }

fun RoomListService.roomOrNull(roomId: String): RoomListItem? {
    return try {
        room(roomId)
    } catch (failure: Throwable) {
        Timber.e(failure, "Failed finding room with id=$roomId")
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
    }
