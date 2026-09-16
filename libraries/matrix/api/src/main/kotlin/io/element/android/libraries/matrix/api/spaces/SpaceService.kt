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

/**
 * Gives access to the spaces the user belongs to, their hierarchy, and the operations that change it.
 */
interface SpaceService {
    /** The spaces that have no joined parent, i.e. the roots of the hierarchy shown to the user; the latest value is replayed to new collectors. */
    val topLevelSpacesFlow: SharedFlow<List<SpaceRoom>>

    /** The filters the space list can be narrowed down with; as with [topLevelSpacesFlow], the latest value is replayed. */
    val spaceFiltersFlow: SharedFlow<List<SpaceServiceFilter>>

    /**
     * Returns the spaces the given room or space is a child of, restricted to the ones the user has joined.
     *
     * @param spaceId the room or space whose parents are requested.
     */
    suspend fun joinedParents(spaceId: RoomId): Result<List<SpaceRoom>>

    /**
     * Returns what is known about a single space, or `null` if it is unknown or the lookup failed.
     *
     * @param spaceId the space to look up.
     */
    suspend fun getSpaceRoom(spaceId: RoomId): SpaceRoom?

    /**
     * Creates a paginated list of the children of a space; the caller owns it and must call `destroy` on it.
     *
     * @param id the space whose children are listed.
     */
    fun spaceRoomList(id: RoomId): SpaceRoomList

    /**
     * Get the list of spaces in which the current user can modify their rooms (adding or removing them).
     */
    suspend fun editableSpaces(): Result<List<SpaceRoom>>

    /**
     * Creates a handle to leave a space, which lets the user also pick which of its rooms to leave; the caller must close it.
     *
     * @param spaceId the space the user wants to leave.
     */
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
