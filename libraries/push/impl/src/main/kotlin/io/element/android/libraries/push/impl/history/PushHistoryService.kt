/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.history

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.impl.db.PushRequest

interface PushHistoryService {
    /**
     * Create a new push history entry.
     * Do not use directly, prefer using the extension functions.
     */
    fun onPushResult(
        providerInfo: String,
        eventId: EventId?,
        roomId: RoomId?,
        sessionId: SessionId?,
        hasBeenResolved: Boolean,
        includeDeviceState: Boolean,
        comment: String?,
    )

    suspend fun enqueuePushRequest(pushRequest: PushRequest): Result<Unit>

    suspend fun replacePushRequests(pushRequests: List<PushRequest>): Result<Unit>

    suspend fun getPendingPushRequests(sessionId: SessionId): Result<List<PushRequest>>
}

fun PushHistoryService.onInvalidPushReceived(
    providerInfo: String,
    data: String,
) = onPushResult(
    providerInfo = providerInfo,
    eventId = null,
    roomId = null,
    sessionId = null,
    hasBeenResolved = false,
    includeDeviceState = false,
    comment = "Invalid or ignored push data:\n$data",
)

fun PushHistoryService.onUnableToRetrieveSession(
    providerInfo: String,
    eventId: EventId,
    roomId: RoomId,
    reason: String,
) = onPushResult(
    providerInfo = providerInfo,
    eventId = eventId,
    roomId = roomId,
    sessionId = null,
    hasBeenResolved = false,
    includeDeviceState = true,
    comment = "Unable to retrieve session: $reason",
)

fun PushHistoryService.onUnableToResolveEvent(
    providerInfo: String,
    eventId: EventId,
    roomId: RoomId,
    sessionId: SessionId,
    reason: String,
) = onPushResult(
    providerInfo = providerInfo,
    eventId = eventId,
    roomId = roomId,
    sessionId = sessionId,
    hasBeenResolved = false,
    includeDeviceState = true,
    comment = "Unable to resolve event: $reason",
)

fun PushHistoryService.onSuccess(
    providerInfo: String,
    eventId: EventId,
    roomId: RoomId,
    sessionId: SessionId,
    comment: String?,
) = onPushResult(
    providerInfo = providerInfo,
    eventId = eventId,
    roomId = roomId,
    sessionId = sessionId,
    hasBeenResolved = true,
    includeDeviceState = false,
    comment = buildString {
        append("Success")
        if (comment.isNullOrBlank().not()) {
            append(" - $comment")
        }
    },
)

fun PushHistoryService.onDiagnosticPush(
    providerInfo: String,
) = onPushResult(
    providerInfo = providerInfo,
    eventId = null,
    roomId = null,
    sessionId = null,
    hasBeenResolved = true,
    includeDeviceState = false,
    comment = "Diagnostic push",
)
