/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import io.element.android.features.messages.impl.R
import io.element.android.features.messages.impl.timeline.TimelineEvent
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.aGroupedEvents
import io.element.android.features.messages.impl.timeline.aRedactedMessagesGroupedEvents
import io.element.android.features.messages.impl.timeline.aTimelineRoomInfo
import io.element.android.features.messages.impl.timeline.components.event.TimelineItemEventContentView
import io.element.android.features.messages.impl.timeline.components.group.GroupHeaderView
import io.element.android.features.messages.impl.timeline.components.receipt.ReadReceiptViewState
import io.element.android.features.messages.impl.timeline.components.receipt.TimelineItemReadReceiptView
import io.element.android.features.messages.impl.timeline.groups.isRedactedMessagesGroup
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.features.messages.impl.timeline.protection.aTimelineProtectionState
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.Timeline

@Composable
fun TimelineItemGroupedEventsRow(
    timelineItem: TimelineItem.GroupedEvents,
    timelineMode: Timeline.Mode,
    timelineRoomInfo: TimelineRoomInfo,
    timelineProtectionState: TimelineProtectionState,
    isLastOutgoingMessage: Boolean,
    focusedEventId: EventId?,
    displayThreadSummaries: Boolean,
    callbacks: TimelineItemCallbacks,
    eventSink: (TimelineEvent.TimelineItemEvent) -> Unit,
    modifier: Modifier = Modifier,
    eventContentView: EventContentView =
        { event, timelineProtectionState, contentModifier, onContentLayoutChange, callbacks ->
            TimelineItemEventContentView(
                eventId = event.eventId,
                content = event.content,
                timelineProtectionState = timelineProtectionState,
                callbacks = TimelineEventContentCallbacks(
                    onLinkClick = callbacks.onLinkClick,
                    onLinkLongClick = callbacks.onLinkLongClick,
                ),
                modifier = contentModifier,
                onContentLayoutChange = onContentLayoutChange
            )
        },
) {
    val isExpanded = rememberSaveable { mutableStateOf(false) }

    fun onExpandGroupClick() {
        isExpanded.value = !isExpanded.value
    }

    TimelineItemGroupedEventsRowContent(
        isExpanded = isExpanded.value,
        onExpandGroupClick = ::onExpandGroupClick,
        timelineItem = timelineItem,
        timelineMode = timelineMode,
        timelineRoomInfo = timelineRoomInfo,
        timelineProtectionState = timelineProtectionState,
        focusedEventId = focusedEventId,
        isLastOutgoingMessage = isLastOutgoingMessage,
        displayThreadSummaries = displayThreadSummaries,
        callbacks = callbacks,
        eventSink = eventSink,
        modifier = modifier,
        eventContentView = eventContentView,
    )
}

@Composable
private fun TimelineItemGroupedEventsRowContent(
    isExpanded: Boolean,
    onExpandGroupClick: () -> Unit,
    timelineItem: TimelineItem.GroupedEvents,
    timelineMode: Timeline.Mode,
    timelineRoomInfo: TimelineRoomInfo,
    timelineProtectionState: TimelineProtectionState,
    focusedEventId: EventId?,
    isLastOutgoingMessage: Boolean,
    displayThreadSummaries: Boolean,
    callbacks: TimelineItemCallbacks,
    eventSink: (TimelineEvent.TimelineItemEvent) -> Unit,
    modifier: Modifier = Modifier,
    eventContentView: EventContentView = DefaultEventContentView::invoke,
) {
    Column(modifier = modifier.animateContentSize()) {
        val count = timelineItem.events.size
        // A group made entirely of redacted events is a collapsed run of deleted messages
        // (element-web style); anything else is the regular run of room state changes. For the
        // redacted case we show only the count: the SDK does not expose who performed the redaction,
        // and showing the original authors would be misleading.
        val headerText = if (timelineItem.isRedactedMessagesGroup()) {
            pluralStringResource(R.plurals.screen_room_timeline_redacted_messages, count, count)
        } else {
            pluralStringResource(R.plurals.screen_room_timeline_state_changes, count, count)
        }
        GroupHeaderView(
            text = headerText,
            isExpanded = isExpanded,
            isHighlighted = !isExpanded && timelineItem.events.any { it.isEvent(focusedEventId) },
            onClick = onExpandGroupClick,
        )
        if (isExpanded) {
            Column {
                timelineItem.events.forEach { subGroupEvent ->
                    TimelineItemRow(
                        timelineMode = timelineMode,
                        timelineItem = subGroupEvent,
                        timelineRoomInfo = timelineRoomInfo,
                        timelineProtectionState = timelineProtectionState,
                        isLastOutgoingMessage = isLastOutgoingMessage,
                        focusedEventId = focusedEventId,
                        displayThreadSummaries = displayThreadSummaries,
                        // Gallery items and swipe to reply do not apply to grouped (state) events
                        callbacks = callbacks.copy(
                            onGalleryItemClick = { _, _ -> },
                            onSwipeToReply = {},
                        ),
                        eventSink = eventSink,
                        eventContentView = eventContentView,
                    )
                }
            }
        } else if (timelineItem.aggregatedReadReceipts.isNotEmpty()) {
            TimelineItemReadReceiptView(
                state = ReadReceiptViewState(
                    sendState = null,
                    isLastOutgoingMessage = false,
                    receipts = timelineItem.aggregatedReadReceipts,
                ),
                onReadReceiptsClick = onExpandGroupClick
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemGroupedEventsRowContentExpandedPreview() = ElementPreview {
    val events = aGroupedEvents(withReadReceipts = true)
    TimelineItemGroupedEventsRowContent(
        isExpanded = true,
        onExpandGroupClick = {},
        timelineItem = events,
        timelineMode = Timeline.Mode.Live,
        timelineRoomInfo = aTimelineRoomInfo(),
        timelineProtectionState = aTimelineProtectionState(),
        focusedEventId = events.events.first().eventId,
        isLastOutgoingMessage = false,
        displayThreadSummaries = false,
        callbacks = TimelineItemCallbacks(),
        eventSink = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemGroupedEventsRowContentCollapsePreview() = ElementPreview {
    TimelineItemGroupedEventsRowContent(
        isExpanded = false,
        onExpandGroupClick = {},
        timelineItem = aGroupedEvents(withReadReceipts = true),
        timelineMode = Timeline.Mode.Live,
        timelineRoomInfo = aTimelineRoomInfo(),
        timelineProtectionState = aTimelineProtectionState(),
        focusedEventId = null,
        isLastOutgoingMessage = false,
        displayThreadSummaries = false,
        onClick = {},
        onLongClick = {},
        onLinkLongClick = {},
        inReplyToClick = {},
        onUserDataClick = {},
        onLinkClick = {},
        onReactionClick = { _, _ -> },
        onReactionLongClick = { _, _ -> },
        onMoreReactionsClick = {},
        onReadReceiptClick = {},
        eventSink = {},
    )
}

@PreviewsDayNight
@Composable
internal fun TimelineItemRedactedMessagesGroupPreview() = ElementPreview {
    // A collapsed run of deleted messages, shown as a single "N removed messages" header.
    TimelineItemGroupedEventsRowContent(
        isExpanded = false,
        onExpandGroupClick = {},
        timelineItem = aRedactedMessagesGroupedEvents(count = 11),
        timelineMode = Timeline.Mode.Live,
        timelineRoomInfo = aTimelineRoomInfo(),
        timelineProtectionState = aTimelineProtectionState(),
        focusedEventId = null,
        isLastOutgoingMessage = false,
        displayThreadSummaries = false,
        callbacks = TimelineItemCallbacks(),
        eventSink = {},
    )
}
