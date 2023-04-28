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

package io.element.android.libraries.matrix.api.room

sealed interface MatrixRoomMembersState {
    object Unknown : MatrixRoomMembersState
    data class Pending(val prevRoomMembers: List<RoomMember>? = null) : MatrixRoomMembersState
    data class Error(val failure: Throwable, val prevRoomMembers: List<RoomMember>? = null) : MatrixRoomMembersState
    data class Ready(val roomMembers: List<RoomMember>) : MatrixRoomMembersState
}

fun MatrixRoomMembersState.roomMembers(): List<RoomMember>? {
    return when (this) {
        is MatrixRoomMembersState.Ready -> roomMembers
        is MatrixRoomMembersState.Pending -> prevRoomMembers
        is MatrixRoomMembersState.Error -> prevRoomMembers
        else -> null
    }
}


