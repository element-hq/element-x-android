/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.leaveroom.api.LeaveRoomEvent
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.features.roomcall.api.RoomCallState
import io.element.android.features.roomcall.api.aStandByCallState
import io.element.android.features.roomdetails.impl.members.aRoomMember
import io.element.android.features.userprofile.api.UserProfileState
import io.element.android.features.userprofile.api.UserProfileVerificationState
import io.element.android.features.userprofile.shared.aUserProfileState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import kotlinx.collections.immutable.toImmutableList

open class RoomDetailsStateProvider : PreviewParameterProvider<RoomDetailsState> {
    override val values: Sequence<RoomDetailsState>
        get() = sequenceOf(
            aRoomDetailsState(displayAdminSettings = true),
            aRoomDetailsState(roomTopic = RoomTopicState.Hidden, showDebugInfo = true),
            aRoomDetailsState(roomTopic = RoomTopicState.CanAddTopic),
            aRoomDetailsState(isEncrypted = false),
            aRoomDetailsState(roomAlias = null),
            aDmRoomDetailsState(),
            aDmRoomDetailsState(isDmMemberIgnored = true, roomName = "Daniel (ignored and clear)", isEncrypted = false),
            aRoomDetailsState(canInvite = true),
            aRoomDetailsState(isFavorite = true),
            aRoomDetailsState(
                canEdit = true,
                // Also test the roomNotificationSettings ALL_MESSAGES in the same screenshot. Icon 'Mute' should be displayed
                roomNotificationSettings = aRoomNotificationSettings(mode = RoomNotificationMode.ALL_MESSAGES, isDefault = true)
            ),
            aRoomDetailsState(roomCallState = aStandByCallState(false), canInvite = false),
            aRoomDetailsState(isPublic = false),
            aRoomDetailsState(heroes = aMatrixUserList()),
            aRoomDetailsState(pinnedMessagesCount = 3),
            aRoomDetailsState(knockRequestsCount = null, canShowKnockRequests = true),
            aRoomDetailsState(knockRequestsCount = 4, canShowKnockRequests = true),
            aRoomDetailsState(hasMemberVerificationViolations = true),
            aRoomDetailsState(isTombstoned = true),
            aDmRoomDetailsState(dmRoomMemberVerificationState = UserProfileVerificationState.VERIFIED),
            aDmRoomDetailsState(dmRoomMemberVerificationState = UserProfileVerificationState.VERIFICATION_VIOLATION),
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
    isIgnored: Boolean = false,
    role: RoomMember.Role = RoomMember.Role.User,
    membershipChangeReason: String? = null,
) = RoomMember(
    userId = userId,
    displayName = displayName,
    avatarUrl = avatarUrl,
    membership = membership,
    isNameAmbiguous = isNameAmbiguous,
    powerLevel = powerLevel,
    isIgnored = isIgnored,
    role = role,
    membershipChangeReason = membershipChangeReason
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
    roomCallState: RoomCallState = aStandByCallState(),
    roomType: RoomDetailsType = RoomDetailsType.Room,
    roomMemberDetailsState: UserProfileState? = null,
    leaveRoomState: LeaveRoomState = aLeaveRoomState(),
    roomNotificationSettings: RoomNotificationSettings = aRoomNotificationSettings(),
    isFavorite: Boolean = false,
    displayAdminSettings: Boolean = false,
    isPublic: Boolean = true,
    heroes: List<MatrixUser> = emptyList(),
    pinnedMessagesCount: Int? = null,
    snackbarMessage: SnackbarMessage? = null,
    canShowKnockRequests: Boolean = false,
    knockRequestsCount: Int? = null,
    canShowSecurityAndPrivacy: Boolean = true,
    hasMemberVerificationViolations: Boolean = false,
    canReportRoom: Boolean = true,
    isTombstoned: Boolean = false,
    showDebugInfo: Boolean = false,
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
    roomCallState = roomCallState,
    roomType = roomType,
    roomMemberDetailsState = roomMemberDetailsState,
    leaveRoomState = leaveRoomState,
    roomNotificationSettings = roomNotificationSettings,
    isFavorite = isFavorite,
    displayRolesAndPermissionsSettings = displayAdminSettings,
    isPublic = isPublic,
    heroes = heroes.toImmutableList(),
    pinnedMessagesCount = pinnedMessagesCount,
    snackbarMessage = snackbarMessage,
    canShowKnockRequests = canShowKnockRequests,
    knockRequestsCount = knockRequestsCount,
    canShowSecurityAndPrivacy = canShowSecurityAndPrivacy,
    hasMemberVerificationViolations = hasMemberVerificationViolations,
    canReportRoom = canReportRoom,
    isTombstoned = isTombstoned,
    showDebugInfo = showDebugInfo,
    roomVersion = "12",
    eventSink = eventSink,
)

internal fun aLeaveRoomState(
    eventSink: (LeaveRoomEvent) -> Unit = {}
) = object : LeaveRoomState {
    override val eventSink: (LeaveRoomEvent) -> Unit = eventSink
}

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
    isEncrypted: Boolean = true,
    dmRoomMemberVerificationState: UserProfileVerificationState = UserProfileVerificationState.UNKNOWN,
) = aRoomDetailsState(
    roomName = roomName,
    isPublic = false,
    isEncrypted = isEncrypted,
    roomType = RoomDetailsType.Dm(
        me = aRoomMember(),
        otherMember = aDmRoomMember(isIgnored = isDmMemberIgnored),
    ),
    roomMemberDetailsState = aUserProfileState(
        isBlocked = AsyncData.Success(isDmMemberIgnored),
        verificationState = dmRoomMemberVerificationState,
    )
)
