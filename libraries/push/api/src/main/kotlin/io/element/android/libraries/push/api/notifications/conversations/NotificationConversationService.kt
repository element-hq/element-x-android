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
     * Called when a message is sent to, or received in, a room.
     * It should create a new conversation shortcut for this room.
     *
     * @param sessionId the session the message belongs to.
     * @param roomId the room the message belongs to.
     * @param roomName the name to show on the shortcut, or `null` when the room has none.
     * @param roomIsDirect whether the room is a direct message, which changes how the shortcut is presented.
     * @param roomAvatarUrl the avatar to show on the shortcut, or `null` when the room has none.
     */
    suspend fun onMessageInRoom(
        sessionId: SessionId,
        roomId: RoomId,
        roomName: String?,
        roomIsDirect: Boolean,
        roomAvatarUrl: String?,
    )

    /**
     * Called when a room is left.
     * It should remove the conversation shortcut for this room.
     *
     * @param sessionId the session the room belongs to.
     * @param roomId the room that was left.
     */
    suspend fun onLeftRoom(sessionId: SessionId, roomId: RoomId)

    /**
     * Called when the list of available rooms changes.
     * It should update the conversation shortcuts accordingly, removing shortcuts for no longer joined rooms.
     *
     * @param sessionId the session the rooms belong to.
     * @param roomIds the rooms the user is currently joined to.
     */
    suspend fun onAvailableRoomsChanged(sessionId: SessionId, roomIds: Set<RoomId>)
}
