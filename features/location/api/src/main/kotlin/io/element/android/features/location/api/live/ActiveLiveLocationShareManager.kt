/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.api.live

import io.element.android.libraries.core.coroutine.mapState
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

interface ActiveLiveLocationShareManager {
    /** All rooms currently sharing live location on this device. */
    val sharingRoomIds: StateFlow<Set<RoomId>>

    suspend fun setup()

    /**
     * Starts live location sharing in the given room.
     * Calls room.startLiveLocationShare() on the SDK, registers the share,
     * and starts the foreground GPS service if not already running.
     */
    suspend fun startShare(roomId: RoomId, duration: Duration): Result<Unit>

    /**
     * Stops live location sharing in the given room.
     * Calls room.stopLiveLocationShare() on the SDK, removes the share,
     * and stops the foreground service if no shares remain.
     */
    suspend fun stopShare(roomId: RoomId): Result<Unit>
}

fun ActiveLiveLocationShareManager.isCurrentlySharing(roomId: RoomId): StateFlow<Boolean> {
    return sharingRoomIds.mapState { roomId in it }
}
