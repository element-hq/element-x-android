/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.timeline.TimelineEvent
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRtcNotificationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.libraries.designsystem.modifiers.onKeyboardContextMenuAction
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.ui.utils.a11y.isTalkbackActive
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
    callbacks: TimelineItemCallbacks,
    eventSink: (TimelineEvent.TimelineItemEvent) -> Unit,
    modifier: Modifier = Modifier,
    eventContentView: EventContentView = DefaultEventContentView::invoke,
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
                            isLastOutgoingMessage = isLastOutgoingMessage,
                            onClick = { callbacks.onContentClick(timelineItem) },
                            onReadReceiptsClick = callbacks.onReadReceiptClick,
                            onLongClick = { callbacks.onLongClick(timelineItem) },
                            timelineProtectionState = timelineProtectionState,
                        )
                    }
                    is TimelineItemRtcNotificationContent -> {
                        TimelineItemCallNotifyView(
                            timelineRoomInfo = timelineRoomInfo,
                            event = timelineItem,
                            content = timelineItem.content,
                            isLastOutgoingMessage = isLastOutgoingMessage,
                            onLongClick = { callbacks.onLongClick(timelineItem) },
                            onReadReceiptsClick = callbacks.onReadReceiptClick,
                        )
                    }
                    else -> {
                        TimelineItemEventRow(
                            modifier = Modifier.timelineItemEventAccessibility(
                                event = timelineItem,
                                onClick = { callbacks.onContentClick(timelineItem) },
                                onLongClick = { callbacks.onLongClick(timelineItem) }
                            ),
                            event = timelineItem,
                            timelineMode = timelineMode,
                            timelineRoomInfo = timelineRoomInfo,
                            timelineProtectionState = timelineProtectionState,
                            isLastOutgoingMessage = isLastOutgoingMessage,
                            displayThreadSummaries = displayThreadSummaries,
                            callbacks = callbacks,
                            eventSink = eventSink,
                            eventContentView = eventContentView,
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
                    callbacks = callbacks,
                    eventSink = eventSink,
                )
            }
        }
    }
}

/**
 * Merges the descendants semantics and sets the proper content description and traversal behaviour
 * for the event, and makes the whole item clickable when Talkback is active.
 */
@Suppress("ModifierComposable")
@Composable
private fun Modifier.timelineItemEventAccessibility(
    event: TimelineItem.Event,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
): Modifier {
    val a11yVoiceMessage = stringResource(CommonStrings.a11y_voice_message)
    return this
        .semantics(mergeDescendants = true) {
            contentDescription = if (event.content is TimelineItemVoiceContent) {
                val voiceMessageText = String.format(a11yVoiceMessage, event.content.duration.toString(DurationUnit.MINUTES))
                "${event.safeSenderName}, $voiceMessageText"
            } else {
                event.safeSenderName
            }
            // For Polls, allow the answers to be traversed by Talkback
            isTraversalGroup = event.content is TimelineItemPollContent ||
                event.failedToSend ||
                event.messageShield != null
            // TODO Also set to true when the event has link(s)
        }
        // Custom clickable that applies over the whole item for accessibility
        .then(
            if (isTalkbackActive()) {
                Modifier
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick,
                        onLongClickLabel = stringResource(CommonStrings.action_open_context_menu),
                    )
                    .onKeyboardContextMenuAction { onLongClick() }
            } else {
                Modifier
            }
        )
}
