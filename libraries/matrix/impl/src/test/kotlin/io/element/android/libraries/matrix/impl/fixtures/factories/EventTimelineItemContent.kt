/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import org.matrix.rustcomponents.sdk.MessageContent
import org.matrix.rustcomponents.sdk.MessageType
import org.matrix.rustcomponents.sdk.TextMessageContent
import org.matrix.rustcomponents.sdk.TimelineItemContent

fun aRustTimelineItemMessageContent(body: String = "Hello") = TimelineItemContent.Message(
    content = MessageContent(
        msgType = MessageType.Text(content = TextMessageContent(body = body, formatted = null)),
        body = body,
        inReplyTo = null,
        threadRoot = null,
        isEdited = false,
        mentions = null,
    )
)
