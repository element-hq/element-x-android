/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.spaces

import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.flow.SharedFlow

interface SpaceService {
    val topLevelSpacesFlow: SharedFlow<List<SpaceRoom>>
    suspend fun joinedParents(spaceId: RoomId): Result<List<SpaceRoom>>

    suspend fun getSpaceRoom(spaceId: RoomId): SpaceRoom?

    fun spaceRoomList(id: RoomId): SpaceRoomList

    /**
     * Get the list of spaces in which the current user can modify their rooms (adding or removing them).
     */
    suspend fun editableSpaces(): Result<List<SpaceRoom>>

    fun getLeaveSpaceHandle(spaceId: RoomId): LeaveSpaceHandle

    /**
     * Add a child room to a space.
     * @param spaceId The space ID to which the child will be added.
     * @param childId The room ID of the child to add.
     * @return A result indicating success or failure.
     */
    suspend fun addChildToSpace(spaceId: RoomId, childId: RoomId): Result<Unit>

    /**
     * Remove a child room from a space.
     * @param spaceId The space ID from which to remove the child.
     * @param childId The room ID of the child to remove.
     * @return A result indicating success or failure.
     */
    suspend fun removeChildFromSpace(spaceId: RoomId, childId: RoomId): Result<Unit>
}
