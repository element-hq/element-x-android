/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api.notifications.conversations

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

/**
 * Service to handle conversation-related notifications.
 */
interface NotificationConversationService {
    /**
     * Called when the current user sends a message in [roomId]. Refreshes the room's conversation
     * shortcut and, unlike [onMessageReceived], also ensures the room's per-room notification
     * channel exists if its notification mode is
     * [io.element.android.libraries.matrix.api.room.RoomNotificationMode.ALL_MESSAGES] - see
     * [io.element.android.libraries.push.api.notifications.RoomNotificationChannelManager]. This is
     * the only way some rooms (e.g. self-chats, which never generate an incoming notification for
     * their own sender) ever get a channel.
     */
    suspend fun onMessageSent(
        sessionId: SessionId,
        roomId: RoomId,
        roomName: String?,
        roomIsDirect: Boolean,
        roomAvatarUrl: String?,
    )

    /**
     * Called when a new, non-outgoing message is received in [roomId]. Only refreshes the room's
     * conversation shortcut - the per-room notification channel, if any, is ensured separately by
     * the notification-building pipeline itself, using that specific event's actual noisiness, so
     * this must not duplicate that with the room's static notification mode.
     */
    suspend fun onMessageReceived(
        sessionId: SessionId,
        roomId: RoomId,
        roomName: String?,
        roomIsDirect: Boolean,
        roomAvatarUrl: String?,
    )

    /**
     * Called when a room is left.
     * It should remove the conversation shortcut for this room.
     */
    suspend fun onLeftRoom(sessionId: SessionId, roomId: RoomId)

    /**
     * Called when the list of available rooms changes.
     * It should update the conversation shortcuts accordingly, removing shortcuts for no longer joined rooms.
     */
    suspend fun onAvailableRoomsChanged(sessionId: SessionId, roomIds: Set<RoomId>)
}
