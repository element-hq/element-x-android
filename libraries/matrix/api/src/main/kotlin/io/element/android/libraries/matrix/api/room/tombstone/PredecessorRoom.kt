/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.tombstone

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId

/**
 *
 * When a room A is tombstoned, it is replaced by a room B. The room A is the
 * predecessor of B, and B is the successor of A. This type holds information
 * about the predecessor room.
 *
 * A room is tombstoned if it has received a m.room.tombstone state event.
 *
 */
data class PredecessorRoom(
    /**
     * The ID of the replaced room.
     */
    val roomId: RoomId,
    /**
     * The event ID of the last known event in the predecessor room.
     */
    val lastEventId: EventId,
)
