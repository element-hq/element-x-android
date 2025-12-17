/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.tombstone

import io.element.android.libraries.matrix.api.core.RoomId

/**
 *
 * When a room A is tombstoned, it is replaced by a room B. The room A is the
 * predecessor of B, and B is the successor of A. This type holds information
 * about the successor room.
 *
 * A room is tombstoned if it has received a m.room.tombstone state event.
 *
 */
data class SuccessorRoom(
    /**
     * The ID of the replacement room.
     */
    val roomId: RoomId,
    /**
     * The message explaining why the room has been tombstoned.
     */
    val reason: String?,
)
