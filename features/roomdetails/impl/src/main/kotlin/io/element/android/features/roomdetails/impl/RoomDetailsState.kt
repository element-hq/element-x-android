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

package io.element.android.features.roomdetails.impl

import io.element.android.libraries.matrix.api.room.MatrixRoom

data class RoomDetailsState(
    val roomId: String,
    val roomName: String,
    val roomAlias: String?,
    val roomAvatarUrl: String?,
    val roomTopic: String?,
    val memberCount: Int,
    val isEncrypted: Boolean,
    val displayLeaveRoomWarning: LeaveRoomWarning?,
    val error: RoomDetailsError?,
    val eventSink: (RoomDetailsEvent) -> Unit
)

sealed class LeaveRoomWarning {
    object Generic : LeaveRoomWarning()
    object PrivateRoom : LeaveRoomWarning()
    object LastUserInRoom : LeaveRoomWarning()

    companion object {
        fun computeLeaveRoomWarning(room: MatrixRoom): LeaveRoomWarning {
            return when {
                !room.isPublic -> PrivateRoom
                room.members.size == 1 -> LastUserInRoom
                else -> Generic
            }
        }
    }
}

sealed interface RoomDetailsError {
    object AlertGeneric : RoomDetailsError
}
