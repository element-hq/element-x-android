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
import io.element.android.features.messages.impl.timeline.components.group.GroupHeaderView
import io.element.android.features.messages.impl.timeline.components.receipt.ReadReceiptViewState
import io.element.android.features.messages.impl.timeline.components.receipt.TimelineItemReadReceiptView
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId

@Composable
fun TimelineItemGroupedEventsRow(
    timelineItem: TimelineItem.GroupedEvents,
    timelineRoomInfo: TimelineRoomInfo,
    renderReadReceipts: Boolean,
    isLastOutgoingMessage: Boolean,
    focusedEventId: EventId?,
    onClick: (TimelineItem.Event) -> Unit,
    onLongClick: (TimelineItem.Event) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onUserDataClick: (UserId) -> Unit,
    onLinkClick: (String) -> Unit,
    onTimestampClick: (TimelineItem.Event) -> Unit,
    onReactionClick: (key: String, TimelineItem.Event) -> Unit,
    onReactionLongClick: (key: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (TimelineItem.Event) -> Unit,
    onReadReceiptClick: (TimelineItem.Event) -> Unit,
    eventSink: (TimelineEvents.EventFromTimelineItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpanded = rememberSaveable(key = timelineItem.identifier()) { mutableStateOf(false) }

    fun onExpandGroupClick() {
        isExpanded.value = !isExpanded.value
    }

    TimelineItemGroupedEventsRowContent(
        isExpanded = isExpanded.value,
        onExpandGroupClick = ::onExpandGroupClick,
        timelineItem = timelineItem,
        timelineRoomInfo = timelineRoomInfo,
        focusedEventId = focusedEventId,
        renderReadReceipts = renderReadReceipts,
        isLastOutgoingMessage = isLastOutgoingMessage,
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
        modifier = modifier,
    )
}

@Composable
private fun TimelineItemGroupedEventsRowContent(
    isExpanded: Boolean,
    onExpandGroupClick: () -> Unit,
    timelineItem: TimelineItem.GroupedEvents,
    timelineRoomInfo: TimelineRoomInfo,
    focusedEventId: EventId?,
    renderReadReceipts: Boolean,
    isLastOutgoingMessage: Boolean,
    onClick: (TimelineItem.Event) -> Unit,
    onLongClick: (TimelineItem.Event) -> Unit,
    inReplyToClick: (EventId) -> Unit,
    onUserDataClick: (UserId) -> Unit,
    onLinkClick: (String) -> Unit,
    onTimestampClick: (TimelineItem.Event) -> Unit,
    onReactionClick: (key: String, TimelineItem.Event) -> Unit,
    onReactionLongClick: (key: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (TimelineItem.Event) -> Unit,
    onReadReceiptClick: (TimelineItem.Event) -> Unit,
    eventSink: (TimelineEvents.EventFromTimelineItem) -> Unit,
    modifier: Modifier = Modifier,
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
                        onSwipeToReply = {},
                        onJoinCallClick = {},
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
        focusedEventId = events.events.first().eventId,
        renderReadReceipts = true,
        isLastOutgoingMessage = false,
        onClick = {},
        onLongClick = {},
        inReplyToClick = {},
        onUserDataClick = {},
        onLinkClick = {},
        onTimestampClick = {},
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
        focusedEventId = null,
        renderReadReceipts = true,
        isLastOutgoingMessage = false,
        onClick = {},
        onLongClick = {},
        inReplyToClick = {},
        onUserDataClick = {},
        onLinkClick = {},
        onTimestampClick = {},
        onReactionClick = { _, _ -> },
        onReactionLongClick = { _, _ -> },
        onMoreReactionsClick = {},
        onReadReceiptClick = {},
        eventSink = {},
    )
}
