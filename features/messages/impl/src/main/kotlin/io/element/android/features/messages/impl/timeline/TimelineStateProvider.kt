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

package io.element.android.features.messages.impl.timeline

import io.element.android.features.messages.impl.timeline.model.AggregatedReaction
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.TimelineItemReactions
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemStateEventContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.libraries.matrix.api.timeline.item.event.EventSendState
import io.element.android.libraries.matrix.api.timeline.item.event.InReplyTo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlin.random.Random

fun aTimelineState(timelineItems: ImmutableList<TimelineItem> = persistentListOf()) = TimelineState(
    timelineItems = timelineItems,
    paginationState = MatrixTimeline.PaginationState(isBackPaginating = false, canBackPaginate = true),
    highlightedEventId = null,
    eventSink = {}
)

internal fun aTimelineItemList(content: TimelineItemEventContent): ImmutableList<TimelineItem> {
    return persistentListOf(
        // 3 items (First Middle Last) with isMine = false
        aTimelineItemEvent(
            isMine = false,
            content = content,
            groupPosition = TimelineItemGroupPosition.Last
        ),
        aTimelineItemEvent(
            isMine = false,
            content = content,
            groupPosition = TimelineItemGroupPosition.Middle,
            sendState = EventSendState.SendingFailed("Message failed to send"),
        ),
        aTimelineItemEvent(
            isMine = false,
            content = content,
            groupPosition = TimelineItemGroupPosition.First
        ),
        // A state event on top of it
        aTimelineItemEvent(
            isMine = false,
            content = aTimelineItemStateEventContent(),
            groupPosition = TimelineItemGroupPosition.None
        ),
        // 3 items (First Middle Last) with isMine = true
        aTimelineItemEvent(
            isMine = true,
            content = content,
            groupPosition = TimelineItemGroupPosition.Last
        ),
        aTimelineItemEvent(
            isMine = true,
            content = content,
            groupPosition = TimelineItemGroupPosition.Middle,
            sendState = EventSendState.SendingFailed("Message failed to send"),
        ),
        aTimelineItemEvent(
            isMine = true,
            content = content,
            groupPosition = TimelineItemGroupPosition.First
        ),
        // A state event on top of it
        aTimelineItemEvent(
            isMine = true,
            content = aTimelineItemStateEventContent(),
            groupPosition = TimelineItemGroupPosition.None
        ),
    )
}

internal fun aTimelineItemEvent(
    eventId: EventId = EventId("\$" + Random.nextInt().toString()),
    transactionId: String? = null,
    isMine: Boolean = false,
    content: TimelineItemEventContent = aTimelineItemTextContent(),
    groupPosition: TimelineItemGroupPosition = TimelineItemGroupPosition.None,
    sendState: EventSendState = EventSendState.Sent(eventId),
    inReplyTo: InReplyTo? = null,
    debugInfo: TimelineItemDebugInfo = aTimelineItemDebugInfo(),
    timelineItemReactions: TimelineItemReactions = aTimelineItemReactions(isMine = isMine),
): TimelineItem.Event {
    return TimelineItem.Event(
        id = eventId.value,
        eventId = eventId,
        transactionId = transactionId,
        senderId = UserId("@senderId:domain"),
        senderAvatar = AvatarData("@senderId:domain", "sender", size = AvatarSize.TimelineSender),
        content = content,
        reactionsState = timelineItemReactions,
        sentTime = "12:34",
        isMine = isMine,
        senderDisplayName = "Sender",
        groupPosition = groupPosition,
        sendState = sendState,
        inReplyTo = inReplyTo,
        debugInfo = debugInfo,
    )
}

fun aTimelineItemReactions(
    count: Int = 1,
    isMine: Boolean = true,
): TimelineItemReactions {
    return TimelineItemReactions(
        reactions = buildList {
            repeat(count) {
                add(AggregatedReaction(key = "👍", count = (it + 1).toString(), isOnMyMessage = isMine))
            }
        }.toPersistentList()
    )
}

internal fun aTimelineItemDebugInfo(
    model: String = "Rust(Model())",
    originalJson: String? = null,
    latestEditedJson: String? = null,
) = TimelineItemDebugInfo(
    model, originalJson, latestEditedJson
)
