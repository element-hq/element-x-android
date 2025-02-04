/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api.notifications

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

interface NotificationCleaner {
    fun clearAllMessagesEvents(sessionId: SessionId)
    fun clearMessagesForRoom(sessionId: SessionId, roomId: RoomId)
    fun clearEvent(sessionId: SessionId, eventId: EventId)

    fun clearMembershipNotificationForSession(sessionId: SessionId)
    fun clearMembershipNotificationForRoom(sessionId: SessionId, roomId: RoomId)
}
