/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface RoomMembersState {
    data object Unknown : RoomMembersState
    data class Pending(val prevRoomMembers: ImmutableList<RoomMember>? = null) : RoomMembersState
    data class Error(val failure: Throwable, val prevRoomMembers: ImmutableList<RoomMember>? = null) : RoomMembersState
    data class Ready(val roomMembers: ImmutableList<RoomMember>) : RoomMembersState
}

fun RoomMembersState.roomMembers(): List<RoomMember>? {
    return when (this) {
        is RoomMembersState.Ready -> roomMembers
        is RoomMembersState.Pending -> prevRoomMembers
        is RoomMembersState.Error -> prevRoomMembers
        else -> null
    }
}

fun RoomMembersState.joinedRoomMembers(): List<RoomMember> {
    return roomMembers().orEmpty().filter { it.membership == RoomMembershipState.JOIN }
}

fun RoomMembersState.activeRoomMembers(): List<RoomMember> {
    return roomMembers().orEmpty().filter { it.membership.isActive() }
}
