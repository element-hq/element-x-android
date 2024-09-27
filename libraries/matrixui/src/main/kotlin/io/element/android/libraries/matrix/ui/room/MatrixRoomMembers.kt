/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.room

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.roomMembers

@Composable
fun MatrixRoom.getRoomMemberAsState(userId: UserId): State<RoomMember?> {
    val roomMembersState by membersStateFlow.collectAsState()
    return getRoomMemberAsState(roomMembersState = roomMembersState, userId = userId)
}

@Composable
fun getRoomMemberAsState(roomMembersState: MatrixRoomMembersState, userId: UserId): State<RoomMember?> {
    val roomMembers = roomMembersState.roomMembers()
    return remember(roomMembers) {
        derivedStateOf {
            roomMembers?.find {
                it.userId == userId
            }
        }
    }
}

@Composable
fun MatrixRoom.getDirectRoomMember(roomMembersState: MatrixRoomMembersState): State<RoomMember?> {
    val roomMembers = roomMembersState.roomMembers()
    return remember(roomMembersState) {
        derivedStateOf {
            roomMembers
                ?.filter { it.membership.isActive() }
                ?.takeIf { it.size == 2 && isDirect }
                ?.find { it.userId != sessionId }
        }
    }
}

@Composable
fun MatrixRoom.getCurrentRoomMember(roomMembersState: MatrixRoomMembersState): State<RoomMember?> {
    return getRoomMemberAsState(roomMembersState = roomMembersState, userId = sessionId)
}
