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

import io.element.android.libraries.matrix.api.core.UserId
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
        currentUserId: UserId,
        isDm: Boolean,
        isAtStartOfTimeline: Boolean
    ): List<MatrixTimelineItem> {
        if (!isDm || !isAtStartOfTimeline) return items
        // This is usually index 1
        val roomCreationEventIndex = items.indexOfFirst {
            val stateEventContent = (it as? MatrixTimelineItem.Event)?.event?.content as? StateContent
            stateEventContent?.content is OtherState.RoomCreate
        }
        // This is usually index 2
        val selfUserJoinedEventIndex = items.indexOfFirst {
            val event = (it as? MatrixTimelineItem.Event)?.event
            val stateEventContent = event?.content as? RoomMembershipContent
            stateEventContent?.change == MembershipChange.JOINED && event.sender == currentUserId
        }
        val newItems = items.toMutableList()
        if (selfUserJoinedEventIndex in 0..newItems.size) {
            newItems.removeAt(selfUserJoinedEventIndex)
        }
        if (roomCreationEventIndex in 0..newItems.size) {
            newItems.removeAt(roomCreationEventIndex)
        }
        return newItems
    }
}
