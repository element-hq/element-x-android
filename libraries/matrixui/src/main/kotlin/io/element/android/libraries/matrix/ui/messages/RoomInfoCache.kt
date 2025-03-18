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
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.withContext
import javax.inject.Inject

@SingleIn(RoomScope::class)
class RoomInfoCache @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
) {
    private val cache = MutableStateFlow(mapOf<RoomIdOrAlias, String?>())
    val updateFlow = cache.runningFold(0) { acc, _ -> acc + 1 }

    suspend fun replace(items: List<RoomSummary>) = withContext(dispatchers.computation) {
        val roomInfoByIdOrAlias = LinkedHashMap<RoomIdOrAlias, String?>(items.size * 2)
        items
            // makes sure to always have the same order
            .sortedBy { summary -> summary.roomId.value }
            .forEach { summary ->
                roomInfoByIdOrAlias[summary.info.id.toRoomIdOrAlias()] = summary.info.name
                val canonicalAlias = summary.info.canonicalAlias
                if (canonicalAlias != null) {
                    roomInfoByIdOrAlias[canonicalAlias.toRoomIdOrAlias()] = summary.info.name
                }
            }
        if (roomInfoByIdOrAlias != cache.value) {
            cache.value = roomInfoByIdOrAlias
        }
    }

    fun getDisplayName(roomIdOrAlias: RoomIdOrAlias): String? {
        return cache.value[roomIdOrAlias]
    }
}
