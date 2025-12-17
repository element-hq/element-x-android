/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.messages

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.runningFold

@SingleIn(RoomScope::class)
@Inject
class RoomMemberProfilesCache {
    private val cache = MutableStateFlow(mapOf<UserId, RoomMember>())
    val updateFlow = cache.drop(1).runningFold(0) { acc, _ -> acc + 1 }

    suspend fun replace(items: List<RoomMember>) = coroutineScope {
        cache.value = items.associateBy { it.userId }
    }

    fun getDisplayName(userId: UserId): String? {
        return cache.value[userId]?.disambiguatedDisplayName
    }
}
