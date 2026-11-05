/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.messages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.collections.immutable.ImmutableList

/**
 * The room's member profiles, for composables that resolve a member at render time rather than
 * having it baked into their state.
 */
val LocalRoomMemberProfilesCache = staticCompositionLocalOf { RoomMemberProfilesCache() }

/**
 * The profile for [userId], or null when the room member list has nothing for them.
 */
@Composable
fun rememberRoomMember(userId: UserId): RoomMember? {
    val cache = LocalRoomMemberProfilesCache.current
    val membersState = cache.membersFlow.collectAsState()
    return remember(userId, membersState) {
        derivedStateOf { membersState.value[userId] }
    }.value
}

/**
 * Provides a cache populated with [members] for previews.
 */
@Composable
fun ProvideRoomMemberProfiles(
    members: ImmutableList<RoomMember>,
    content: @Composable () -> Unit,
) {
    val cache = remember(members) {
        RoomMemberProfilesCache().apply { replace(members) }
    }
    CompositionLocalProvider(LocalRoomMemberProfilesCache provides cache, content = content)
}
