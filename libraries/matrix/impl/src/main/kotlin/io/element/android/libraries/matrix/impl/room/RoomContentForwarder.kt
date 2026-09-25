/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.core.coroutine.parallelMap
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.ForwardEventException
import io.element.android.libraries.matrix.impl.roomlist.roomOrNull
import kotlinx.coroutines.withTimeout
import org.matrix.rustcomponents.sdk.MsgLikeKind
import org.matrix.rustcomponents.sdk.RoomListService
import org.matrix.rustcomponents.sdk.Timeline
import org.matrix.rustcomponents.sdk.TimelineItemContent
import org.matrix.rustcomponents.sdk.contentWithoutRelationFromMessage
import kotlin.time.Duration.Companion.milliseconds

/**
 * Helper to forward event contents from a room to a set of other rooms.
 * @param roomListService the [RoomListService] to fetch room instances to forward the event to
 */
class RoomContentForwarder(
    private val roomListService: RoomListService,
) {
    /**
     * Forwards the events with the given [eventIds] from the [fromTimeline] to the given [toRoomIds].
     * The events are sent to each room in the order they are provided in [eventIds], it is up to the caller to sort them.
     * @param fromTimeline the room to forward the events from
     * @param eventIds the ids of the events to forward, in the order they must be sent
     * @param toRoomIds the ids of the rooms to forward the events to
     * @param timeoutMs the maximum time in milliseconds to wait for an event to be sent to a room
     */
    suspend fun forward(
        fromTimeline: Timeline,
        eventIds: List<EventId>,
        toRoomIds: List<RoomId>,
        timeoutMs: Long = 5000L
    ) {
        val contents = eventIds.map { eventId ->
            val timelineEvent = fromTimeline.getEventTimelineItemByEventId(eventId.value)
            val content = timelineEvent.content as? TimelineItemContent.MsgLike ?: throw ForwardEventException(toRoomIds)
            val message = content.content.kind as? MsgLikeKind.Message ?: throw ForwardEventException(toRoomIds)
            contentWithoutRelationFromMessage(message.content)
        }
        val targetRooms = toRoomIds.toSet().mapNotNull { roomId -> roomListService.roomOrNull(roomId.value) }
        val failedForwardingTo = mutableSetOf<RoomId>()
        targetRooms.parallelMap { room ->
            room.use { targetRoom ->
                runCatchingExceptions {
                    targetRoom.timeline().use { timeline ->
                        // Send the contents sequentially, so that they are received in the same order
                        contents.forEach { content ->
                            withTimeout(timeoutMs.milliseconds) {
                                timeline.send(content)
                            }
                        }
                    }
                }
            }.onFailure {
                failedForwardingTo.add(RoomId(room.id()))
            }
        }

        if (failedForwardingTo.isNotEmpty()) {
            throw ForwardEventException(failedForwardingTo.toList())
        }
    }
}
