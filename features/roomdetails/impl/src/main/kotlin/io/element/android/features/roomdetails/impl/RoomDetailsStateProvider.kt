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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.features.roomdetails.impl.members.details.aRoomMemberDetailsState
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings

open class RoomDetailsStateProvider : PreviewParameterProvider<RoomDetailsState> {
    override val values: Sequence<RoomDetailsState>
        get() = sequenceOf(
            aRoomDetailsState(),
            aRoomDetailsState().copy(roomTopic = RoomTopicState.Hidden),
            aRoomDetailsState().copy(roomTopic = RoomTopicState.CanAddTopic),
            aRoomDetailsState().copy(isEncrypted = false),
            aRoomDetailsState().copy(roomAlias = null),
            aDmRoomDetailsState().copy(roomName = "Daniel"),
            aDmRoomDetailsState(isDmMemberIgnored = true).copy(roomName = "Daniel"),
            aRoomDetailsState().copy(canInvite = true),
            aRoomDetailsState().copy(canEdit = true),
            // Add other state here
        )
}

fun aDmRoomMember(
    userId: UserId = UserId("@daniel:domain.com"),
    displayName: String? = "Daniel",
    avatarUrl: String? = null,
    membership: RoomMembershipState = RoomMembershipState.JOIN,
    isNameAmbiguous: Boolean = false,
    powerLevel: Long = 0,
    normalizedPowerLevel: Long = powerLevel,
    isIgnored: Boolean = false,
) = RoomMember(
    userId = userId,
    displayName = displayName,
    avatarUrl = avatarUrl,
    membership = membership,
    isNameAmbiguous = isNameAmbiguous,
    powerLevel = powerLevel,
    normalizedPowerLevel = normalizedPowerLevel,
    isIgnored = isIgnored,
)

fun aRoomDetailsState() = RoomDetailsState(
    roomId = "a room id",
    roomName = "Marketing",
    roomAlias = "#marketing:domain.com",
    roomAvatarUrl = null,
    roomTopic = RoomTopicState.ExistingTopic(
        "Welcome to #marketing, home of the Marketing team " +
            "|| WIKI PAGE: https://domain.org/wiki/Marketing " +
            "|| MAIL iki/Marketing " +
            "|| MAI iki/Marketing " +
            "|| MAI iki/Marketing..."
    ),
    memberCount = 32,
    isEncrypted = true,
    canInvite = false,
    canEdit = false,
    roomType = RoomDetailsType.Room,
    roomMemberDetailsState = null,
    leaveRoomState = LeaveRoomState(),
    roomNotificationSettings = Async.Success(RoomNotificationSettings(mode = RoomNotificationMode.MUTE, isDefault = false)),
    eventSink = {}
)

fun aDmRoomDetailsState(isDmMemberIgnored: Boolean = false) = aRoomDetailsState().copy(
    roomType = RoomDetailsType.Dm(aDmRoomMember(isIgnored = isDmMemberIgnored)), roomMemberDetailsState = aRoomMemberDetailsState()
)
