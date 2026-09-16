/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.recent

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.room.RoomInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Returns a [Flow] of [RoomInfo] from recently visited rooms.
 * The flow emits items lazily, allowing callers to filter and take only what they need.
 * Use [kotlinx.coroutines.flow.take] to limit results and stop iteration early.
 *
 */
fun MatrixClient.getRecentlyVisitedRoomInfoFlow(
    predicate: (RoomInfo) -> Boolean,
): Flow<RoomInfo> = flow {
    val recentlyVisitedRooms = getRecentlyVisitedRooms().getOrDefault(emptyList())
    for (roomId in recentlyVisitedRooms) {
        getRoom(roomId)?.use { room ->
            val info = room.info()
            if (predicate(info)) {
                emit(info)
            }
        }
    }
}
