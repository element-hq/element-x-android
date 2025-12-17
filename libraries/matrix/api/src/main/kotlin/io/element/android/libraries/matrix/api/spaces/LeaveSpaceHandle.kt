/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.spaces

import io.element.android.libraries.matrix.api.core.RoomId

interface LeaveSpaceHandle {
    /**
     * The id of the space to leave.
     */
    val id: RoomId

    /**
     * Get a list of rooms that can be left when leaving the space.
     * It will include the current space and all the subspaces and rooms that the user has joined.
     */
    suspend fun rooms(): Result<List<LeaveSpaceRoom>>

    /**
     * Leave the space and the given rooms.
     * If [roomIds] is empty, only the space will be left.
     */
    suspend fun leave(roomIds: List<RoomId>): Result<Unit>

    /**
     * Close the handle and free resources.
     */
    fun close()
}
