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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * An in memory cache of space rooms.
 * Only caches Rooms with roomType [io.element.android.libraries.matrix.api.room.RoomType.Space].
 */
class SpaceRoomCache {
    private val inMemoryCache = MutableStateFlow<MutableMap<RoomId, SpaceRoom>>(LinkedHashMap())
    private val mutex = Mutex()

    fun getSpaceRoomFlow(roomId: RoomId): Flow<SpaceRoom?> {
        return inMemoryCache.map { it[roomId] }
    }

    suspend fun update(spaceRooms: List<SpaceRoom>) = mutex.withLock {
        inMemoryCache.update { cache ->
            spaceRooms
                .filter { it.isSpace }
                .forEach { spaceRoom ->
                    cache.put(spaceRoom.roomId, spaceRoom)
                }
            cache
        }
    }
}
