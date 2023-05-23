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

import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsState
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.room.RoomMember

data class RoomDetailsState(
    val roomId: String,
    val roomName: String,
    val roomAlias: String?,
    val roomAvatarUrl: String?,
    val roomTopic: String?,
    val memberCount: Async<Int>,
    val isEncrypted: Boolean,
    val displayLeaveRoomWarning: LeaveRoomWarning?,
    val error: RoomDetailsError?,
    val roomType: RoomDetailsType,
    val roomMemberDetailsState: RoomMemberDetailsState?,
    val canInvite: Boolean,
    val eventSink: (RoomDetailsEvent) -> Unit
)

sealed interface RoomDetailsType {
    object Room : RoomDetailsType
    data class Dm(val roomMember: RoomMember) : RoomDetailsType
}

sealed class LeaveRoomWarning {
    object Generic : LeaveRoomWarning()
    object PrivateRoom : LeaveRoomWarning()
    object LastUserInRoom : LeaveRoomWarning()

    companion object {
        fun computeLeaveRoomWarning(isPublic: Boolean, memberCount: Async<Int>): LeaveRoomWarning {
            return when {
                !isPublic -> PrivateRoom
                (memberCount as? Async.Success<Int>)?.state == 1 -> LastUserInRoom
                else -> Generic
            }
        }
    }
}

sealed interface RoomDetailsError {
    object AlertGeneric : RoomDetailsError
}
