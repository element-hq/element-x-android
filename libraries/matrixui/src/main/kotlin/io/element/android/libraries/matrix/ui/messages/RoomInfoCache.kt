/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.messages

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.LinkedHashMap
import javax.inject.Inject

@SingleIn(RoomScope::class)
class RoomInfoCache @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
) {
    private val cache = MutableStateFlow(mapOf<RoomIdOrAlias, MatrixRoomInfo>())

    private val _lastCacheUpdate = MutableStateFlow(0L)
    val lastCacheUpdate: StateFlow<Long> = _lastCacheUpdate

    suspend fun replace(items: List<RoomSummary>) = withContext(dispatchers.computation) {
        val cachedValues = LinkedHashMap<RoomIdOrAlias, MatrixRoomInfo>(items.size *2)
        items.forEach { summary ->
            cachedValues[summary.info.id.toRoomIdOrAlias()] = summary.info
            val canonicalAlias = summary.info.canonicalAlias
            if(canonicalAlias != null) {
                cachedValues[canonicalAlias.toRoomIdOrAlias()] = summary.info
            }
        }
        cache.value = cachedValues
    }

    fun getDisplayName(roomIdOrAlias: RoomIdOrAlias): String? {
        return cache.value[roomIdOrAlias]?.name
    }
}
