/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.sharing.api

interface SharingShortcutsManager {
    /**
     * Publish shortcuts for the given rooms.
     * Call this from a background coroutine or viewModelScope ideally.
     */
    suspend fun publishShortcutsForRooms(rooms: List<SharingRoomInfo>)

    /**
     * Remove a room shortcut (call when user leaves the room).
     * This removes the stored mapping as well.
     */
    fun removeShortcutForRoom(roomId: String)
}

/** Light-weight room descriptor used by the manager. */
data class SharingRoomInfo(
    val roomId: String,
    val sessionId: String,
    val displayName: String,
    val avatarUrl: String?,
)
