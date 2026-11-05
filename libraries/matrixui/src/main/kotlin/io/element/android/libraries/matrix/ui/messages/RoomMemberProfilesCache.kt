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
import io.element.android.libraries.matrix.api.user.DisplayedStatus
import io.element.android.libraries.matrix.ui.model.toEmojiText
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@SingleIn(RoomScope::class)
@Inject
class RoomMemberProfilesCache {
    private val cache = MutableStateFlow(mapOf<UserId, RoomMember>())
    val membersFlow: StateFlow<Map<UserId, RoomMember>> = cache.asStateFlow()

    private val _displayNames = MutableStateFlow<ImmutableMap<UserId, String>>(persistentMapOf())

    /**
     * The disambiguated display names of the room members, by user id. Members without a display name are not included.
     */
    val displayNames: StateFlow<ImmutableMap<UserId, String>> = _displayNames.asStateFlow()

    private val _displayedStatusEmojis = MutableStateFlow<ImmutableMap<UserId, String>>(persistentMapOf())

    /**
     * The emoji of the displayed status of the room members, by user id. Members without a displayed status are not included.
     */
    val displayedStatusEmojis: StateFlow<ImmutableMap<UserId, String>> = _displayedStatusEmojis.asStateFlow()

    fun replace(items: List<RoomMember>) {
        cache.value = items.associateBy { it.userId }
        _displayNames.value = items
            .filter { it.displayName != null }
            .associate { it.userId to it.disambiguatedDisplayName }
            .toImmutableMap()
        _displayedStatusEmojis.value = items
            .mapNotNull { member -> member.displayedStatus?.let { member.userId to it.toEmojiText() } }
            .toMap()
            .toImmutableMap()
    }

    fun getDisplayName(userId: UserId): String? {
        return cache.value[userId]?.disambiguatedDisplayName
    }

    fun getDisplayedStatus(userId: UserId): DisplayedStatus? {
        return cache.value[userId]?.displayedStatus
    }
}
