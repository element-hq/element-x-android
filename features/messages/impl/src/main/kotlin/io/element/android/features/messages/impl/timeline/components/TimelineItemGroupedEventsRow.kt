/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.TimelineRoomInfo
import io.element.android.features.messages.impl.timeline.aGroupedEvents
import io.element.android.features.messages.impl.timeline.aTimelineRoomInfo
import io.element.android.features.messages.impl.timeline.components.event.TimelineItemEventContentView
import io.element.android.features.messages.impl.timeline.components.group.GroupHeaderView
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.components.receipt.ReadReceiptViewState
import io.element.android.features.messages.impl.timeline.components.receipt.TimelineItemReadReceiptView
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionEvent
import io.element.android.features.messages.impl.timeline.protection.TimelineProtectionState
import io.element.android.features.messages.impl.timeline.protection.aTimelineProtectionState
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId

@Composable
fun TimelineItemGroupedEventsRow(
    timelineItem: TimelineItem.GroupedEvents,
    timelineRoomInfo: TimelineRoomInfo,
    timelineProtectionState: TimelineProtectionState,
    renderReadReceipts: Boolean,
    isLastOutgoingMessage: Boolean,
    focusedEventId: EventId?,
    onClick: (TimelineItem.Event) -> Unit,
    onLongClick: (TimelineItem.Event) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onUserDataClick: (UserId) -> Unit,
    onLinkClick: (String) -> Unit,
    onReactionClick: (key: String, TimelineItem.Event) -> Unit,
    onReactionLongClick: (key: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (TimelineItem.Event) -> Unit,
    onReadReceiptClick: (TimelineItem.Event) -> Unit,
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
    val isExpanded = rememberSaveable(key = timelineItem.identifier().value) { mutableStateOf(false) }

    fun onExpandGroupClick() {
        isExpanded.value = !isExpanded.value
    }

    TimelineItemGroupedEventsRowContent(
        isExpanded = isExpanded.value,
        onExpandGroupClick = ::onExpandGroupClick,
        timelineItem = timelineItem,
        timelineRoomInfo = timelineRoomInfo,
        timelineProtectionState = timelineProtectionState,
        focusedEventId = focusedEventId,
        renderReadReceipts = renderReadReceipts,
        isLastOutgoingMessage = isLastOutgoingMessage,
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
        modifier = modifier,
        eventContentView = eventContentView,
    )
}

@Composable
private fun TimelineItemGroupedEventsRowContent(
    isExpanded: Boolean,
    onExpandGroupClick: () -> Unit,
    timelineItem: TimelineItem.GroupedEvents,
    timelineRoomInfo: TimelineRoomInfo,
    timelineProtectionState: TimelineProtectionState,
    focusedEventId: EventId?,
    renderReadReceipts: Boolean,
    isLastOutgoingMessage: Boolean,
    onClick: (TimelineItem.Event) -> Unit,
    onLongClick: (TimelineItem.Event) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onUserDataClick: (UserId) -> Unit,
    onLinkClick: (String) -> Unit,
    onReactionClick: (key: String, TimelineItem.Event) -> Unit,
    onReactionLongClick: (key: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (TimelineItem.Event) -> Unit,
    onReadReceiptClick: (TimelineItem.Event) -> Unit,
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
    Column(modifier = modifier.animateContentSize()) {
        GroupHeaderView(
            text = pluralStringResource(
                id = R.plurals.screen_room_timeline_state_changes,
                count = timelineItem.events.size,
                timelineItem.events.size
            ),
            isExpanded = isExpanded,
            isHighlighted = !isExpanded && timelineItem.events.any { it.isEvent(focusedEventId) },
            onClick = onExpandGroupClick,
        )
        if (isExpanded) {
            Column {
                timelineItem.events.forEach { subGroupEvent ->
                    TimelineItemRow(
                        timelineItem = subGroupEvent,
                        timelineRoomInfo = timelineRoomInfo,
                        timelineProtectionState = timelineProtectionState,
                        renderReadReceipts = renderReadReceipts,
                        isLastOutgoingMessage = isLastOutgoingMessage,
                        focusedEventId = focusedEventId,
                        onUserDataClick = onUserDataClick,
                        onLinkClick = onLinkClick,
                        onClick = onClick,
                        onLongClick = onLongClick,
                        inReplyToClick = inReplyToClick,
                        onReactionClick = onReactionClick,
                        onReactionLongClick = onReactionLongClick,
                        onMoreReactionsClick = onMoreReactionsClick,
                        onReadReceiptClick = onReadReceiptClick,
                        onSwipeToReply = {},
                        onJoinCallClick = {},
                        eventSink = eventSink,
                        eventContentView = eventContentView,
                    )
                }
            }
        } else if (renderReadReceipts) {
            TimelineItemReadReceiptView(
                state = ReadReceiptViewState(
                    sendState = null,
                    isLastOutgoingMessage = false,
                    receipts = timelineItem.aggregatedReadReceipts,
                ),
                renderReadReceipts = true,
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
        timelineRoomInfo = aTimelineRoomInfo(),
        timelineProtectionState = aTimelineProtectionState(),
        focusedEventId = events.events.first().eventId,
        renderReadReceipts = true,
        isLastOutgoingMessage = false,
        onClick = {},
        onLongClick = {},
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
internal fun TimelineItemGroupedEventsRowContentCollapsePreview() = ElementPreview {
    TimelineItemGroupedEventsRowContent(
        isExpanded = false,
        onExpandGroupClick = {},
        timelineItem = aGroupedEvents(withReadReceipts = true),
        timelineRoomInfo = aTimelineRoomInfo(),
        timelineProtectionState = aTimelineProtectionState(),
        focusedEventId = null,
        renderReadReceipts = true,
        isLastOutgoingMessage = false,
        onClick = {},
        onLongClick = {},
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
