/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import io.element.android.features.messages.impl.crypto.sendfailure.resolve.ResolveVerifiedUserSendFailureState
import io.element.android.features.messages.impl.crypto.sendfailure.resolve.aResolveVerifiedUserSendFailureState
import io.element.android.features.messages.impl.timeline.components.receipt.aReadReceiptData
import io.element.android.features.messages.impl.timeline.model.NewEventState
import io.element.android.features.messages.impl.timeline.model.ReadReceiptData
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.TimelineItemReactions
import io.element.android.features.messages.impl.timeline.model.TimelineItemReadReceipts
import io.element.android.features.messages.impl.timeline.model.anAggregatedReaction
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemStateEventContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.features.messages.impl.timeline.model.virtual.aTimelineItemDaySeparatorModel
import io.element.android.features.messages.impl.typing.TypingNotificationState
import io.element.android.features.messages.impl.typing.aTypingNotificationState
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.matrix.api.timeline.item.event.MessageShield
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.matrix.ui.messages.reply.aProfileTimelineDetailsReady
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import java.util.UUID
import kotlin.random.Random

fun aTimelineState(
    timelineItems: ImmutableList<TimelineItem> = persistentListOf(),
    renderReadReceipts: Boolean = false,
    timelineRoomInfo: TimelineRoomInfo = aTimelineRoomInfo(),
    focusedEventIndex: Int = -1,
    isLive: Boolean = true,
    messageShield: MessageShield? = null,
    resolveVerifiedUserSendFailureState: ResolveVerifiedUserSendFailureState = aResolveVerifiedUserSendFailureState(),
    eventSink: (TimelineEvents) -> Unit = {},
): TimelineState {
    val focusedEventId = timelineItems.filterIsInstance<TimelineItem.Event>().getOrNull(focusedEventIndex)?.eventId
    val focusRequestState = if (focusedEventId != null) {
        FocusRequestState.Success(focusedEventId, focusedEventIndex)
    } else {
        FocusRequestState.None
    }
    return TimelineState(
        timelineItems = timelineItems,
        timelineRoomInfo = timelineRoomInfo,
        renderReadReceipts = renderReadReceipts,
        newEventState = NewEventState.None,
        isLive = isLive,
        focusRequestState = focusRequestState,
        messageShield = messageShield,
        resolveVerifiedUserSendFailureState = resolveVerifiedUserSendFailureState,
        eventSink = eventSink,
    )
}

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
            sendState = LocalEventSendState.Failed.Unknown("Message failed to send"),
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
            sendState = LocalEventSendState.Failed.Unknown("Message failed to send"),
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
    return TimelineItem.Virtual(
        id = UniqueId(UUID.randomUUID().toString()),
        model = aTimelineItemDaySeparatorModel("Today"),
    )
}

internal fun aTimelineItemEvent(
    eventId: EventId = EventId("\$" + Random.nextInt().toString()),
    transactionId: TransactionId? = null,
    isMine: Boolean = false,
    isEditable: Boolean = false,
    canBeRepliedTo: Boolean = false,
    senderDisplayName: String = "Sender",
    displayNameAmbiguous: Boolean = false,
    content: TimelineItemEventContent = aTimelineItemTextContent(),
    groupPosition: TimelineItemGroupPosition = TimelineItemGroupPosition.None,
    sendState: LocalEventSendState? = null,
    inReplyTo: InReplyToDetails? = null,
    isThreaded: Boolean = false,
    debugInfo: TimelineItemDebugInfo = aTimelineItemDebugInfo(),
    timelineItemReactions: TimelineItemReactions = aTimelineItemReactions(),
    readReceiptState: TimelineItemReadReceipts = aTimelineItemReadReceipts(),
    messageShield: MessageShield? = null,
): TimelineItem.Event {
    return TimelineItem.Event(
        id = UniqueId(UUID.randomUUID().toString()),
        eventId = eventId,
        transactionId = transactionId,
        senderId = UserId("@senderId:domain"),
        senderAvatar = AvatarData("@senderId:domain", "sender", size = AvatarSize.TimelineSender),
        content = content,
        reactionsState = timelineItemReactions,
        readReceiptState = readReceiptState,
        sentTime = "12:34",
        isMine = isMine,
        isEditable = isEditable,
        canBeRepliedTo = canBeRepliedTo,
        senderProfile = aProfileTimelineDetailsReady(
            displayName = senderDisplayName,
            displayNameAmbiguous = displayNameAmbiguous,
        ),
        groupPosition = groupPosition,
        localSendState = sendState,
        inReplyTo = inReplyTo,
        debugInfoProvider = { debugInfo },
        isThreaded = isThreaded,
        origin = null,
        messageShield = messageShield,
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
    model,
    originalJson,
    latestEditedJson
)

internal fun aTimelineItemReadReceipts(
    receipts: List<ReadReceiptData> = emptyList(),
): TimelineItemReadReceipts {
    return TimelineItemReadReceipts(
        receipts = receipts.toImmutableList(),
    )
}

internal fun aGroupedEvents(
    id: UniqueId = UniqueId("0"),
    withReadReceipts: Boolean = false,
): TimelineItem.GroupedEvents {
    val event1 = aTimelineItemEvent(
        isMine = true,
        content = aTimelineItemStateEventContent(),
        groupPosition = TimelineItemGroupPosition.None,
        readReceiptState = TimelineItemReadReceipts(
            receipts = (if (withReadReceipts) listOf(aReadReceiptData(0)) else emptyList()).toImmutableList()
        ),
    )
    val event2 = aTimelineItemEvent(
        isMine = true,
        content = aTimelineItemStateEventContent(body = "Another state event"),
        groupPosition = TimelineItemGroupPosition.None,
        readReceiptState = TimelineItemReadReceipts(
            receipts = (if (withReadReceipts) listOf(aReadReceiptData(1)) else emptyList()).toImmutableList()
        ),
    )
    val events = listOf(event1, event2)
    return TimelineItem.GroupedEvents(
        id = id,
        events = events.toImmutableList(),
        aggregatedReadReceipts = events.flatMap { it.readReceiptState.receipts }.toImmutableList(),
    )
}

internal fun aTimelineRoomInfo(
    name: String = "Room name",
    isDm: Boolean = false,
    userHasPermissionToSendMessage: Boolean = true,
    pinnedEventIds: List<EventId> = emptyList(),
    typingNotificationState: TypingNotificationState = aTypingNotificationState(),
) = TimelineRoomInfo(
    isDm = isDm,
    name = name,
    userHasPermissionToSendMessage = userHasPermissionToSendMessage,
    userHasPermissionToSendReaction = true,
    isCallOngoing = false,
    pinnedEventIds = pinnedEventIds,
    typingNotificationState = typingNotificationState,
)
