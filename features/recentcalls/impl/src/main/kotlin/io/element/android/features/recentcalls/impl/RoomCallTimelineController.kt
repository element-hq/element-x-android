/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.recentcalls.impl

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.notification.CallIntent
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.CallNotifyContent
import io.element.android.libraries.matrix.api.timeline.item.event.LegacyCallInviteContent

data class RawCallEvent(
    val eventId: EventId,
    val roomId: RoomId,
    val roomInfo: RoomInfo,
    val senderId: UserId,
    val isOwn: Boolean,
    val timestamp: Long,
    val callIntent: CallIntent,
    val declinedBy: List<UserId>,
)

internal class RoomCallTimelineController(
    val roomId: RoomId,
    val roomInfo: RoomInfo,
    val timeline: Timeline,
) : AutoCloseable {
    override fun close() {
        timeline.close()
    }

    fun canPaginateBackwards(): Boolean = timeline.backwardPaginationStatus.value.canPaginate

    suspend fun paginateBackwards(): Result<Boolean> = timeline.paginate(Timeline.PaginationDirection.BACKWARDS)

    fun extractRawEvents(items: List<MatrixTimelineItem>): List<RawCallEvent> {
        return items.mapNotNull { item ->
            val event = (item as? MatrixTimelineItem.Event)?.event ?: return@mapNotNull null
            val eventId = event.eventId ?: return@mapNotNull null
            when (val content = event.content) {
                is CallNotifyContent -> RawCallEvent(
                    eventId = eventId,
                    roomId = roomId,
                    roomInfo = roomInfo,
                    senderId = event.sender,
                    isOwn = event.isOwn,
                    timestamp = event.timestamp,
                    callIntent = content.callIntent,
                    declinedBy = content.declinedBy,
                )
                LegacyCallInviteContent -> RawCallEvent(
                    eventId = eventId,
                    roomId = roomId,
                    roomInfo = roomInfo,
                    senderId = event.sender,
                    isOwn = event.isOwn,
                    timestamp = event.timestamp,
                    callIntent = CallIntent.VIDEO,
                    declinedBy = emptyList(),
                )
                else -> null
            }
        }
    }
}
