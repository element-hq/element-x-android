/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.api.notifications

import io.element.android.libraries.matrix.api.core.SessionId
import kotlin.math.abs

object NotificationIdProvider {
    fun getSummaryNotificationId(sessionId: SessionId): Int {
        return getOffset(sessionId) + SUMMARY_NOTIFICATION_ID
    }

    fun getRoomMessagesNotificationId(sessionId: SessionId): Int {
        return getOffset(sessionId) + ROOM_MESSAGES_NOTIFICATION_ID
    }

    fun getRoomEventNotificationId(sessionId: SessionId): Int {
        return getOffset(sessionId) + ROOM_EVENT_NOTIFICATION_ID
    }

    fun getRoomInvitationNotificationId(sessionId: SessionId): Int {
        return getOffset(sessionId) + ROOM_INVITATION_NOTIFICATION_ID
    }

    fun getFallbackNotificationId(sessionId: SessionId): Int {
        return getOffset(sessionId) + FALLBACK_NOTIFICATION_ID
    }

    fun getCallNotificationId(sessionId: SessionId): Int {
        return getOffset(sessionId) + ROOM_CALL_NOTIFICATION_ID
    }

    fun getForegroundServiceNotificationId(type: ForegroundServiceType): Int {
        return type.id * 10 + FOREGROUND_SERVICE_NOTIFICATION_ID
    }

    private fun getOffset(sessionId: SessionId): Int {
        // Compute a int from a string with a low risk of collision.
        return abs(sessionId.value.hashCode() % 100_000) * 10
    }

    private const val FALLBACK_NOTIFICATION_ID = -1
    private const val SUMMARY_NOTIFICATION_ID = 0
    private const val ROOM_MESSAGES_NOTIFICATION_ID = 1
    private const val ROOM_EVENT_NOTIFICATION_ID = 2
    private const val ROOM_INVITATION_NOTIFICATION_ID = 3
    private const val ROOM_CALL_NOTIFICATION_ID = 3

    private const val FOREGROUND_SERVICE_NOTIFICATION_ID = 4
}

enum class ForegroundServiceType(val id: Int) {
    INCOMING_CALL(1),
    ONGOING_CALL(2),
}
