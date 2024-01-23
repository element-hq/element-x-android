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
import androidx.compose.ui.Modifier
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.session.SessionState
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId

@Composable
internal fun TimelineItemRow(
    timelineItem: TimelineItem,
    timelineRoomInfo: TimelineRoomInfo,
    isLastOutgoingMessage: Boolean,
    highlightedItem: String?,
    sessionState: SessionState,
    onUserDataClick: (UserId) -> Unit,
    onClick: (TimelineItem.Event) -> Unit,
    onLongClick: (TimelineItem.Event) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onReactionClick: (key: String, TimelineItem.Event) -> Unit,
    onReactionLongClick: (key: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (TimelineItem.Event) -> Unit,
    onReadReceiptClick: (TimelineItem.Event) -> Unit,
    onTimestampClicked: (TimelineItem.Event) -> Unit,
    onSwipeToReply: (TimelineItem.Event) -> Unit,
    eventSink: (TimelineEvents) -> Unit,
    modifier: Modifier = Modifier
) {
    when (timelineItem) {
        is TimelineItem.Virtual -> {
            TimelineItemVirtualRow(
                virtual = timelineItem,
                sessionState = sessionState,
                modifier = modifier,
            )
        }
        is TimelineItem.Event -> {
            if (timelineItem.content is TimelineItemStateContent) {
                TimelineItemStateEventRow(
                    event = timelineItem,
                    isLastOutgoingMessage = isLastOutgoingMessage,
                    isHighlighted = highlightedItem == timelineItem.identifier(),
                    onClick = { onClick(timelineItem) },
                    onReadReceiptsClick = onReadReceiptClick,
                    onLongClick = { onLongClick(timelineItem) },
                    eventSink = eventSink,
                    modifier = modifier,
                )
            } else {
                TimelineItemEventRow(
                    event = timelineItem,
                    timelineRoomInfo = timelineRoomInfo,
                    isLastOutgoingMessage = isLastOutgoingMessage,
                    isHighlighted = highlightedItem == timelineItem.identifier(),
                    onClick = { onClick(timelineItem) },
                    onLongClick = { onLongClick(timelineItem) },
                    onUserDataClick = onUserDataClick,
                    inReplyToClick = inReplyToClick,
                    onReactionClick = onReactionClick,
                    onReactionLongClick = onReactionLongClick,
                    onMoreReactionsClick = onMoreReactionsClick,
                    onReadReceiptClick = onReadReceiptClick,
                    onTimestampClicked = onTimestampClicked,
                    onSwipeToReply = { onSwipeToReply(timelineItem) },
                    eventSink = eventSink,
                    modifier = modifier,
                )
            }
        }
        is TimelineItem.GroupedEvents -> {
            TimelineItemGroupedEventsRow(
                timelineItem = timelineItem,
                timelineRoomInfo = timelineRoomInfo,
                isLastOutgoingMessage = isLastOutgoingMessage,
                highlightedItem = highlightedItem,
                sessionState = sessionState,
                onClick = onClick,
                onLongClick = onLongClick,
                inReplyToClick = inReplyToClick,
                onUserDataClick = onUserDataClick,
                onTimestampClicked = onTimestampClicked,
                onReactionClick = onReactionClick,
                onReactionLongClick = onReactionLongClick,
                onMoreReactionsClick = onMoreReactionsClick,
                onReadReceiptClick = onReadReceiptClick,
                eventSink = eventSink,
                modifier = modifier,
            )
        }
    }
}
