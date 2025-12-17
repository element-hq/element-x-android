/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.room

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.api.room.getDirectRoomMember
import io.element.android.libraries.matrix.api.room.roomMembers

@Composable
fun BaseRoom.getRoomMemberAsState(userId: UserId): State<RoomMember?> {
    val roomMembersState by membersStateFlow.collectAsState()
    return getRoomMemberAsState(roomMembersState = roomMembersState, userId = userId)
}

@Composable
fun getRoomMemberAsState(roomMembersState: RoomMembersState, userId: UserId): State<RoomMember?> {
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
fun BaseRoom.getDirectRoomMember(roomMembersState: RoomMembersState): State<RoomMember?> {
    val roomInfo by roomInfoFlow.collectAsState()
    return remember {
        derivedStateOf {
            roomMembersState.getDirectRoomMember(roomInfo, sessionId)
        }
    }
}

@Composable
fun BaseRoom.getCurrentRoomMember(roomMembersState: RoomMembersState): State<RoomMember?> {
    return getRoomMemberAsState(roomMembersState = roomMembersState, userId = sessionId)
}
