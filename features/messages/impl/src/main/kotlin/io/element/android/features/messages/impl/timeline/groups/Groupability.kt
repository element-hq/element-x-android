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
import io.element.android.libraries.matrix.api.timeline.item.event.LiveLocationContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileChangeContent
import io.element.android.libraries.matrix.api.timeline.item.event.RedactedContent
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.libraries.matrix.api.timeline.item.event.StickerContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownContent
import kotlinx.collections.immutable.toImmutableList

/**
 * Return true when every event in the group is a redacted (deleted) message, i.e. the group is a
 * collapsed run of deleted messages rather than the usual run of room state changes. Used to pick
 * the group header label. An empty group is not considered a redacted group.
 */
internal fun TimelineItem.GroupedEvents.isRedactedMessagesGroup(): Boolean =
    events.isNotEmpty() && events.all { it.content is TimelineItemRedactedContent }

// Runs shorter than this are left as individual "Message removed" tiles, like element-web.
internal const val MIN_REDACTED_RUN_SIZE = 3

/**
 * Collapse runs of [MIN_REDACTED_RUN_SIZE] or more consecutive redacted events into a single
 * [TimelineItem.GroupedEvents], so they render as one expandable "N deleted messages" block the way
 * element-web does. Shorter runs and every non-redacted item are passed through untouched, day
 * dividers included. The grouped events are stored oldest-first to match the existing grouper, and
 * the group id is derived from the newest event of the run (its first element in this newest-first
 * list) so it stays stable as older history pages in and the run grows at its older end.
 */
internal fun List<TimelineItem>.collapseRedactedRuns(): List<TimelineItem> {
    val result = mutableListOf<TimelineItem>()
    val run = mutableListOf<TimelineItem.Event>()

    fun flushRun() {
        when {
            run.isEmpty() -> Unit
            run.size < MIN_REDACTED_RUN_SIZE -> result.addAll(run)
            else -> result.add(
                TimelineItem.GroupedEvents(
                    id = computeGroupIdWith(run.first()),
                    events = run.reversed().toImmutableList(),
                    aggregatedReadReceipts = run.flatMap { it.readReceiptState.receipts }.toImmutableList(),
                )
            )
        }
        run.clear()
    }

    for (item in this) {
        if (item is TimelineItem.Event && item.content is TimelineItemRedactedContent) {
            run.add(item)
        } else {
            flushRun()
            result.add(item)
        }
    }
    flushRun()
    return result
}

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
        is UnableToDecryptContent,
        is LiveLocationContent -> true
        // Can't be grouped
        is FailedToParseStateContent,
        is ProfileChangeContent,
        is RoomMembershipContent,
        UnknownContent,
        is LegacyCallInviteContent,
        is CallNotifyContent,
        is StateContent -> false
    }
}
