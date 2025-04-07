/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.message

import io.element.android.libraries.matrix.api.core.EventId

/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

data class ReplyParameters(
    val inReplyToEventId: EventId,
    val enforceThreadReply: Boolean,
    val replyWithinThread: Boolean,
)

fun inReplyTo(
    eventId: EventId,
    enforceThreadReply: Boolean = false,
    replyWithinThread: Boolean = false,
): ReplyParameters = ReplyParameters(
    inReplyToEventId = eventId,
    enforceThreadReply = enforceThreadReply,
    replyWithinThread = replyWithinThread,
)
