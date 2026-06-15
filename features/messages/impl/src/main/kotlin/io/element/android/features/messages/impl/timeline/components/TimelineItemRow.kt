/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.selection.SelectionIndicator
import io.element.android.features.messages.impl.timeline.TimelineEvent
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.components.event.TimelineItemEventContentView
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRtcNotificationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.timeline.model.event.isBulkSelectable
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionEvent
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
import kotlinx.collections.immutable.ImmutableSet
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
    onLongClick: (TimelineItem.Event) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onReactionClick: (key: String, TimelineItem.Event) -> Unit,
    onReactionLongClick: (key: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (TimelineItem.Event) -> Unit,
    onReadReceiptClick: (TimelineItem.Event) -> Unit,
    onSwipeToReply: (TimelineItem.Event) -> Unit,
    eventSink: (TimelineEvent.TimelineItemEvent) -> Unit,
    modifier: Modifier = Modifier,
    selectedEventIds: ImmutableSet<EventId>? = null,
    eventContentView: @Composable (TimelineItem.Event, Modifier, (ContentAvoidingLayoutData) -> Unit) -> Unit =
        { event, contentModifier, onContentLayoutChange ->
            TimelineItemEventContentView(
                content = event.content,
                hideMediaContent = timelineProtectionState.hideMediaContent(event.eventId, event.isMine),
                onShowContentClick = { timelineProtectionState.eventSink(TimelineProtectionEvent.ShowContent(event.eventId)) },
                onContentClick = { onContentClick(event) },
                onLongClick = { onLongClick(event) },
                onLinkClick = onLinkClick,
                onLinkLongClick = onLinkLongClick,
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
    val ids = selectedEventIds
    val selectableEvent = (timelineItem as? TimelineItem.Event)?.takeIf {
        it.eventId != null && ids != null && it.content.isBulkSelectable()
    }
    val isSelected = selectableEvent != null && ids != null && selectableEvent.eventId in ids
    val selectionTint = if (isSelected) {
        Modifier.background(ElementTheme.colors.bgAccentSelected)
    } else {
        Modifier
    }
    val selectionClick = if (selectableEvent != null) {
        // While selecting, the whole row is the toggle surface: Role.Checkbox makes TalkBack
        // announce the selected state, and the content semantics below merge up into this node
        // (mergeDescendants is disabled there in selection mode) so the row is read as one item.
        Modifier.toggleable(
            value = isSelected,
            role = Role.Checkbox,
            onValueChange = { onContentClick(selectableEvent) },
        )
    } else {
        Modifier
    }
    Box(modifier = modifier.then(backgroundModifier).then(selectionTint).then(selectionClick)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            AnimatedVisibility(
                visible = selectableEvent != null,
                enter = fadeIn(tween(150)) + expandHorizontally(tween(180)),
                exit = fadeOut(tween(120)) + shrinkHorizontally(tween(150)),
            ) {
                SelectionIndicator(checked = isSelected)
            }
            Box(modifier = Modifier.weight(1f)) {
                TimelineItemRowContent(
                    timelineItem = timelineItem,
                    timelineMode = timelineMode,
                    timelineRoomInfo = timelineRoomInfo,
                    isLastOutgoingMessage = isLastOutgoingMessage,
                    selectionMode = selectableEvent != null,
                    timelineProtectionState = timelineProtectionState,
                    focusedEventId = focusedEventId,
                    displayThreadSummaries = displayThreadSummaries,
                    onUserDataClick = onUserDataClick,
                    onLinkClick = onLinkClick,
                    onLinkLongClick = onLinkLongClick,
                    onContentClick = onContentClick,
                    onLongClick = onLongClick,
                    inReplyToClick = inReplyToClick,
                    onReactionClick = onReactionClick,
                    onReactionLongClick = onReactionLongClick,
                    onMoreReactionsClick = onMoreReactionsClick,
                    onReadReceiptClick = onReadReceiptClick,
                    onSwipeToReply = onSwipeToReply,
                    eventSink = eventSink,
                    eventContentView = eventContentView,
                )
            }
        }
    }
}

@Composable
private fun TimelineItemRowContent(
    timelineItem: TimelineItem,
    timelineMode: Timeline.Mode,
    timelineRoomInfo: TimelineRoomInfo,
    isLastOutgoingMessage: Boolean,
    selectionMode: Boolean,
    timelineProtectionState: TimelineProtectionState,
    focusedEventId: EventId?,
    displayThreadSummaries: Boolean,
    onUserDataClick: (MatrixUser) -> Unit,
    onLinkClick: (Link) -> Unit,
    onLinkLongClick: (Link) -> Unit,
    onContentClick: (TimelineItem.Event) -> Unit,
    onLongClick: (TimelineItem.Event) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onReactionClick: (key: String, TimelineItem.Event) -> Unit,
    onReactionLongClick: (key: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (TimelineItem.Event) -> Unit,
    onReadReceiptClick: (TimelineItem.Event) -> Unit,
    onSwipeToReply: (TimelineItem.Event) -> Unit,
    eventSink: (TimelineEvent.TimelineItemEvent) -> Unit,
    eventContentView: @Composable (TimelineItem.Event, Modifier, (ContentAvoidingLayoutData) -> Unit) -> Unit,
) {
    Box {
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
                            eventSink = eventSink,
                        )
                    }
                    is TimelineItemRtcNotificationContent -> {
                        TimelineItemCallNotifyView(
                            timelineRoomInfo = timelineRoomInfo,
                            event = timelineItem,
                            content = timelineItem.content,
                            isLastOutgoingMessage = isLastOutgoingMessage,
                            onLongClick = onLongClick,
                            onReadReceiptsClick = onReadReceiptClick,
                        )
                    }
                    else -> {
                        val a11yVoiceMessage = stringResource(CommonStrings.a11y_voice_message)
                        TimelineItemEventRow(
                            modifier = Modifier
                                // In selection mode the row's toggleable owns a single Checkbox node;
                                // letting this flow up (mergeDescendants = false) folds the message into
                                // it instead of exposing a second TalkBack node for the same row.
                                .semantics(mergeDescendants = !selectionMode) {
                                    contentDescription = if (timelineItem.content is TimelineItemVoiceContent) {
                                        val voiceMessageText = String.format(a11yVoiceMessage, timelineItem.content.duration.toString(DurationUnit.MINUTES))
                                        "${timelineItem.safeSenderName}, $voiceMessageText"
                                    } else {
                                        timelineItem.safeSenderName
                                    }
                                    // For Polls, allow the answers to be traversed by Talkback (but not
                                    // while selecting, where the row is a single toggle).
                                    isTraversalGroup = !selectionMode && (
                                        timelineItem.content is TimelineItemPollContent ||
                                            timelineItem.failedToSend ||
                                            timelineItem.messageShield != null
                                        )
                                    // TODO Also set to true when the event has link(s)
                                }
                                // Custom clickable that applies over the whole item for accessibility.
                                // Suppressed while selecting: the row's toggleable is the single action.
                                .then(
                                    if (isTalkbackActive() && !selectionMode) {
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
