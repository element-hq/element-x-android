/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.features.leaveroom.api.aLeaveRoomState
import io.element.android.features.roomdetails.impl.members.aRoomMember
import io.element.android.features.userprofile.api.UserProfileState
import io.element.android.features.userprofile.shared.aUserProfileState
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import kotlinx.collections.immutable.toPersistentList

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
            aRoomDetailsState(canCall = false, canInvite = false),
            aRoomDetailsState(isPublic = false),
            aRoomDetailsState(heroes = aMatrixUserList()),
            aRoomDetailsState(pinnedMessagesCount = 3),
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
    canCall: Boolean = true,
    roomType: RoomDetailsType = RoomDetailsType.Room,
    roomMemberDetailsState: UserProfileState? = null,
    leaveRoomState: LeaveRoomState = aLeaveRoomState(),
    roomNotificationSettings: RoomNotificationSettings = aRoomNotificationSettings(),
    isFavorite: Boolean = false,
    displayAdminSettings: Boolean = false,
    isPublic: Boolean = true,
    heroes: List<MatrixUser> = emptyList(),
    canShowPinnedMessages: Boolean = true,
    pinnedMessagesCount: Int? = null,
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
    canCall = canCall,
    roomType = roomType,
    roomMemberDetailsState = roomMemberDetailsState,
    leaveRoomState = leaveRoomState,
    roomNotificationSettings = roomNotificationSettings,
    isFavorite = isFavorite,
    displayRolesAndPermissionsSettings = displayAdminSettings,
    isPublic = isPublic,
    heroes = heroes.toPersistentList(),
    canShowPinnedMessages = canShowPinnedMessages,
    pinnedMessagesCount = pinnedMessagesCount,
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
    isPublic = false,
    roomType = RoomDetailsType.Dm(
        aRoomMember(),
        aDmRoomMember(isIgnored = isDmMemberIgnored),
    ),
    roomMemberDetailsState = aUserProfileState()
)
