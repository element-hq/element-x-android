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
import io.element.android.features.leaveroom.api.aLeaveRoomState
import io.element.android.features.userprofile.shared.UserProfileState
import io.element.android.features.userprofile.shared.aUserProfileState
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings

open class RoomDetailsStateProvider : PreviewParameterProvider<RoomDetailsState> {
    override val values: Sequence<RoomDetailsState>
        get() = sequenceOf(
            aRoomDetailsState(displayAdminSettings = true),
            aRoomDetailsState(roomTopic = RoomTopicState.Hidden),
            aRoomDetailsState(roomTopic = RoomTopicState.CanAddTopic),
            aRoomDetailsState(isEncrypted = false),
            aRoomDetailsState(roomAlias = null),
            aDmRoomDetailsState(),
            aDmRoomDetailsState(isDmMemberIgnored = true),
            aRoomDetailsState(canInvite = true),
            aRoomDetailsState(isFavorite = true),
            aRoomDetailsState(
                canEdit = true,
                // Also test the roomNotificationSettings ALL_MESSAGES in the same screenshot. Icon 'Mute' should be displayed
                roomNotificationSettings = aRoomNotificationSettings(mode = RoomNotificationMode.ALL_MESSAGES, isDefault = true)
            ),
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
    role: RoomMember.Role = RoomMember.Role.USER,
) = RoomMember(
    userId = userId,
    displayName = displayName,
    avatarUrl = avatarUrl,
    membership = membership,
    isNameAmbiguous = isNameAmbiguous,
    powerLevel = powerLevel,
    normalizedPowerLevel = normalizedPowerLevel,
    isIgnored = isIgnored,
    role = role,
)

fun aRoomDetailsState(
    roomId: RoomId = RoomId("!aRoomId:domain.com"),
    roomName: String = "Marketing",
    roomAlias: RoomAlias? = RoomAlias("#marketing:domain.com"),
    roomAvatarUrl: String? = null,
    roomTopic: RoomTopicState = RoomTopicState.ExistingTopic(
        "Welcome to #marketing, home of the Marketing team " +
            "|| WIKI PAGE: https://domain.org/wiki/Marketing " +
            "|| MAIL iki/Marketing " +
            "|| MAI iki/Marketing " +
            "|| MAI iki/Marketing..."
    ),
    memberCount: Long = 32,
    isEncrypted: Boolean = true,
    canInvite: Boolean = false,
    canEdit: Boolean = false,
    canShowNotificationSettings: Boolean = true,
    roomType: RoomDetailsType = RoomDetailsType.Room,
    roomMemberDetailsState: UserProfileState? = null,
    leaveRoomState: LeaveRoomState = aLeaveRoomState(),
    roomNotificationSettings: RoomNotificationSettings = aRoomNotificationSettings(),
    isFavorite: Boolean = false,
    displayAdminSettings: Boolean = false,
    eventSink: (RoomDetailsEvent) -> Unit = {},
) = RoomDetailsState(
    roomId = roomId,
    roomName = roomName,
    roomAlias = roomAlias,
    roomAvatarUrl = roomAvatarUrl,
    roomTopic = roomTopic,
    memberCount = memberCount,
    isEncrypted = isEncrypted,
    canInvite = canInvite,
    canEdit = canEdit,
    canShowNotificationSettings = canShowNotificationSettings,
    roomType = roomType,
    roomMemberDetailsState = roomMemberDetailsState,
    leaveRoomState = leaveRoomState,
    roomNotificationSettings = roomNotificationSettings,
    isFavorite = isFavorite,
    displayRolesAndPermissionsSettings = displayAdminSettings,
    eventSink = eventSink
)

fun aRoomNotificationSettings(
    mode: RoomNotificationMode = RoomNotificationMode.MUTE,
    isDefault: Boolean = false,
) = RoomNotificationSettings(
    mode = mode,
    isDefault = isDefault
)

fun aDmRoomDetailsState(
    isDmMemberIgnored: Boolean = false,
    roomName: String = "Daniel",
) = aRoomDetailsState(
    roomName = roomName,
    roomType = RoomDetailsType.Dm(aDmRoomMember(isIgnored = isDmMemberIgnored)),
    roomMemberDetailsState = aUserProfileState()
)
