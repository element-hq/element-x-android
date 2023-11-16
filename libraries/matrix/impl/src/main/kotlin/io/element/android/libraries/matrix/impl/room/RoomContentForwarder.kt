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

import io.element.android.libraries.core.coroutine.parallelMap
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.ForwardEventException
import io.element.android.libraries.matrix.impl.roomlist.roomOrNull
import io.element.android.libraries.matrix.impl.timeline.runWithTimelineListenerRegistered
import io.element.android.libraries.matrix.impl.util.useAny
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout
import org.matrix.rustcomponents.sdk.RoomInterface
import org.matrix.rustcomponents.sdk.RoomListServiceInterface
import kotlin.time.Duration.Companion.milliseconds

/**
 * Helper to forward event contents from a room to a set of other rooms.
 * @param roomListService the [RoomListServiceInterface] to fetch room instances to forward the event to
 */
class RoomContentForwarder(
    private val roomListService: RoomListServiceInterface,
) {

    /**
     * Forwards the event with the given [eventId] from the [fromRoom] to the given [toRoomIds].
     * @param fromRoom the room to forward the event from
     * @param eventId the id of the event to forward
     * @param toRoomIds the ids of the rooms to forward the event to
     * @param timeoutMs the maximum time in milliseconds to wait for the event to be sent to a room
     */
    suspend fun forward(
        fromRoom: RoomInterface,
        eventId: EventId,
        toRoomIds: List<RoomId>,
        timeoutMs: Long = 5000L
    ) {
        val content = fromRoom.getTimelineEventContentByEventId(eventId.value)
        val targetSlidingSyncRooms = toRoomIds.mapNotNull { roomId -> roomListService.roomOrNull(roomId.value) }
        val targetRooms: List<RoomInterface> = targetSlidingSyncRooms.mapNotNull { slidingSyncRoom ->
            slidingSyncRoom.useAny { it.fullRoom() }
        }
        val failedForwardingTo = mutableSetOf<RoomId>()
        targetRooms.parallelMap { room ->
            room.useAny { targetRoom ->
                runCatching {
                    // Sending a message requires a registered timeline listener
                    targetRoom.runWithTimelineListenerRegistered {
                        withTimeout(timeoutMs.milliseconds) {
                            targetRoom.send(content)
                        }
                    }
                }
            }.onFailure {
                failedForwardingTo.add(RoomId(room.id()))
                if (it is CancellationException) {
                    throw it
                }
            }
        }

        if (failedForwardingTo.isNotEmpty()) {
            throw ForwardEventException(toRoomIds.toList())
        }
    }
}
