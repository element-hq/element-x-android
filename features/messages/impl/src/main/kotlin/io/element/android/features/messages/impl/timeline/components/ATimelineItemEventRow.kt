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

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.runtime.Composable
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.aTimelineRoomInfo
import io.element.android.features.messages.impl.timeline.model.TimelineItem

// For previews
@Composable
internal fun ATimelineItemEventRow(
    event: TimelineItem.Event,
    timelineRoomInfo: TimelineRoomInfo = aTimelineRoomInfo(),
    renderReadReceipts: Boolean = false,
    isLastOutgoingMessage: Boolean = false,
    isHighlighted: Boolean = false,
) = TimelineItemEventRow(
    event = event,
    timelineRoomInfo = timelineRoomInfo,
    renderReadReceipts = renderReadReceipts,
    isLastOutgoingMessage = isLastOutgoingMessage,
    isHighlighted = isHighlighted,
    onClick = {},
    onLongClick = {},
    onUserDataClick = {},
    onLinkClick = {},
    inReplyToClick = {},
    onReactionClick = { _, _ -> },
    onReactionLongClick = { _, _ -> },
    onMoreReactionsClick = {},
    onReadReceiptClick = {},
    onSwipeToReply = {},
    onTimestampClick = {},
    eventSink = {},
)
