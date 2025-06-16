/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.message

import io.element.android.libraries.matrix.api.core.EventId

data class ReplyParameters(
    val inReplyToEventId: EventId,
    val enforceThreadReply: Boolean,
    val replyWithinThread: Boolean,
)

fun replyInThread(eventId: EventId, explicitReply: Boolean = false) = ReplyParameters(
    inReplyToEventId = eventId,
    enforceThreadReply = true,
    replyWithinThread = explicitReply,
)
