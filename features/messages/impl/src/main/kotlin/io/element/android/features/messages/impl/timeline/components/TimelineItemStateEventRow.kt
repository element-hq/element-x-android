/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.components.event.TimelineItemEventContentView
import io.element.android.features.messages.impl.timeline.components.receipt.ReadReceiptViewState
import io.element.android.features.messages.impl.timeline.components.receipt.TimelineItemReadReceiptView
import io.element.android.features.messages.impl.timeline.components.receipt.aReadReceiptData
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.TimelineItemReadReceipts
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemStateEventContent
import io.element.android.features.messages.impl.timeline.util.defaultTimelineContentPadding
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import kotlinx.collections.immutable.toPersistentList

@Composable
fun TimelineItemStateEventRow(
    event: TimelineItem.Event,
    renderReadReceipts: Boolean,
    isLastOutgoingMessage: Boolean,
    isHighlighted: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onReadReceiptsClick: (event: TimelineItem.Event) -> Unit,
    eventSink: (TimelineEvents.EventFromTimelineItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 2.dp)
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            MessageStateEventContainer(
                isHighlighted = isHighlighted,
                interactionSource = interactionSource,
                onClick = onClick,
                onLongClick = onLongClick,
                modifier = Modifier
                    .zIndex(-1f)
                    .widthIn(max = 320.dp)
            ) {
                TimelineItemEventContentView(
                    content = event.content,
                    onLinkClick = {},
                    hideMediaContent = false,
                    onShowClick = {},
                    eventSink = eventSink,
                    modifier = Modifier.defaultTimelineContentPadding()
                )
            }
        }
        TimelineItemReadReceiptView(
            state = ReadReceiptViewState(
                sendState = event.localSendState,
                isLastOutgoingMessage = isLastOutgoingMessage,
                receipts = event.readReceiptState.receipts,
            ),
            renderReadReceipts = renderReadReceipts,
            onReadReceiptsClick = { onReadReceiptsClick(event) },
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemStateEventRowPreview() = ElementPreview {
    TimelineItemStateEventRow(
        event = aTimelineItemEvent(
            isMine = false,
            content = aTimelineItemStateEventContent(),
            groupPosition = TimelineItemGroupPosition.None,
            readReceiptState = TimelineItemReadReceipts(
                receipts = listOf(aReadReceiptData(0)).toPersistentList(),
            )
        ),
        renderReadReceipts = true,
        isLastOutgoingMessage = false,
        isHighlighted = false,
        onClick = {},
        onLongClick = {},
        onReadReceiptsClick = {},
        eventSink = {}
    )
}
