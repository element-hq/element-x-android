/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.messages

import androidx.compose.runtime.staticCompositionLocalOf
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@SingleIn(RoomScope::class)
class RoomMemberProfilesCache @Inject constructor() {
    private val cache = MutableStateFlow(mapOf<UserId, RoomMember>())

    private val _lastCacheUpdate = MutableStateFlow(0L)
    val lastCacheUpdate: StateFlow<Long> = _lastCacheUpdate

    fun replace(items: List<RoomMember>) {
        cache.value = items.associateBy { it.userId }
        _lastCacheUpdate.tryEmit(_lastCacheUpdate.value + 1)
    }

    fun getDisplayName(userId: UserId): String? {
        return cache.value[userId]?.disambiguatedDisplayName
    }
}

val LocalRoomMemberProfilesCache = staticCompositionLocalOf {
    RoomMemberProfilesCache()
}
