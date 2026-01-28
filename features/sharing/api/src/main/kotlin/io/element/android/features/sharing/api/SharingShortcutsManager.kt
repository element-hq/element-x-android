/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.sharing.api

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

interface SharingShortcutsManager {
    /**
     * Publish shortcuts for the given rooms.
     * Call this from a background coroutine or viewModelScope ideally.
     */
    suspend fun publishShortcutsForRooms(rooms: List<SharingRoomInfo>)
}

/** Light-weight room descriptor used by the manager. */
data class SharingRoomInfo(
    val sessionId: SessionId,
    val roomId: RoomId,
    val displayName: String,
    val avatarUrl: String?,
)
