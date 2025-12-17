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
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.ui.utils.time.isTalkbackActive
import io.element.android.wysiwyg.link.Link

@Composable
fun TimelineItemGroupedEventsRow(
    timelineItem: TimelineItem.GroupedEvents,
    timelineMode: Timeline.Mode,
    timelineRoomInfo: TimelineRoomInfo,
    timelineProtectionState: TimelineProtectionState,
    renderReadReceipts: Boolean,
    isLastOutgoingMessage: Boolean,
    focusedEventId: EventId?,
    displayThreadSummaries: Boolean,
    onClick: (TimelineItem.Event) -> Unit,
    onLongClick: (TimelineItem.Event) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onUserDataClick: (MatrixUser) -> Unit,
    onLinkClick: (Link) -> Unit,
    onLinkLongClick: (Link) -> Unit,
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
                onShowContentClick = { timelineProtectionState.eventSink(TimelineProtectionEvent.ShowContent(event.eventId)) },
                onLinkClick = onLinkClick,
                onLinkLongClick = onLinkLongClick,
                eventSink = eventSink,
                modifier = contentModifier,
                onContentClick = null,
                onLongClick = null,
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
        renderReadReceipts = renderReadReceipts,
        isLastOutgoingMessage = isLastOutgoingMessage,
        displayThreadSummaries = displayThreadSummaries,
        onClick = onClick,
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
    renderReadReceipts: Boolean,
    isLastOutgoingMessage: Boolean,
    displayThreadSummaries: Boolean,
    onClick: (TimelineItem.Event) -> Unit,
    onLongClick: (TimelineItem.Event) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onUserDataClick: (MatrixUser) -> Unit,
    onLinkClick: (Link) -> Unit,
    onLinkLongClick: (Link) -> Unit,
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
                onShowContentClick = { timelineProtectionState.eventSink(TimelineProtectionEvent.ShowContent(event.eventId)) },
                onLinkClick = onLinkClick,
                onLinkLongClick = onLinkLongClick,
                eventSink = eventSink,
                modifier = contentModifier,
                onContentClick = null,
                onLongClick = null,
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
                timelineItem.events.let {
                    if (isTalkbackActive()) {
                        it.reversed()
                    } else {
                        it
                    }
                }.forEach { subGroupEvent ->
                    TimelineItemRow(
                        timelineMode = timelineMode,
                        timelineItem = subGroupEvent,
                        timelineRoomInfo = timelineRoomInfo,
                        timelineProtectionState = timelineProtectionState,
                        renderReadReceipts = renderReadReceipts,
                        isLastOutgoingMessage = isLastOutgoingMessage,
                        focusedEventId = focusedEventId,
                        displayThreadSummaries = displayThreadSummaries,
                        onUserDataClick = onUserDataClick,
                        onLinkClick = onLinkClick,
                        onLinkLongClick = onLinkLongClick,
                        onContentClick = onClick,
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
        timelineMode = Timeline.Mode.Live,
        timelineRoomInfo = aTimelineRoomInfo(),
        timelineProtectionState = aTimelineProtectionState(),
        focusedEventId = events.events.first().eventId,
        renderReadReceipts = true,
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
internal fun TimelineItemGroupedEventsRowContentCollapsePreview() = ElementPreview {
    TimelineItemGroupedEventsRowContent(
        isExpanded = false,
        onExpandGroupClick = {},
        timelineItem = aGroupedEvents(withReadReceipts = true),
        timelineMode = Timeline.Mode.Live,
        timelineRoomInfo = aTimelineRoomInfo(),
        timelineProtectionState = aTimelineProtectionState(),
        focusedEventId = null,
        renderReadReceipts = true,
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
