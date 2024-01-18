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

import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.api.timeline.item.event.OtherState
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent

/**
 * This timeline post-processor removes the room creation event and the self-join event from the timeline for DMs.
 */
class DmBeginningTimelineProcessor {
    fun process(
        items: List<MatrixTimelineItem>,
        isDm: Boolean,
        isAtStartOfTimeline: Boolean
    ): List<MatrixTimelineItem> {
        if (!isDm || !isAtStartOfTimeline) return items

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
}
