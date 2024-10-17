/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

import androidx.compose.runtime.Immutable
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.features.userprofile.api.UserProfileState
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList

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
    val canCall: Boolean,
    val leaveRoomState: LeaveRoomState,
    val roomNotificationSettings: RoomNotificationSettings?,
    val isFavorite: Boolean,
    val displayRolesAndPermissionsSettings: Boolean,
    val isPublic: Boolean,
    val heroes: ImmutableList<MatrixUser>,
    val canShowPinnedMessages: Boolean,
    val pinnedMessagesCount: Int?,
    val eventSink: (RoomDetailsEvent) -> Unit
)

@Immutable
sealed interface RoomDetailsType {
    data object Room : RoomDetailsType
    data class Dm(
        val me: RoomMember,
        val otherMember: RoomMember,
    ) : RoomDetailsType
}

@Immutable
sealed interface RoomTopicState {
    data object Hidden : RoomTopicState
    data object CanAddTopic : RoomTopicState
    data class ExistingTopic(val topic: String) : RoomTopicState
}
