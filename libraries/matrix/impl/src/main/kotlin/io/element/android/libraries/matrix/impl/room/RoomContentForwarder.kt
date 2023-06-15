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

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.ForwardEventException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.SlidingSync
import org.matrix.rustcomponents.sdk.TimelineDiff
import org.matrix.rustcomponents.sdk.TimelineListener
import org.matrix.rustcomponents.sdk.genTransactionId
import kotlin.time.Duration.Companion.milliseconds

/**
 * Helper to forward event contents from a room to a set of other rooms.
 */
class RoomContentForwarder(
    private val slidingSync: SlidingSync,
    private val coroutineScope: CoroutineScope,
) {

    /**
     * Forwards the event with the given [eventId] from the [fromRoom] to the given [toRoomIds].
     * @param fromRoom the room to forward the event from
     * @param eventId the id of the event to forward
     * @param toRoomIds the ids of the rooms to forward the event to
     * @param timeoutMs the maximum time in milliseconds to wait for the event to be sent to a room
     */
    suspend fun forward(
        fromRoom: Room,
        eventId: EventId,
        toRoomIds: List<RoomId>,
        timeoutMs: Long = 5000L
    ) {
        val content = fromRoom.getTimelineEventContentByEventId(eventId.value)
        val targetSlidingSyncRooms = toRoomIds.mapNotNull { slidingSync.getRoom(it.value) }
        val targetRooms = targetSlidingSyncRooms.mapNotNull { room -> room.use { it.fullRoom() } }
        val failedForwardingTo = mutableSetOf<RoomId>()
        val results = targetRooms.map { room ->
            coroutineScope.launch {
                room.use {
                    val result = runCatching {
                        // Sending a message requires a registered timeline listener
                        it.addTimelineListener(NoOpTimelineListener)
                        withTimeout(timeoutMs.milliseconds) {
                            it.send(content, genTransactionId())
                        }
                    }
                    // After sending, we remove the timeline
                    it.removeTimeline()
                    result
                }.onFailure {
                    failedForwardingTo.add(RoomId(room.id()))
                }
            }
        }
        results.joinAll()

        if (failedForwardingTo.isNotEmpty()) {
            throw ForwardEventException(toRoomIds.toList())
        }
    }

    private object NoOpTimelineListener: TimelineListener {
        override fun onUpdate(diff: TimelineDiff) = Unit
    }
}
