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

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.model.InReplyToDetails
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType

@PreviewsDayNight
@Composable
internal fun TimelineItemEventRowWithReplyPreview() = ElementPreview {
    Column {
        sequenceOf(false, true).forEach {
            val replyContent = if (it) {
                // Short
                "Message which are being replied."
            } else {
                // Long, to test 2 lines and ellipsis)
                "Message which are being replied, and which was long enough to be displayed on two lines (only!)."
            }
            TimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = it,
                    content = aTimelineItemTextContent().copy(
                        body = "A long text which will be displayed on several lines and" +
                            " hopefully can be manually adjusted to test different behaviors."
                    ),
                    inReplyTo = aInReplyToDetails(replyContent),
                    groupPosition = TimelineItemGroupPosition.First,
                ),
                showReadReceipts = false,
                isLastOutgoingMessage = false,
                isHighlighted = false,
                canReply = true,
                onClick = {},
                onLongClick = {},
                onUserDataClick = {},
                inReplyToClick = {},
                onReactionClick = { _, _ -> },
                onReactionLongClick = { _, _ -> },
                onMoreReactionsClick = {},
                onReadReceiptClick = {},
                onTimestampClicked = {},
                onSwipeToReply = {},
                eventSink = {},
            )
            TimelineItemEventRow(
                event = aTimelineItemEvent(
                    isMine = it,
                    content = aTimelineItemImageContent().copy(
                        aspectRatio = 5f
                    ),
                    inReplyTo = aInReplyToDetails(replyContent),
                    isThreaded = true,
                    groupPosition = TimelineItemGroupPosition.Last,
                ),
                showReadReceipts = false,
                isLastOutgoingMessage = false,
                isHighlighted = false,
                canReply = true,
                onClick = {},
                onLongClick = {},
                onUserDataClick = {},
                inReplyToClick = {},
                onReactionClick = { _, _ -> },
                onReactionLongClick = { _, _ -> },
                onMoreReactionsClick = {},
                onReadReceiptClick = {},
                onTimestampClicked = {},
                onSwipeToReply = {},
                eventSink = {},
            )
        }
    }
}

private fun aInReplyToDetails(
    replyContent: String,
): InReplyToDetails {
    return InReplyToDetails(
        eventId = EventId("\$event"),
        eventContent = MessageContent(replyContent, null, false, false, TextMessageType(replyContent, null)),
        senderId = UserId("@Sender:domain"),
        senderDisplayName = "Sender",
        senderAvatarUrl = null,
        textContent = replyContent,
    )
}
