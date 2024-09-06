/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface MatrixRoomMembersState {
    data object Unknown : MatrixRoomMembersState
    data class Pending(val prevRoomMembers: ImmutableList<RoomMember>? = null) : MatrixRoomMembersState
    data class Error(val failure: Throwable, val prevRoomMembers: ImmutableList<RoomMember>? = null) : MatrixRoomMembersState
    data class Ready(val roomMembers: ImmutableList<RoomMember>) : MatrixRoomMembersState
}

fun MatrixRoomMembersState.roomMembers(): List<RoomMember>? {
    return when (this) {
        is MatrixRoomMembersState.Ready -> roomMembers
        is MatrixRoomMembersState.Pending -> prevRoomMembers
        is MatrixRoomMembersState.Error -> prevRoomMembers
        else -> null
    }
}

fun MatrixRoomMembersState.joinedRoomMembers(): List<RoomMember> {
    return roomMembers().orEmpty().filter { it.membership == RoomMembershipState.JOIN }
}

fun MatrixRoomMembersState.activeRoomMembers(): List<RoomMember> {
    return roomMembers().orEmpty().filter { it.membership.isActive() }
}
