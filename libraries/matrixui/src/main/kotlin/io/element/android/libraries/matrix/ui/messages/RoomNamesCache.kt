/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.messages

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.runningFold

@SingleIn(RoomScope::class)
@Inject
class RoomNamesCache {
    private val cache = MutableStateFlow(mapOf<RoomIdOrAlias, String?>())
    val updateFlow = cache.drop(1).runningFold(0) { acc, _ -> acc + 1 }

    suspend fun replace(items: List<RoomSummary>) = coroutineScope {
        val roomNamesByRoomIdOrAlias = LinkedHashMap<RoomIdOrAlias, String?>(items.size * 2)
        items
            .forEach { summary ->
                roomNamesByRoomIdOrAlias[summary.info.id.toRoomIdOrAlias()] = summary.info.name
                val canonicalAlias = summary.info.canonicalAlias
                if (canonicalAlias != null) {
                    roomNamesByRoomIdOrAlias[canonicalAlias.toRoomIdOrAlias()] = summary.info.name
                }
            }
        cache.value = roomNamesByRoomIdOrAlias
    }

    fun getDisplayName(roomIdOrAlias: RoomIdOrAlias): String? {
        return cache.value[roomIdOrAlias]
    }
}
