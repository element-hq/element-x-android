/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.alias

import io.element.android.libraries.matrix.api.core.RoomId

/**
 * Information about a room, that was resolved from a room alias.
 */
data class ResolvedRoomAlias(
    /**
     * The room ID that the alias resolved to.
     */
    val roomId: RoomId,
    /**
     * A list of servers that can be used to find the room by its room ID.
     */
    val servers: List<String>
)
