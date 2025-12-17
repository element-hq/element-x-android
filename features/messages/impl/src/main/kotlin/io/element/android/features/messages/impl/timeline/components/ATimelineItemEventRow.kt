/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.runtime.Composable
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.aTimelineRoomInfo
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.features.messages.impl.timeline.protection.aTimelineProtectionState
import io.element.android.libraries.matrix.api.timeline.Timeline

// For previews
@Composable
internal fun ATimelineItemEventRow(
    event: TimelineItem.Event,
    timelineMode: Timeline.Mode = Timeline.Mode.Live,
    timelineRoomInfo: TimelineRoomInfo = aTimelineRoomInfo(),
    renderReadReceipts: Boolean = false,
    isLastOutgoingMessage: Boolean = false,
    timelineProtectionState: TimelineProtectionState = aTimelineProtectionState(),
    displayThreadSummaries: Boolean = false,
) = TimelineItemEventRow(
    event = event,
    timelineMode = timelineMode,
    timelineRoomInfo = timelineRoomInfo,
    renderReadReceipts = renderReadReceipts,
    timelineProtectionState = timelineProtectionState,
    isLastOutgoingMessage = isLastOutgoingMessage,
    displayThreadSummaries = displayThreadSummaries,
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
