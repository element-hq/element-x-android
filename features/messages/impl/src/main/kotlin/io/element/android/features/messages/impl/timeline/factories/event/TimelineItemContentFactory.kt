/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories.event

import dev.zacsweers.metro.Inject
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRtcNotificationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.CallNotifyContent
import io.element.android.libraries.matrix.api.timeline.item.event.EventContent
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import io.element.android.libraries.matrix.api.timeline.item.event.LegacyCallInviteContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileChangeContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.api.timeline.item.event.RedactedContent
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.libraries.matrix.api.timeline.item.event.StickerContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownContent
import io.element.android.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName

@Inject
class TimelineItemContentFactory(
    private val messageFactory: TimelineItemContentMessageFactory,
    private val redactedMessageFactory: TimelineItemContentRedactedFactory,
    private val stickerFactory: TimelineItemContentStickerFactory,
    private val pollFactory: TimelineItemContentPollFactory,
    private val utdFactory: TimelineItemContentUTDFactory,
    private val roomMembershipFactory: TimelineItemContentRoomMembershipFactory,
    private val profileChangeFactory: TimelineItemContentProfileChangeFactory,
    private val stateFactory: TimelineItemContentStateFactory,
    private val failedToParseMessageFactory: TimelineItemContentFailedToParseMessageFactory,
    private val failedToParseStateFactory: TimelineItemContentFailedToParseStateFactory,
    private val sessionId: SessionId,
) {
    suspend fun create(eventTimelineItem: EventTimelineItem): TimelineItemEventContent {
        return create(
            itemContent = eventTimelineItem.content,
            eventId = eventTimelineItem.eventId,
            isEditable = eventTimelineItem.isEditable,
            sender = eventTimelineItem.sender,
            senderProfile = eventTimelineItem.senderProfile,
        )
    }

    suspend fun create(
        itemContent: EventContent,
        eventId: EventId?,
        isEditable: Boolean,
        sender: UserId,
        senderProfile: ProfileDetails,
    ): TimelineItemEventContent {
        val isOutgoing = sessionId == sender
        return when (itemContent) {
            is FailedToParseMessageLikeContent -> failedToParseMessageFactory.create(itemContent)
            is FailedToParseStateContent -> failedToParseStateFactory.create(itemContent)
            is MessageContent -> {
                val senderDisambiguatedDisplayName = senderProfile.getDisambiguatedDisplayName(sender)
                messageFactory.create(
                    content = itemContent,
                    senderDisambiguatedDisplayName = senderDisambiguatedDisplayName,
                    eventId = eventId,
                )
            }
            is ProfileChangeContent -> {
                val senderDisambiguatedDisplayName = senderProfile.getDisambiguatedDisplayName(sender)
                profileChangeFactory.create(itemContent, isOutgoing, sender, senderDisambiguatedDisplayName)
            }
            is RedactedContent -> redactedMessageFactory.create(itemContent)
            is RoomMembershipContent -> {
                val senderDisambiguatedDisplayName = senderProfile.getDisambiguatedDisplayName(sender)
                roomMembershipFactory.create(itemContent, isOutgoing, sender, senderDisambiguatedDisplayName)
            }
            is LegacyCallInviteContent -> TimelineItemLegacyCallInviteContent
            is StateContent -> {
                val senderDisambiguatedDisplayName = senderProfile.getDisambiguatedDisplayName(sender)
                stateFactory.create(itemContent, isOutgoing, sender, senderDisambiguatedDisplayName)
            }
            is StickerContent -> stickerFactory.create(itemContent)
            is PollContent -> pollFactory.create(eventId, isEditable, isOutgoing, itemContent)
            is UnableToDecryptContent -> utdFactory.create(itemContent)
            is CallNotifyContent -> TimelineItemRtcNotificationContent()
            is UnknownContent -> TimelineItemUnknownContent
        }
    }
}
