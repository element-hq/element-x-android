/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.api

import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.flow.Flow

/**
 * Remembers which invitations the user has already looked at, so that only genuinely new ones are highlighted in the room list.
 */
interface SeenInvitesStore {
    /**
     * Returns a flow of seen room IDs of invitation.
     */
    fun seenRoomIds(): Flow<Set<RoomId>>

    /**
     * Mark the invitation as seen.
     * Call this when the invitation details are shown to the user.
     * @param roomId the room ID of the invitation to mark as seen.
     */
    suspend fun markAsSeen(roomId: RoomId)

    /**
     * Mark the invitation as unseen.
     * Call this when the invitation has been accepted or declined.
     * @param roomId the room ID of the invitation to mark as unseen.
     */
    suspend fun markAsUnSeen(roomId: RoomId)

    /**
     * Delete the store, so every invitation counts as unseen again.
     */
    suspend fun clear()
}
