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

import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.features.userprofile.shared.UserProfileState
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings

data class RoomDetailsState(
    val roomId: RoomId,
    val roomName: String,
    val roomAlias: RoomAlias?,
    val roomAvatarUrl: String?,
    val roomTopic: RoomTopicState,
    val memberCount: Long,
    val isEncrypted: Boolean,
    val roomType: RoomDetailsType,
    val roomMemberDetailsState: UserProfileState?,
    val canEdit: Boolean,
    val canInvite: Boolean,
    val canShowNotificationSettings: Boolean,
    val leaveRoomState: LeaveRoomState,
    val roomNotificationSettings: RoomNotificationSettings?,
    val isFavorite: Boolean,
    val displayRolesAndPermissionsSettings: Boolean,
    val eventSink: (RoomDetailsEvent) -> Unit
)

sealed interface RoomDetailsType {
    data object Room : RoomDetailsType
    data class Dm(val roomMember: RoomMember) : RoomDetailsType
}

sealed interface RoomTopicState {
    data object Hidden : RoomTopicState
    data object CanAddTopic : RoomTopicState
    data class ExistingTopic(val topic: String) : RoomTopicState
}
