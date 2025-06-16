/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.runtime.Composable
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.aTimelineRoomInfo
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.features.messages.impl.timeline.protection.aTimelineProtectionState

// For previews
@Composable
internal fun ATimelineItemEventRow(
    event: TimelineItem.Event,
    timelineRoomInfo: TimelineRoomInfo = aTimelineRoomInfo(),
    renderReadReceipts: Boolean = false,
    isLastOutgoingMessage: Boolean = false,
    timelineProtectionState: TimelineProtectionState = aTimelineProtectionState(),
) = TimelineItemEventRow(
    event = event,
    timelineRoomInfo = timelineRoomInfo,
    renderReadReceipts = renderReadReceipts,
    timelineProtectionState = timelineProtectionState,
    isLastOutgoingMessage = isLastOutgoingMessage,
    onEventClick = {},
    onLongClick = {},
    onLinkClick = {},
    onLinkLongClick = {},
    onUserDataClick = {},
    inReplyToClick = {},
    onReactionClick = { _, _ -> },
    onReactionLongClick = { _, _ -> },
    onMoreReactionsClick = {},
    onReadReceiptClick = {},
    onSwipeToReply = {},
    eventSink = {},
)
