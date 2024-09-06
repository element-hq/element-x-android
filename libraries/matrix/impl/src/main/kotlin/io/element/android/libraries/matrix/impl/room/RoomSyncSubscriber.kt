/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.timeline.item.event.EventType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.RequiredState
import org.matrix.rustcomponents.sdk.RoomListServiceInterface
import org.matrix.rustcomponents.sdk.RoomSubscription
import timber.log.Timber

private const val DEFAULT_TIMELINE_LIMIT = 20u

class RoomSyncSubscriber(
    private val roomListService: RoomListServiceInterface,
    private val dispatchers: CoroutineDispatchers,
) {
    private val subscribedRoomIds = mutableSetOf<RoomId>()
    private val mutex = Mutex()

    private val settings = RoomSubscription(
        requiredState = listOf(
            RequiredState(key = EventType.STATE_ROOM_NAME, value = ""),
            RequiredState(key = EventType.STATE_ROOM_TOPIC, value = ""),
            RequiredState(key = EventType.STATE_ROOM_AVATAR, value = ""),
            RequiredState(key = EventType.STATE_ROOM_CANONICAL_ALIAS, value = ""),
            RequiredState(key = EventType.STATE_ROOM_JOIN_RULES, value = ""),
            RequiredState(key = EventType.STATE_ROOM_POWER_LEVELS, value = ""),
            RequiredState(key = EventType.STATE_ROOM_PINNED_EVENT, value = ""),
        ),
        timelineLimit = DEFAULT_TIMELINE_LIMIT,
        // We don't need heroes here as they're already included in the `all_rooms` list
        includeHeroes = false,
    )

    suspend fun subscribe(roomId: RoomId) {
        mutex.withLock {
            withContext(dispatchers.io) {
                try {
                    if (!isSubscribedTo(roomId)) {
                        Timber.d("Subscribing to room $roomId}")
                        roomListService.subscribeToRooms(listOf(roomId.value), settings)
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
                    roomListService.subscribeToRooms(roomIdsToSubscribeTo.map { it.value }, settings)
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
