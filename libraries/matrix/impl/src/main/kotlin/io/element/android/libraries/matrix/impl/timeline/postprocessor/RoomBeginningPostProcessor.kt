/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.matrix.impl.timeline.postprocessor

import androidx.annotation.VisibleForTesting
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
        hasMoreToLoadBackwards: Boolean
    ): List<MatrixTimelineItem> {
        return when {
            mode == Timeline.Mode.FOCUSED_ON_PINNED_EVENTS -> items
            hasMoreToLoadBackwards -> items
            isDm -> processForDM(items)
            else -> processForRoom(items)
        }
    }

    private fun processForRoom(items: List<MatrixTimelineItem>): List<MatrixTimelineItem> {
        if (items.hasEncryptionHistoryBanner()) return items
        val roomBeginningItem = createRoomBeginningItem()
        return listOf(roomBeginningItem) + items
    }

    private fun processForDM(items: List<MatrixTimelineItem>): List<MatrixTimelineItem> {
        // Find room creation event. This is usually index 0
        val roomCreationEventIndex = items.indexOfFirst {
            val stateEventContent = (it as? MatrixTimelineItem.Event)?.event?.content as? StateContent
            stateEventContent?.content is OtherState.RoomCreate
        }

        // Find self-join event for room creator. This is usually index 1
        val roomCreatorUserId = (items.getOrNull(roomCreationEventIndex) as? MatrixTimelineItem.Event)?.event?.sender
        val selfUserJoinedEventIndex = roomCreatorUserId?.let { creatorUserId ->
            items.indexOfFirst {
                val stateEventContent = (it as? MatrixTimelineItem.Event)?.event?.content as? RoomMembershipContent
                stateEventContent?.change == MembershipChange.JOINED && stateEventContent.userId == creatorUserId
            }
        } ?: -1

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
            uniqueId = VirtualTimelineItem.RoomBeginning.toString(),
            virtual = VirtualTimelineItem.RoomBeginning
        )
    }
}
