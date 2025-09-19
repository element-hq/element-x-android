/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.spaces

import io.element.android.libraries.core.coroutine.mapState
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.Optional

/**
 * An in memory cache of space rooms.
 * Only caches Rooms with roomType [io.element.android.libraries.matrix.api.room.RoomType.Space].
 */
class SpaceRoomCache {
    private val inMemoryCache = MutableStateFlow<Map<RoomId, SpaceRoom?>>(emptyMap())
    fun getSpaceRoomFlow(roomId: RoomId): StateFlow<Optional<SpaceRoom>> {
        return inMemoryCache.mapState { Optional.ofNullable(it[roomId]) }
    }

    fun update(spaceRooms: List<SpaceRoom>) {
        inMemoryCache.update { currentValues ->
            val newValues = spaceRooms
                .filter { it.isSpace }
                .associateBy { it.roomId }
            currentValues + newValues
        }
    }
}
