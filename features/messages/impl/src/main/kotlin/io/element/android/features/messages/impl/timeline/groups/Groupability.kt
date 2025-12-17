/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.groups

import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAudioContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEncryptedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemProfileChangeContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRoomMembershipContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRtcNotificationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.CallNotifyContent
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

/**
 * Return true if the Event can be grouped in a collapse/expand block
 * When [canBeGrouped] returns a value, [canBeDisplayedInBubbleBlock] MUST return the opposite value.
 * Since the receiving type are not the same, the two functions exist.
 */
internal fun TimelineItem.Event.canBeGrouped(): Boolean {
    return when (content) {
        is TimelineItemTextBasedContent,
        is TimelineItemEncryptedContent,
        is TimelineItemImageContent,
        is TimelineItemStickerContent,
        is TimelineItemFileContent,
        is TimelineItemVideoContent,
        is TimelineItemAudioContent,
        is TimelineItemLocationContent,
        is TimelineItemPollContent,
        is TimelineItemVoiceContent,
        TimelineItemRedactedContent,
        TimelineItemUnknownContent,
        is TimelineItemLegacyCallInviteContent,
        is TimelineItemRtcNotificationContent -> false
        is TimelineItemProfileChangeContent,
        is TimelineItemRoomMembershipContent,
        is TimelineItemStateEventContent -> true
    }
}

/**
 * Return true if the Event can be grouped in a block of message bubbles.
 * When [canBeDisplayedInBubbleBlock] returns a value, [canBeGrouped] MUST return the opposite value.
 * Since the receiving type are not the same, the two functions exist.
 */
internal fun MatrixTimelineItem.Event.canBeDisplayedInBubbleBlock(): Boolean {
    return when (event.content) {
        // Can be grouped
        is FailedToParseMessageLikeContent,
        is MessageContent,
        RedactedContent,
        is StickerContent,
        is PollContent,
        is UnableToDecryptContent -> true
        // Can't be grouped
        is FailedToParseStateContent,
        is ProfileChangeContent,
        is RoomMembershipContent,
        UnknownContent,
        is LegacyCallInviteContent,
        CallNotifyContent,
        is StateContent -> false
    }
}
