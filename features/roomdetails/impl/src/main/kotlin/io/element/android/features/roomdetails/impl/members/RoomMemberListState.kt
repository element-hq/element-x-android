/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members

import io.element.android.features.roommembermoderation.api.RoomMemberModerationState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class RoomMemberListState(
    // Only used to know if we can show the banned section
    private val roomMembers: AsyncData<RoomMembers>,
    val filteredRoomMembers: AsyncData<RoomMembers>,
    val searchQuery: String,
    val canInvite: Boolean,
    val selectedSection: SelectedSection,
    val moderationState: RoomMemberModerationState,
    val eventSink: (RoomMemberListEvents) -> Unit,
) {
    val showBannedSection: Boolean = moderationState.canBan && roomMembers.dataOrNull()?.banned?.isNotEmpty() == true
}

enum class SelectedSection {
    MEMBERS,
    BANNED
}

data class RoomMembers(
    val invited: ImmutableList<RoomMemberWithIdentityState>,
    val joined: ImmutableList<RoomMemberWithIdentityState>,
    val banned: ImmutableList<RoomMemberWithIdentityState>,
) {
    fun isEmpty(section: SelectedSection): Boolean {
        return when (section) {
            SelectedSection.MEMBERS -> invited.isEmpty() && joined.isEmpty()
            SelectedSection.BANNED -> banned.isEmpty()
        }
    }

    fun filter(query: String): RoomMembers {
        if (query.isBlank()) {
            return this
        }
        val filterPredicate = { member: RoomMemberWithIdentityState ->
            member.roomMember.userId.value.contains(query, ignoreCase = true) ||
                member.roomMember.displayName?.contains(query, ignoreCase = true).orFalse()
        }
        return RoomMembers(
            invited = invited.filter(filterPredicate).toImmutableList(),
            joined = joined.filter(filterPredicate).toImmutableList(),
            banned = banned.filter(filterPredicate).toImmutableList(),
        )
    }
}

data class RoomMemberWithIdentityState(
    val roomMember: RoomMember,
    val identityState: IdentityState?,
)
