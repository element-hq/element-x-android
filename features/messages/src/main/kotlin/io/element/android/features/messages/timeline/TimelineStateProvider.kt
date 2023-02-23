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

package io.element.android.features.messages.timeline

import io.element.android.features.messages.timeline.model.AggregatedReaction
import io.element.android.features.messages.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.timeline.model.TimelineItem
import io.element.android.features.messages.timeline.model.TimelineItemReactions
import io.element.android.features.messages.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.timeline.model.event.TimelineItemTextContent
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.core.EventId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

fun aTimelineState() = TimelineState(
    timelineItems = persistentListOf(),
    highlightedEventId = null,
    eventSink = {}
)

internal fun aTimelineItemList(content: TimelineItemEventContent): ImmutableList<TimelineItem> {
    return persistentListOf(
        // 3 items (First Middle Last) with isMine = false
        aMessageEvent(
            isMine = false,
            content = content,
            groupPosition = TimelineItemGroupPosition.Last
        ),
        aMessageEvent(
            isMine = false,
            content = content,
            groupPosition = TimelineItemGroupPosition.Middle
        ),
        aMessageEvent(
            isMine = false,
            content = content,
            groupPosition = TimelineItemGroupPosition.First
        ),
        // 3 items (First Middle Last) with isMine = true
        aMessageEvent(
            isMine = true,
            content = content,
            groupPosition = TimelineItemGroupPosition.Last
        ),
        aMessageEvent(
            isMine = true,
            content = content,
            groupPosition = TimelineItemGroupPosition.Middle
        ),
        aMessageEvent(
            isMine = true,
            content = content,
            groupPosition = TimelineItemGroupPosition.First
        ),
    )
}

internal fun aMessageEvent(
    isMine: Boolean = false,
    content: TimelineItemEventContent = aTimelineItemContent(),
    groupPosition: TimelineItemGroupPosition = TimelineItemGroupPosition.First
): TimelineItem.Event {
    return TimelineItem.Event(
        id = EventId(Math.random().toString()),
        senderId = "@senderId",
        senderAvatar = AvatarData("@senderId", "sender"),
        content = content,
        reactionsState = TimelineItemReactions(
            persistentListOf(
                AggregatedReaction("👍", "1")
            )
        ),
        isMine = isMine,
        senderDisplayName = "sender",
        groupPosition = groupPosition,
    )
}

internal fun aTimelineItemContent(): TimelineItemEventContent {
    return TimelineItemTextContent(
        body = "Text",
        htmlDocument = null
    )
}
