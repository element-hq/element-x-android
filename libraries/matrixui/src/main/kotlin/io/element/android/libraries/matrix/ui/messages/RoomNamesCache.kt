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
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.runningFold

@SingleIn(RoomScope::class)
@Inject
class RoomNamesCache {
    private val cache = MutableStateFlow<ImmutableMap<RoomIdOrAlias, String>>(persistentMapOf())
    val updateFlow = cache.drop(1).runningFold(0) { acc, _ -> acc + 1 }

    /**
     * The known room names, by room id and canonical alias. Rooms without a name are not included.
     */
    val displayNames: StateFlow<ImmutableMap<RoomIdOrAlias, String>> = cache.asStateFlow()

    suspend fun replace(items: List<RoomSummary>) = coroutineScope {
        val roomNamesByRoomIdOrAlias = LinkedHashMap<RoomIdOrAlias, String>(items.size * 2)
        items
            .forEach { summary ->
                val name = summary.info.name ?: return@forEach
                roomNamesByRoomIdOrAlias[summary.info.id.toRoomIdOrAlias()] = name
                val canonicalAlias = summary.info.canonicalAlias
                if (canonicalAlias != null) {
                    roomNamesByRoomIdOrAlias[canonicalAlias.toRoomIdOrAlias()] = name
                }
            }
        cache.value = roomNamesByRoomIdOrAlias.toImmutableMap()
    }

    fun getDisplayName(roomIdOrAlias: RoomIdOrAlias): String? {
        return cache.value[roomIdOrAlias]
    }
}
