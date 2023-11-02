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

import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.TimelineItemReactions
import io.element.android.features.messages.impl.timeline.model.anAggregatedReaction
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemStateEventContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.features.messages.impl.timeline.model.virtual.aTimelineItemDaySeparatorModel
import io.element.android.features.messages.impl.timeline.session.aSessionState
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.libraries.matrix.api.timeline.item.event.InReplyTo
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import java.util.UUID
import kotlin.random.Random

fun aTimelineState(timelineItems: ImmutableList<TimelineItem> = persistentListOf()) = TimelineState(
    timelineItems = timelineItems,
    paginationState = MatrixTimeline.PaginationState(isBackPaginating = false, hasMoreToLoadBackwards = true),
    highlightedEventId = null,
    userHasPermissionToSendMessage = true,
    hasNewItems = false,
    sessionState = aSessionState(
        isSessionVerified = true,
        isKeyBackupEnabled = true,
    ),
    eventSink = {},
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
            sendState = LocalEventSendState.SendingFailed("Message failed to send"),
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
            sendState = LocalEventSendState.SendingFailed("Message failed to send"),
        ),
        aTimelineItemEvent(
            isMine = true,
            content = content,
            groupPosition = TimelineItemGroupPosition.First
        ),
        // A grouped event on top of it
        aGroupedEvents(),
        // A day separator
        aTimelineItemDaySeparator(),
    )
}

fun aTimelineItemDaySeparator(): TimelineItem.Virtual {
    return TimelineItem.Virtual(UUID.randomUUID().toString(), aTimelineItemDaySeparatorModel("Today"))
}

internal fun aTimelineItemEvent(
    eventId: EventId = EventId("\$" + Random.nextInt().toString()),
    transactionId: TransactionId? = null,
    isMine: Boolean = false,
    senderDisplayName: String = "Sender",
    content: TimelineItemEventContent = aTimelineItemTextContent(),
    groupPosition: TimelineItemGroupPosition = TimelineItemGroupPosition.None,
    sendState: LocalEventSendState = LocalEventSendState.Sent(eventId),
    inReplyTo: InReplyTo? = null,
    isThreaded: Boolean = false,
    debugInfo: TimelineItemDebugInfo = aTimelineItemDebugInfo(),
    timelineItemReactions: TimelineItemReactions = aTimelineItemReactions(),
): TimelineItem.Event {
    return TimelineItem.Event(
        id = UUID.randomUUID().toString(),
        eventId = eventId,
        transactionId = transactionId,
        senderId = UserId("@senderId:domain"),
        senderAvatar = AvatarData("@senderId:domain", "sender", size = AvatarSize.TimelineSender),
        content = content,
        reactionsState = timelineItemReactions,
        sentTime = "12:34",
        isMine = isMine,
        senderDisplayName = senderDisplayName,
        groupPosition = groupPosition,
        localSendState = sendState,
        inReplyTo = inReplyTo,
        debugInfo = debugInfo,
        isThreaded = isThreaded,
        origin = null
    )
}

fun aTimelineItemReactions(
    count: Int = 1,
    isHighlighted: Boolean = false,
): TimelineItemReactions {
    val emojis = arrayOf("👍️", "😀️", "😁️", "😆️", "😅️", "🤣️", "🥰️", "😇️", "😊️", "😉️", "🙃️", "🙂️", "😍️", "🤗️", "🤭️")
    return TimelineItemReactions(
        reactions = buildList {
            repeat(count) { index ->
                val key = emojis[index % emojis.size]
                add(
                    anAggregatedReaction(
                        key = key,
                        count = index + 1,
                        isHighlighted = isHighlighted
                    )
                )
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

fun aGroupedEvents(id: Long = 0): TimelineItem.GroupedEvents {
    val event = aTimelineItemEvent(
        isMine = true,
        content = aTimelineItemStateEventContent(),
        groupPosition = TimelineItemGroupPosition.None
    )
    return TimelineItem.GroupedEvents(
        id = id.toString(),
        events = listOf(
            event,
            event,
        ).toImmutableList()
    )
}
