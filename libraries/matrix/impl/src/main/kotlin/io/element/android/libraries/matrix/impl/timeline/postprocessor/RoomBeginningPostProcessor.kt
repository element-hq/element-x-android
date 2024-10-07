/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.postprocessor

import androidx.annotation.VisibleForTesting
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.api.timeline.item.event.OtherState
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem

/**
 * This timeline post-processor removes the room creation event and the self-join event from the timeline for DMs
 * or add the RoomBeginning item for non DM room.
 */
class RoomBeginningPostProcessor(private val mode: Timeline.Mode) {
    fun process(
        items: List<MatrixTimelineItem>,
        isDm: Boolean,
        roomCreator: UserId?,
        hasMoreToLoadBackwards: Boolean,
    ): List<MatrixTimelineItem> {
        return when {
            items.isEmpty() -> items
            mode == Timeline.Mode.PINNED_EVENTS -> items
            isDm -> processForDM(items, roomCreator)
            hasMoreToLoadBackwards -> items
            else -> processForRoom(items)
        }
    }

    private fun processForRoom(items: List<MatrixTimelineItem>): List<MatrixTimelineItem> {
        val roomBeginningItem = createRoomBeginningItem()
        return listOf(roomBeginningItem) + items
    }

    private fun processForDM(items: List<MatrixTimelineItem>, roomCreator: UserId?): List<MatrixTimelineItem> {
        // Find room creation event.
        // This is usually the first MatrixTimelineItem.Event (so index 1, index 0 is a date)
        val roomCreationEventIndex = items.indexOfFirst {
            val stateEventContent = (it as? MatrixTimelineItem.Event)?.event?.content as? StateContent
            stateEventContent?.content is OtherState.RoomCreate
        }

        // If the parameter roomCreator is null, the creator is the sender of the RoomCreate Event.
        val roomCreatorUserId = roomCreator ?: (items.getOrNull(roomCreationEventIndex) as? MatrixTimelineItem.Event)?.event?.sender
        // Find self-join event for the room creator.
        // This is usually the second MatrixTimelineItem.Event (so index 2)
        val selfUserJoinedEventIndex = roomCreatorUserId?.let { creatorUserId ->
            items.indexOfFirst {
                val stateEventContent = (it as? MatrixTimelineItem.Event)?.event?.content as? RoomMembershipContent
                stateEventContent?.change == MembershipChange.JOINED && stateEventContent.userId == creatorUserId
            }
        } ?: -1

        if (roomCreationEventIndex == -1 && selfUserJoinedEventIndex == -1) {
            return items
        }
        // Remove items at the indices we found
        val newItems = items.toMutableList()
        if (selfUserJoinedEventIndex in newItems.indices) {
            newItems.removeAt(selfUserJoinedEventIndex)
        }
        if (roomCreationEventIndex in newItems.indices) {
            newItems.removeAt(roomCreationEventIndex)
        }
        return newItems
    }

    @VisibleForTesting
    fun createRoomBeginningItem(): MatrixTimelineItem.Virtual {
        return MatrixTimelineItem.Virtual(
            uniqueId = UniqueId("RoomBeginning"),
            virtual = VirtualTimelineItem.RoomBeginning
        )
    }
}
