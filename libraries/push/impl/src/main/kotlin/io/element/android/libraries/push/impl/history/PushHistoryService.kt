/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.history

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

interface PushHistoryService {
    /**
     * Create a new push history entry.
     * Do not use directly, prefer using the extension functions.
     */
    fun onPushReceived(
        providerInfo: String,
        eventId: EventId?,
        roomId: RoomId?,
        sessionId: SessionId?,
        hasBeenResolved: Boolean,
        comment: String?,
    )
}

fun PushHistoryService.onInvalidPushReceived(
    providerInfo: String,
) = onPushReceived(
    providerInfo = providerInfo,
    eventId = null,
    roomId = null,
    sessionId = null,
    hasBeenResolved = false,
    comment = "Invalid push data",
)

fun PushHistoryService.onUnableToRetrieveSession(
    providerInfo: String,
    eventId: EventId,
    roomId: RoomId,
    reason: String,
) = onPushReceived(
    providerInfo = providerInfo,
    eventId = eventId,
    roomId = roomId,
    sessionId = null,
    hasBeenResolved = false,
    comment = "Unable to retrieve session: $reason",
)

fun PushHistoryService.onUnableToResolveEvent(
    providerInfo: String,
    eventId: EventId,
    roomId: RoomId,
    sessionId: SessionId,
    reason: String,
) = onPushReceived(
    providerInfo = providerInfo,
    eventId = eventId,
    roomId = roomId,
    sessionId = sessionId,
    hasBeenResolved = false,
    comment = "Unable to resolve event: $reason",
)

fun PushHistoryService.onSuccess(
    providerInfo: String,
    eventId: EventId,
    roomId: RoomId,
    sessionId: SessionId,
    comment: String?,
) = onPushReceived(
    providerInfo = providerInfo,
    eventId = eventId,
    roomId = roomId,
    sessionId = sessionId,
    hasBeenResolved = true,
    comment = buildString {
        append("Success")
        if (comment.isNullOrBlank().not()) {
            append(" - $comment")
        }
    },
)

fun PushHistoryService.onDiagnosticPush(
    providerInfo: String,
) = onPushReceived(
    providerInfo = providerInfo,
    eventId = null,
    roomId = null,
    sessionId = null,
    hasBeenResolved = true,
    comment = "Diagnostic push",
)
