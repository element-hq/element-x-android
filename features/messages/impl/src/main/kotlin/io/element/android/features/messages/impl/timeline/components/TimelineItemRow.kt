/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import io.element.android.features.messages.impl.timeline.components.event.TimelineItemEventContentView
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemCallNotifyContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionEvent
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.features.messages.impl.timeline.protection.mustBeProtected
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
    timelineProtectionState: TimelineProtectionState,
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
    onSwipeToReply: (TimelineItem.Event) -> Unit,
    onJoinCallClick: () -> Unit,
    eventSink: (TimelineEvents.EventFromTimelineItem) -> Unit,
    modifier: Modifier = Modifier,
    eventContentView: @Composable (TimelineItem.Event, Modifier, (ContentAvoidingLayoutData) -> Unit) -> Unit =
        { event, contentModifier, onContentLayoutChange ->
            TimelineItemEventContentView(
                content = event.content,
                hideMediaContent = timelineProtectionState.hideMediaContent(event.eventId),
                onShowClick = { timelineProtectionState.eventSink(TimelineProtectionEvent.ShowContent(event.eventId)) },
                onLinkClick = onLinkClick,
                eventSink = eventSink,
                modifier = contentModifier,
                onContentLayoutChange = onContentLayoutChange
            )
        },
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
                            timelineProtectionState = timelineProtectionState,
                            isLastOutgoingMessage = isLastOutgoingMessage,
                            isHighlighted = timelineItem.isEvent(focusedEventId),
                            onClick = if (timelineProtectionState.hideMediaContent(timelineItem.eventId) && timelineItem.mustBeProtected()) {
                                {}
                            } else {
                                { onClick(timelineItem) }
                            },
                            onLongClick = { onLongClick(timelineItem) },
                            onLinkClick = onLinkClick,
                            onUserDataClick = onUserDataClick,
                            inReplyToClick = inReplyToClick,
                            onReactionClick = onReactionClick,
                            onReactionLongClick = onReactionLongClick,
                            onMoreReactionsClick = onMoreReactionsClick,
                            onReadReceiptClick = onReadReceiptClick,
                            onSwipeToReply = { onSwipeToReply(timelineItem) },
                            eventSink = eventSink,
                            eventContentView = { contentModifier, onContentLayoutChange ->
                                eventContentView(timelineItem, contentModifier, onContentLayoutChange)
                            },
                        )
                    }
                }
            }
            is TimelineItem.GroupedEvents -> {
                TimelineItemGroupedEventsRow(
                    timelineItem = timelineItem,
                    timelineRoomInfo = timelineRoomInfo,
                    timelineProtectionState = timelineProtectionState,
                    renderReadReceipts = renderReadReceipts,
                    isLastOutgoingMessage = isLastOutgoingMessage,
                    focusedEventId = focusedEventId,
                    onClick = onClick,
                    onLongClick = onLongClick,
                    inReplyToClick = inReplyToClick,
                    onUserDataClick = onUserDataClick,
                    onLinkClick = onLinkClick,
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
