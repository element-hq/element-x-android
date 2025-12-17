/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import io.element.android.libraries.matrix.test.A_MESSAGE
import org.matrix.rustcomponents.sdk.FormattedBody
import org.matrix.rustcomponents.sdk.MessageLikeEventContent
import org.matrix.rustcomponents.sdk.MessageType
import org.matrix.rustcomponents.sdk.TextMessageContent
import org.matrix.rustcomponents.sdk.TimelineEventType

fun aRustTimelineEventTypeMessageLike(
    content: MessageLikeEventContent = aRustMessageLikeEventContentRoomMessage(),
): TimelineEventType.MessageLike {
    return TimelineEventType.MessageLike(
        content = content,
    )
}

fun aRustMessageLikeEventContentRoomMessage(
    messageType: MessageType = aRustMessageTypeText(),
    inReplyToEventId: String? = null,
) = MessageLikeEventContent.RoomMessage(
    messageType = messageType,
    inReplyToEventId = inReplyToEventId,
)

fun aRustMessageTypeText(
    content: TextMessageContent = aRustTextMessageContent(),
) = MessageType.Text(
    content = content,
)

fun aRustTextMessageContent(
    body: String = A_MESSAGE,
    formatted: FormattedBody? = null,
) = TextMessageContent(
    body = body,
    formatted = formatted,
)
