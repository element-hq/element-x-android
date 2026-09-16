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
import io.element.android.libraries.push.impl.push.PushRequestStatus
import kotlin.time.Instant

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

    /**
     * Adds or replaces an existing [PushRequest] in the local database.
     */
    suspend fun insertOrUpdatePushRequest(pushRequest: PushRequest): Result<Unit>

    /**
     * Replace a list of [PushRequest] in the database.
     */
    suspend fun insertOrUpdatePushRequests(pushRequests: List<PushRequest>): Result<Unit>

    /**
     * Gets [PushRequestStatus.PENDING] push requests from the local database for a [SessionId].
     * A [since] param can optionally be provided to only return those received after that date.
     */
    suspend fun getPendingPushRequests(sessionId: SessionId, since: Instant?): Result<List<PushRequest>>

    /**
     * Removes the oldest push requests for a [SessionId].
     */
    suspend fun removeOldPushRequests(sessionId: SessionId): Result<Unit>
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
