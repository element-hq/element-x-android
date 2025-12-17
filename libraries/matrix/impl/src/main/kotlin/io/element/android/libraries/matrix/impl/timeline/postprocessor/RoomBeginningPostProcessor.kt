/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.postprocessor

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.api.timeline.item.event.OtherState
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent

/**
 * This timeline post-processor removes the room creation event and the self-join event from the timeline for DMs
 * or add the RoomBeginning item.
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
            mode == Timeline.Mode.PinnedEvents -> items
            isDm -> processForDM(items, roomCreator)
            hasMoreToLoadBackwards -> items
            else -> processForRoom(items)
        }
    }

    private fun processForRoom(items: List<MatrixTimelineItem>): List<MatrixTimelineItem> {
        // No changes needed, timeline start item is already added by the SDK
        return items
    }

    private fun processForDM(items: List<MatrixTimelineItem>, roomCreator: UserId?): List<MatrixTimelineItem> {
        // Find room creation event.
        // This is usually the first MatrixTimelineItem.Event (so index 1, index 0 is a date)
        val roomCreationEventIndex = items.indexOfFirst {
            val stateEventContent = (it as? MatrixTimelineItem.Event)?.event?.content as? StateContent
            stateEventContent?.content is OtherState.RoomCreate
        }.takeIf { it >= 0 }

        // If the parameter roomCreator is null, the creator is the sender of the RoomCreate Event.
        val roomCreatorUserId = roomCreator ?: roomCreationEventIndex?.let {
            (items.getOrNull(it) as? MatrixTimelineItem.Event)?.event?.sender
        }
        // Find self-join event for the room creator.
        // This is usually the second MatrixTimelineItem.Event (so index 2)
        val selfUserJoinedEventIndex = roomCreatorUserId?.let { creatorUserId ->
            items.indexOfFirst {
                val stateEventContent = (it as? MatrixTimelineItem.Event)?.event?.content as? RoomMembershipContent
                stateEventContent?.change == MembershipChange.JOINED && stateEventContent.userId == creatorUserId
            }.takeIf { it >= 0 }
        }

        val indicesToRemove = listOfNotNull(
            roomCreationEventIndex,
            selfUserJoinedEventIndex,
        )
        if (indicesToRemove.isEmpty()) {
            // Nothing to do, return the list as is
            return items
        }

        // Remove items at the indices we found
        val newItems = items.toMutableList()
        indicesToRemove.sortedDescending().forEach { index ->
            newItems.removeAt(index)
        }
        return newItems
    }
}
