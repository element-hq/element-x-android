/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.message

import io.element.android.libraries.matrix.api.room.message.ReplyParameters

fun ReplyParameters.map() = org.matrix.rustcomponents.sdk.ReplyParameters(
    eventId = inReplyToEventId.value,
    enforceThread = enforceThreadReply,
    replyWithinThread = replyWithinThread,
)
