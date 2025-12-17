/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import org.matrix.rustcomponents.sdk.MessageContent
import org.matrix.rustcomponents.sdk.MessageType
import org.matrix.rustcomponents.sdk.MsgLikeContent
import org.matrix.rustcomponents.sdk.MsgLikeKind
import org.matrix.rustcomponents.sdk.TextMessageContent
import org.matrix.rustcomponents.sdk.TimelineItemContent

fun aRustTimelineItemMessageContent(body: String = "Hello") = TimelineItemContent.MsgLike(
    content = MsgLikeContent(
        kind = MsgLikeKind.Message(
            content = MessageContent(
                msgType = MessageType.Text(content = TextMessageContent(body = body, formatted = null)),
                body = body,
                isEdited = false,
                mentions = null,
            )
        ),
        reactions = emptyList(),
        threadRoot = null,
        inReplyTo = null,
        threadSummary = null,
    ),
)
