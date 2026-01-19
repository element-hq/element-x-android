/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api.store

import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.flow.Flow

/**
 * Store for tracking which rooms have custom notification channels.
 */
interface CustomNotificationChannelsStore {
    /**
     * Returns a flow of room IDs that have custom notification channels.
     */
    fun roomIdsWithCustomChannel(): Flow<Set<RoomId>>

    /**
     * Check if a room has a custom notification channel.
     * @param roomId the room ID to check.
     * @return true if the room has a custom notification channel.
     */
    suspend fun hasCustomChannel(roomId: RoomId): Boolean

    /**
     * Mark a room as having a custom notification channel.
     * @param roomId the room ID to add.
     */
    suspend fun addCustomChannel(roomId: RoomId)

    /**
     * Remove the custom notification channel for a room.
     * @param roomId the room ID to remove.
     */
    suspend fun removeCustomChannel(roomId: RoomId)

    /**
     * Delete the store.
     */
    suspend fun clear()
}
