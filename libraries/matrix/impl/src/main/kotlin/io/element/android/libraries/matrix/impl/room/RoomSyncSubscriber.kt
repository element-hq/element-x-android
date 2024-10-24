/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.RoomListService
import timber.log.Timber

class RoomSyncSubscriber(
    private val roomListService: RoomListService,
    private val dispatchers: CoroutineDispatchers,
) {
    private val subscribedRoomIds = mutableSetOf<RoomId>()
    private val mutex = Mutex()

    suspend fun subscribe(roomId: RoomId) {
        mutex.withLock {
            withContext(dispatchers.io) {
                try {
                    if (!isSubscribedTo(roomId)) {
                        Timber.d("Subscribing to room $roomId}")
                        roomListService.subscribeToRooms(listOf(roomId.value))
                    }
                    subscribedRoomIds.add(roomId)
                } catch (exception: Exception) {
                    Timber.e("Failed to subscribe to room $roomId")
                }
            }
        }
    }

    suspend fun batchSubscribe(roomIds: List<RoomId>) = mutex.withLock {
        withContext(dispatchers.io) {
            try {
                val roomIdsToSubscribeTo = roomIds.filterNot { isSubscribedTo(it) }
                if (roomIdsToSubscribeTo.isNotEmpty()) {
                    Timber.d("Subscribing to rooms: $roomIds")
                    roomListService.subscribeToRooms(roomIdsToSubscribeTo.map { it.value })
                    subscribedRoomIds.addAll(roomIds)
                }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Timber.e(exception, "Failed to subscribe to rooms: $roomIds")
            }
        }
    }

    fun isSubscribedTo(roomId: RoomId): Boolean {
        return subscribedRoomIds.contains(roomId)
    }
}
