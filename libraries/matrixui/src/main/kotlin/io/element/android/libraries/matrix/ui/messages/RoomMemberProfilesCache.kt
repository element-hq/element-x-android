/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.messages

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.withContext
import javax.inject.Inject

@SingleIn(RoomScope::class)
class RoomMemberProfilesCache @Inject constructor(
    private val coroutineDispatchers: CoroutineDispatchers,
) {
    private val cache = MutableStateFlow(mapOf<UserId, RoomMember>())
    val updateFlow = cache.drop(1).runningFold(0) { acc, _ -> acc + 1 }

    suspend fun replace(items: List<RoomMember>) = withContext(coroutineDispatchers.computation) {
        cache.value = items.associateBy { it.userId }
    }

    fun getDisplayName(userId: UserId): String? {
        return cache.value[userId]?.disambiguatedDisplayName
    }
}
