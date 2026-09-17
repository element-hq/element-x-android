/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.TimelineEvent
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.components.event.TimelineItemEventContentView
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.canBeSelected
import io.element.android.features.messages.impl.timeline.model.event.RtcNotificationState
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRtcNotificationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.libraries.designsystem.colors.gradientSubtleColors
import io.element.android.libraries.designsystem.modifiers.onKeyboardContextMenuAction
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toPx
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.ui.utils.a11y.isTalkbackActive
import io.element.android.wysiwyg.link.Link
import kotlin.time.DurationUnit

@Composable
internal fun TimelineItemRow(
    timelineItem: TimelineItem,
    timelineMode: Timeline.Mode,
    timelineRoomInfo: TimelineRoomInfo,
    isLastOutgoingMessage: Boolean,
    timelineProtectionState: TimelineProtectionState,
    focusedEventId: EventId?,
    displayThreadSummaries: Boolean,
    selectionData: TimelineItemSelectionData,
    onUserDataClick: (MatrixUser) -> Unit,
    onLinkClick: (Link) -> Unit,
    onLinkLongClick: (Link) -> Unit,
    onContentClick: (TimelineItem.Event) -> Unit,
    onGalleryItemClick: (TimelineItem.Event, Int) -> Unit,
    onLongClick: (TimelineItem.Event) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onReactionClick: (key: String, TimelineItem.Event) -> Unit,
    onReactionLongClick: (key: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (TimelineItem.Event) -> Unit,
    onReadReceiptClick: (TimelineItem.Event) -> Unit,
    onJoinCallClick: (isAudioCall: Boolean) -> Unit,
    onSwipeToReply: (TimelineItem.Event) -> Unit,
    eventSink: (TimelineEvent.TimelineItemEvent) -> Unit,
    modifier: Modifier = Modifier,
    eventContentView: @Composable (TimelineItem.Event, Modifier, (ContentAvoidingLayoutData) -> Unit) -> Unit =
        { event, contentModifier, onContentLayoutChange ->
            TimelineItemEventContentView(
                eventId = event.eventId,
                content = event.content,
                timelineProtectionState = timelineProtectionState,
                onContentClick = { onContentClick(event) },
                onGalleryItemClick = { index -> onGalleryItemClick(event, index) },
                onLongClick = { onLongClick(event) },
                onLinkClick = onLinkClick,
                onLinkLongClick = onLinkLongClick,
                eventSink = eventSink,
                modifier = contentModifier,
                onContentLayoutChange = onContentLayoutChange,
            )
        },
) {
    val isSelectionModeActive = selectionData.isSelectionModeActive
    val selectionProgress = selectionData.progress
    val isSelected = selectionData.isSelected
    val backgroundModifier = when {
        isSelected -> {
            Modifier.background(ElementTheme.colors.bgAccentSelected)
        }
        timelineItem.isEvent(focusedEventId) -> {
            val focusedEventOffset =
                if ((timelineItem as? TimelineItem.Event)?.showSenderInformation == true) {
                    14.dp
                } else {
                    2.dp
                }
            Modifier.focusedEvent(focusedEventOffset)
        }
        else -> {
            Modifier
        }
    }
    val selectableEvent = (timelineItem as? TimelineItem.Event)?.takeIf { it.canBeSelected() }
    // Only start-aligned message bubbles slide to make room for the selection indicator:
    //  - Outgoing (own) messages are end-aligned, opposite the indicator, so they already have room.
    //  - Centered tiles (state events, call tiles, day dividers, grouped events) must stay put.
    val isOutgoing = (timelineItem as? TimelineItem.Event)?.isMine == true
    val shouldSlideContent = timelineItem.rendersAsBubble() && !isOutgoing
    val layoutDirection = LocalLayoutDirection.current
    val contentSlide = if (shouldSlideContent) SELECTION_COLUMN_WIDTH * selectionProgress else 0.dp
    Box(
        modifier = modifier.then(backgroundModifier)
    ) {
        TimelineItemRowContent(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = contentSlide),
            selectionContentOffset = contentSlide,
            timelineItem = timelineItem,
            timelineMode = timelineMode,
            timelineRoomInfo = timelineRoomInfo,
            timelineProtectionState = timelineProtectionState,
            isLastOutgoingMessage = isLastOutgoingMessage,
            focusedEventId = focusedEventId,
            displayThreadSummaries = displayThreadSummaries,
            onUserDataClick = onUserDataClick,
            onLinkClick = onLinkClick,
            onLinkLongClick = onLinkLongClick,
            onContentClick = onContentClick,
            onGalleryItemClick = onGalleryItemClick,
            onLongClick = onLongClick,
            inReplyToClick = inReplyToClick,
            onReactionClick = onReactionClick,
            onReactionLongClick = onReactionLongClick,
            onMoreReactionsClick = onMoreReactionsClick,
            onReadReceiptClick = onReadReceiptClick,
            onJoinCallClick = onJoinCallClick,
            onSwipeToReply = onSwipeToReply,
            eventSink = eventSink,
            eventContentView = eventContentView,
        )
        if (selectableEvent != null && selectionProgress > 0f) {
            val slideSign = if (layoutDirection == LayoutDirection.Ltr) -1f else 1f
            SelectionIndicator(
                selected = isSelected,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .graphicsLayer { translationX = slideSign * SELECTION_COLUMN_WIDTH.toPx() * (1f - selectionProgress) }
                    .padding(start = 12.dp)
                    .size(24.dp),
            )
        }
        if (isSelectionModeActive && selectableEvent != null) {
            // In selection mode the whole row toggles selection.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { onContentClick(selectableEvent) }
            )
        }
    }
}

/** Width reserved for the selection indicator; the row content slides by this amount. */
private val SELECTION_COLUMN_WIDTH = 36.dp

/**
 * Whether this item is rendered as a start/end-aligned message bubble, as opposed to a centered
 * tile (state event, legacy call invite, call/RTC notification, day divider, grouped events).
 * Mirrors the dispatch in [TimelineItemRowContent]: only bubbles shift to reveal the selection
 * indicator; centered tiles stay put.
 */
private fun TimelineItem.rendersAsBubble(): Boolean = this is TimelineItem.Event &&
    content !is TimelineItemStateContent &&
    content !is TimelineItemLegacyCallInviteContent &&
    content !is TimelineItemRtcNotificationContent

@Suppress("LongMethod")
@Composable
private fun TimelineItemRowContent(
    timelineItem: TimelineItem,
    timelineMode: Timeline.Mode,
    timelineRoomInfo: TimelineRoomInfo,
    isLastOutgoingMessage: Boolean,
    timelineProtectionState: TimelineProtectionState,
    focusedEventId: EventId?,
    displayThreadSummaries: Boolean,
    onUserDataClick: (MatrixUser) -> Unit,
    onLinkClick: (Link) -> Unit,
    onLinkLongClick: (Link) -> Unit,
    onContentClick: (TimelineItem.Event) -> Unit,
    onGalleryItemClick: (TimelineItem.Event, Int) -> Unit,
    onLongClick: (TimelineItem.Event) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onReactionClick: (key: String, TimelineItem.Event) -> Unit,
    onReactionLongClick: (key: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (TimelineItem.Event) -> Unit,
    onReadReceiptClick: (TimelineItem.Event) -> Unit,
    onJoinCallClick: (isAudioCall: Boolean) -> Unit,
    onSwipeToReply: (TimelineItem.Event) -> Unit,
    eventSink: (TimelineEvent.TimelineItemEvent) -> Unit,
    eventContentView: @Composable (TimelineItem.Event, Modifier, (ContentAvoidingLayoutData) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    selectionContentOffset: Dp = 0.dp,
) {
    Box(modifier) {
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
                            isLastOutgoingMessage = isLastOutgoingMessage,
                            onClick = { onContentClick(timelineItem) },
                            onReadReceiptsClick = onReadReceiptClick,
                            onLongClick = { onLongClick(timelineItem) },
                            timelineProtectionState = timelineProtectionState,
                            eventSink = eventSink,
                        )
                    }
                    is TimelineItemRtcNotificationContent -> {
                        when (timelineItem.content.state) {
                            is RtcNotificationState.Active -> ActiveCallTimelineItemView(
                                timelineRoomInfo = timelineRoomInfo,
                                event = timelineItem,
                                state = timelineItem.content.state,
                                isLastOutgoingMessage = isLastOutgoingMessage,
                                onLongClick = onLongClick,
                                onReadReceiptsClick = onReadReceiptClick,
                                onJoinCallClick = onJoinCallClick,
                            )
                            is RtcNotificationState.Started, is RtcNotificationState.Declined ->
                                TimelineItemCallNotifyView(
                                    timelineRoomInfo = timelineRoomInfo,
                                    event = timelineItem,
                                    content = timelineItem.content,
                                    state = timelineItem.content.state,
                                    isLastOutgoingMessage = isLastOutgoingMessage,
                                    onLongClick = onLongClick,
                                    onReadReceiptsClick = onReadReceiptClick,
                                )
                        }
                    }
                    else -> {
                        val a11yVoiceMessage = stringResource(CommonStrings.a11y_voice_message)
                        TimelineItemEventRow(
                            modifier = Modifier
                                .semantics(mergeDescendants = true) {
                                    contentDescription = if (timelineItem.content is TimelineItemVoiceContent) {
                                        val voiceMessageText = String.format(a11yVoiceMessage, timelineItem.content.duration.toString(DurationUnit.MINUTES))
                                        "${timelineItem.safeSenderName}, $voiceMessageText"
                                    } else {
                                        timelineItem.safeSenderName
                                    }
                                    // For Polls, allow the answers to be traversed by Talkback
                                    isTraversalGroup = timelineItem.content is TimelineItemPollContent ||
                                        timelineItem.failedToSend ||
                                        timelineItem.messageShield != null
                                    // TODO Also set to true when the event has link(s)
                                }
                                // Custom clickable that applies over the whole item for accessibility
                                .then(
                                    if (isTalkbackActive()) {
                                        Modifier
                                            .combinedClickable(
                                                onClick = { onContentClick(timelineItem) },
                                                onLongClick = { onLongClick(timelineItem) },
                                                onLongClickLabel = stringResource(CommonStrings.action_open_context_menu),
                                            )
                                            .onKeyboardContextMenuAction { onLongClick(timelineItem) }
                                    } else {
                                        Modifier
                                    }
                                ),
                            event = timelineItem,
                            timelineMode = timelineMode,
                            timelineRoomInfo = timelineRoomInfo,
                            timelineProtectionState = timelineProtectionState,
                            isLastOutgoingMessage = isLastOutgoingMessage,
                            displayThreadSummaries = displayThreadSummaries,
                            onEventClick = { onContentClick(timelineItem) },
                            onLongClick = { onLongClick(timelineItem) },
                            onLinkClick = onLinkClick,
                            onLinkLongClick = onLinkLongClick,
                            onUserDataClick = onUserDataClick,
                            inReplyToClick = inReplyToClick,
                            onReactionClick = onReactionClick,
                            onReactionLongClick = onReactionLongClick,
                            onMoreReactionsClick = onMoreReactionsClick,
                            onReadReceiptClick = onReadReceiptClick,
                            onSwipeToReply = { onSwipeToReply(timelineItem) },
                            onGalleryItemClick = { index -> onGalleryItemClick(timelineItem, index) },
                            eventSink = eventSink,
                            selectionContentOffset = selectionContentOffset,
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
                    timelineMode = timelineMode,
                    timelineRoomInfo = timelineRoomInfo,
                    timelineProtectionState = timelineProtectionState,
                    isLastOutgoingMessage = isLastOutgoingMessage,
                    focusedEventId = focusedEventId,
                    displayThreadSummaries = displayThreadSummaries,
                    onClick = onContentClick,
                    onLongClick = onLongClick,
                    inReplyToClick = inReplyToClick,
                    onUserDataClick = onUserDataClick,
                    onLinkClick = onLinkClick,
                    onLinkLongClick = onLinkLongClick,
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
    focusedEventOffset: Dp,
): Modifier {
    val highlightedLineColor = ElementTheme.colors.borderAccentSubtle
    val gradientColors = gradientSubtleColors()
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

@PreviewsDayNight
@Composable
internal fun FocusedEventPreview() = ElementPreview {
    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(160.dp)
            .focusedEvent(0.dp),
    )
}
