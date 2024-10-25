/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.joinroom.impl

import androidx.compose.runtime.Immutable
import io.element.android.features.invite.api.response.AcceptDeclineInviteState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.room.RoomType
import io.element.android.libraries.matrix.ui.model.InviteSender

@Immutable
data class JoinRoomState(
    val contentState: ContentState,
    val acceptDeclineInviteState: AcceptDeclineInviteState,
    val joinAction: AsyncAction<Unit>,
    val knockAction: AsyncAction<Unit>,
    val cancelKnockAction: AsyncAction<Unit>,
    val applicationName: String,
    val knockMessage: String,
    val eventSink: (JoinRoomEvents) -> Unit
) {
    val joinAuthorisationStatus = when (contentState) {
        // Use the join authorisation status from the loaded content state
        is ContentState.Loaded -> contentState.joinAuthorisationStatus
        // Assume that if the room is unknown, the user can join it
        is ContentState.UnknownRoom -> JoinAuthorisationStatus.CanJoin
        // Otherwise assume that the user can't join the room
        else -> JoinAuthorisationStatus.Unknown
    }
}

@Immutable
sealed interface ContentState {
    data class Loading(val roomIdOrAlias: RoomIdOrAlias) : ContentState
    data class Failure(val roomIdOrAlias: RoomIdOrAlias, val error: Throwable) : ContentState
    data class UnknownRoom(val roomIdOrAlias: RoomIdOrAlias) : ContentState
    data class Loaded(
        val roomId: RoomId,
        val name: String?,
        val topic: String?,
        val alias: RoomAlias?,
        val numberOfMembers: Long?,
        val isDm: Boolean,
        val roomType: RoomType,
        val roomAvatarUrl: String?,
        val joinAuthorisationStatus: JoinAuthorisationStatus,
    ) : ContentState {
        val showMemberCount = numberOfMembers != null

        fun avatarData(size: AvatarSize): AvatarData {
            return AvatarData(
                id = roomId.value,
                name = name,
                url = roomAvatarUrl,
                size = size,
            )
        }
    }
}

sealed interface JoinAuthorisationStatus {
    data class IsInvited(val inviteSender: InviteSender?) : JoinAuthorisationStatus
    data object IsKnocked : JoinAuthorisationStatus
    data object CanKnock : JoinAuthorisationStatus
    data object CanJoin : JoinAuthorisationStatus
    data object Unknown : JoinAuthorisationStatus
}
