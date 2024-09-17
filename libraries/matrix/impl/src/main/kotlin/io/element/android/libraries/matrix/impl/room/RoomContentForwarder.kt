/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.core.coroutine.parallelMap
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.ForwardEventException
import io.element.android.libraries.matrix.impl.roomlist.fullRoomWithTimeline
import io.element.android.libraries.matrix.impl.roomlist.roomOrNull
import io.element.android.libraries.matrix.impl.timeline.runWithTimelineListenerRegistered
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout
import org.matrix.rustcomponents.sdk.Disposable
import org.matrix.rustcomponents.sdk.RoomListServiceInterface
import org.matrix.rustcomponents.sdk.TimelineInterface
import kotlin.time.Duration.Companion.milliseconds

/**
 * Helper to forward event contents from a room to a set of other rooms.
 * @param roomListService the [RoomListServiceInterface] to fetch room instances to forward the event to
 */
class RoomContentForwarder(
    private val roomListService: RoomListServiceInterface,
) {
    /**
     * Forwards the event with the given [eventId] from the [fromTimeline] to the given [toRoomIds].
     * @param fromTimeline the room to forward the event from
     * @param eventId the id of the event to forward
     * @param toRoomIds the ids of the rooms to forward the event to
     * @param timeoutMs the maximum time in milliseconds to wait for the event to be sent to a room
     */
    suspend fun forward(
        fromTimeline: TimelineInterface,
        eventId: EventId,
        toRoomIds: List<RoomId>,
        timeoutMs: Long = 5000L
    ) {
        val content = fromTimeline
            .getEventTimelineItemByEventId(eventId.value)
            .content()
            .asMessage()
            ?.content()
            ?: throw ForwardEventException(toRoomIds)

        val targetSlidingSyncRooms = toRoomIds.mapNotNull { roomId -> roomListService.roomOrNull(roomId.value) }
        val targetRooms = targetSlidingSyncRooms.map { slidingSyncRoom ->
            slidingSyncRoom.use { it.fullRoomWithTimeline(null) }
        }
        val failedForwardingTo = mutableSetOf<RoomId>()
        targetRooms.parallelMap { room ->
            try {
                runCatching {
                    // Sending a message requires a registered timeline listener
                    room.timeline().runWithTimelineListenerRegistered {
                        withTimeout(timeoutMs.milliseconds) {
                            room.timeline().send(content)
                        }
                    }
                }.onFailure {
                    failedForwardingTo.add(RoomId(room.id()))
                    if (it is CancellationException) {
                        throw it
                    }
                }
            } finally {
                Disposable.destroy(room)
            }
        }

        if (failedForwardingTo.isNotEmpty()) {
            throw ForwardEventException(failedForwardingTo.toList())
        }
    }
}
