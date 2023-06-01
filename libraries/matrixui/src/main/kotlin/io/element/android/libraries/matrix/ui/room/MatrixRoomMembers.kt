/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
fun MatrixRoom.getDirectRoomMember(): State<RoomMember?> {
    val roomMembersState by membersStateFlow.collectAsState()
    return getDirectRoomMember(roomMembersState = roomMembersState)
}

@Composable
fun MatrixRoom.getDirectRoomMember(roomMembersState: MatrixRoomMembersState): State<RoomMember?> {
    val roomMembers = roomMembersState.roomMembers()
    return remember(roomMembers) {
        derivedStateOf {
            if (roomMembers == null) {
                null
            } else if (roomMembers.size == 2 && isDirect && isEncrypted) {
                roomMembers.find { it.userId != this.sessionId }
            } else {
                null
            }
        }
    }
}

