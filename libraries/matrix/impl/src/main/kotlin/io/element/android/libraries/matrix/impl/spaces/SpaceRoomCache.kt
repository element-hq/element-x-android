/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.spaces

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * An in memory cache of space rooms.
 * Only caches Rooms with roomType [io.element.android.libraries.matrix.api.room.RoomType.Space].
 */
class SpaceRoomCache {
    private val inMemoryCache = ConcurrentHashMap<RoomId, MutableStateFlow<SpaceRoom?>>()
    private val mutex = Mutex()

    fun getSpaceRoomFlow(roomId: RoomId): Flow<SpaceRoom?> {
        return getMutableFlow(roomId).asStateFlow()
    }

    suspend fun update(spaceRooms: List<SpaceRoom>) = mutex.withLock {
        spaceRooms
            .filter { it.isSpace }
            .forEach { spaceRoom ->
                getMutableFlow(spaceRoom.roomId).value = spaceRoom
            }
    }

    private fun getMutableFlow(roomId: RoomId): MutableStateFlow<SpaceRoom?> {
        return inMemoryCache.getOrPut(roomId, { MutableStateFlow(null) })
    }
}
