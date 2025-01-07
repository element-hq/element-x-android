/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories.event

import io.element.android.features.messages.impl.timeline.model.event.TimelineItemCallNotifyContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import io.element.android.libraries.matrix.api.timeline.item.event.CallNotifyContent
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import io.element.android.libraries.matrix.api.timeline.item.event.LegacyCallInviteContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileChangeContent
import io.element.android.libraries.matrix.api.timeline.item.event.RedactedContent
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.libraries.matrix.api.timeline.item.event.StickerContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownContent
import io.element.android.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import javax.inject.Inject

class TimelineItemContentFactory @Inject constructor(
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
) {
    suspend fun create(eventTimelineItem: EventTimelineItem): TimelineItemEventContent {
        return when (val itemContent = eventTimelineItem.content) {
            is FailedToParseMessageLikeContent -> failedToParseMessageFactory.create(itemContent)
            is FailedToParseStateContent -> failedToParseStateFactory.create(itemContent)
            is MessageContent -> {
                val senderDisambiguatedDisplayName = eventTimelineItem.senderProfile.getDisambiguatedDisplayName(eventTimelineItem.sender)
                messageFactory.create(
                    content = itemContent,
                    senderDisambiguatedDisplayName = senderDisambiguatedDisplayName,
                    eventId = eventTimelineItem.eventId,
                )
            }
            is ProfileChangeContent -> profileChangeFactory.create(eventTimelineItem)
            is RedactedContent -> redactedMessageFactory.create(itemContent)
            is RoomMembershipContent -> roomMembershipFactory.create(eventTimelineItem)
            is LegacyCallInviteContent -> TimelineItemLegacyCallInviteContent
            is StateContent -> stateFactory.create(eventTimelineItem)
            is StickerContent -> stickerFactory.create(itemContent)
            is PollContent -> pollFactory.create(eventTimelineItem, itemContent)
            is UnableToDecryptContent -> utdFactory.create(itemContent)
            is CallNotifyContent -> TimelineItemCallNotifyContent()
            is UnknownContent -> TimelineItemUnknownContent
        }
    }
}
