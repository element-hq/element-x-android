/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

import androidx.compose.runtime.Immutable
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.features.roomcall.api.RoomCallState
import io.element.android.features.userprofile.api.UserProfileState
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

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
    val roomCallState: RoomCallState,
    val leaveRoomState: LeaveRoomState,
    val roomNotificationSettings: RoomNotificationSettings?,
    val isFavorite: Boolean,
    val displayRolesAndPermissionsSettings: Boolean,
    val isPublic: Boolean,
    val heroes: ImmutableList<MatrixUser>,
    val pinnedMessagesCount: Int?,
    val snackbarMessage: SnackbarMessage?,
    val canShowKnockRequests: Boolean,
    val knockRequestsCount: Int?,
    val canShowSecurityAndPrivacy: Boolean,
    val hasMemberVerificationViolations: Boolean,
    val canReportRoom: Boolean,
    val isTombstoned: Boolean,
    val showDebugInfo: Boolean,
    val roomVersion: String?,
    val eventSink: (RoomDetailsEvent) -> Unit
) {
    val roomBadges = buildList {
        if (isEncrypted) {
            add(RoomBadge.ENCRYPTED)
        } else {
            add(RoomBadge.NOT_ENCRYPTED)
        }
        if (isPublic) {
            add(RoomBadge.PUBLIC)
        }
    }.toImmutableList()
}

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

enum class RoomBadge {
    ENCRYPTED,
    NOT_ENCRYPTED,
    PUBLIC,
}
