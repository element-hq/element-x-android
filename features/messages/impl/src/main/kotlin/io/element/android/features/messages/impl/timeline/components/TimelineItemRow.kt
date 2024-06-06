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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemCallNotifyContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.libraries.designsystem.text.toPx
import io.element.android.libraries.designsystem.theme.highlightedMessageBackgroundColor
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId

@Composable
internal fun TimelineItemRow(
    timelineItem: TimelineItem,
    timelineRoomInfo: TimelineRoomInfo,
    renderReadReceipts: Boolean,
    isLastOutgoingMessage: Boolean,
    focusedEventId: EventId?,
    onUserDataClick: (UserId) -> Unit,
    onLinkClick: (String) -> Unit,
    onClick: (TimelineItem.Event) -> Unit,
    onLongClick: (TimelineItem.Event) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onReactionClick: (key: String, TimelineItem.Event) -> Unit,
    onReactionLongClick: (key: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (TimelineItem.Event) -> Unit,
    onReadReceiptClick: (TimelineItem.Event) -> Unit,
    onTimestampClick: (TimelineItem.Event) -> Unit,
    onSwipeToReply: (TimelineItem.Event) -> Unit,
    onJoinCallClick: () -> Unit,
    eventSink: (TimelineEvents.EventFromTimelineItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundModifier = if (timelineItem.isEvent(focusedEventId)) {
        val focusedEventOffset = if ((timelineItem as? TimelineItem.Event)?.showSenderInformation == true) {
            14.dp
        } else {
            2.dp
        }
        Modifier.focusedEvent(focusedEventOffset)
    } else {
        Modifier
    }
    Box(modifier = modifier.then(backgroundModifier)) {
        when (timelineItem) {
            is TimelineItem.Virtual -> {
                TimelineItemVirtualRow(
                    virtual = timelineItem,
                    timelineRoomInfo = timelineRoomInfo,
                    eventSink = eventSink,
                )
            }
            is TimelineItem.Event -> {
                when (timelineItem.content) {
                    is TimelineItemStateContent, is TimelineItemLegacyCallInviteContent -> {
                        TimelineItemStateEventRow(
                            event = timelineItem,
                            renderReadReceipts = renderReadReceipts,
                            isLastOutgoingMessage = isLastOutgoingMessage,
                            isHighlighted = timelineItem.isEvent(focusedEventId),
                            onClick = { onClick(timelineItem) },
                            onReadReceiptsClick = onReadReceiptClick,
                            onLongClick = { onLongClick(timelineItem) },
                            eventSink = eventSink,
                        )
                    }
                    is TimelineItemCallNotifyContent -> {
                        TimelineItemCallNotifyView(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                            event = timelineItem,
                            isCallOngoing = timelineRoomInfo.isCallOngoing,
                            onLongClick = onLongClick,
                            onJoinCallClick = onJoinCallClick,
                        )
                    }
                    else -> {
                        TimelineItemEventRow(
                            event = timelineItem,
                            timelineRoomInfo = timelineRoomInfo,
                            renderReadReceipts = renderReadReceipts,
                            isLastOutgoingMessage = isLastOutgoingMessage,
                            isHighlighted = timelineItem.isEvent(focusedEventId),
                            onClick = { onClick(timelineItem) },
                            onLongClick = { onLongClick(timelineItem) },
                            onUserDataClick = onUserDataClick,
                            onLinkClick = onLinkClick,
                            inReplyToClick = inReplyToClick,
                            onReactionClick = onReactionClick,
                            onReactionLongClick = onReactionLongClick,
                            onMoreReactionsClick = onMoreReactionsClick,
                            onReadReceiptClick = onReadReceiptClick,
                            onTimestampClick = onTimestampClick,
                            onSwipeToReply = { onSwipeToReply(timelineItem) },
                            eventSink = eventSink,
                        )
                    }
                }
            }
            is TimelineItem.GroupedEvents -> {
                TimelineItemGroupedEventsRow(
                    timelineItem = timelineItem,
                    timelineRoomInfo = timelineRoomInfo,
                    renderReadReceipts = renderReadReceipts,
                    isLastOutgoingMessage = isLastOutgoingMessage,
                    focusedEventId = focusedEventId,
                    onClick = onClick,
                    onLongClick = onLongClick,
                    inReplyToClick = inReplyToClick,
                    onUserDataClick = onUserDataClick,
                    onLinkClick = onLinkClick,
                    onTimestampClick = onTimestampClick,
                    onReactionClick = onReactionClick,
                    onReactionLongClick = onReactionLongClick,
                    onMoreReactionsClick = onMoreReactionsClick,
                    onReadReceiptClick = onReadReceiptClick,
                    eventSink = eventSink,
                )
            }
        }
    }
}

@Suppress("ModifierComposable")
@Composable
private fun Modifier.focusedEvent(
    focusedEventOffset: Dp
): Modifier {
    val highlightedLineColor = ElementTheme.colors.textActionAccent
    val gradientColors = listOf(
        ElementTheme.colors.highlightedMessageBackgroundColor,
        ElementTheme.materialColors.background
    )
    val verticalOffset = focusedEventOffset.toPx()
    val verticalRatio = 0.7f
    return drawWithCache {
        val brush = Brush.verticalGradient(
            colors = gradientColors,
            endY = size.height * verticalRatio,
        )
        onDrawBehind {
            drawRect(
                brush,
                topLeft = Offset(0f, verticalOffset),
                size = Size(size.width, size.height * verticalRatio)
            )
            drawLine(
                highlightedLineColor,
                start = Offset(0f, verticalOffset),
                end = Offset(size.width, verticalOffset)
            )
        }
    }.padding(top = 4.dp)
}
