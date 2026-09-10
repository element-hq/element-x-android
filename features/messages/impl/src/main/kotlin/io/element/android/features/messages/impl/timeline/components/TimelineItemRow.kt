/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.TimelineEvent
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.components.event.TimelineItemEventContentView
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.TimelineItem
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
import io.element.android.libraries.matrix.api.timeline.item.event.TimelineItemEventOrigin
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
    val content = @Composable {
        Box(modifier = modifier.then(backgroundModifier)) {
            RowContent(
                timelineItem = timelineItem,
                timelineMode = timelineMode,
                timelineRoomInfo = timelineRoomInfo,
                isLastOutgoingMessage = isLastOutgoingMessage,
                timelineProtectionState = timelineProtectionState,
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
        }
    }

    WithEnterAnimation(
        timelineItem = timelineItem,
        content = content,
    )
}

@Composable
private fun WithEnterAnimation(
    timelineItem: TimelineItem,
    content: @Composable () -> Unit,
) {
    val movableContent = remember { movableContentOf(content) }
    // Only events with SYNC or LOCAL origin should have an enter animation, to avoid animating events that are already in the timeline
    // when the user opens the room.
    val needsEnterAnimation = remember {
        timelineItem is TimelineItem.Event &&
        (timelineItem.origin == TimelineItemEventOrigin.SYNC || timelineItem.origin == TimelineItemEventOrigin.LOCAL)
    }
    if (needsEnterAnimation && !LocalInspectionMode.current) {
        // Animate the  item in the first time it is displayed, e.g. when a new message arrives.
        // We also remember the animation state so it doesn't re-animate when the timeline recomposes or we come back to the screen it's in.
        var isDisplayed by rememberSaveable { mutableStateOf(false) }
        val visibleState = remember { MutableTransitionState(initialState = isDisplayed).apply { targetState = true } }
        LaunchedEffect(visibleState.currentState) {
            isDisplayed = visibleState.currentState
        }
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn() + slideInVertically { fullHeight -> fullHeight / 2 },
        ) {
            movableContent()
        }
    } else {
        movableContent()
    }
}

@Composable
private fun RowContent(
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
) {
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
