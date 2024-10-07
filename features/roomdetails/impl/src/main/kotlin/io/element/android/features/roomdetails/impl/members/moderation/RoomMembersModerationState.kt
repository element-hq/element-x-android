/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members.moderation

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.collections.immutable.ImmutableList

data class RoomMembersModerationState(
    val canDisplayModerationActions: Boolean,
    val selectedRoomMember: RoomMember?,
    val actions: ImmutableList<ModerationAction>,
    val kickUserAsyncAction: AsyncAction<Unit>,
    val banUserAsyncAction: AsyncAction<Unit>,
    val unbanUserAsyncAction: AsyncAction<Unit>,
    val canDisplayBannedUsers: Boolean,
    val eventSink: (RoomMembersModerationEvents) -> Unit,
)

sealed interface ModerationAction {
    data class DisplayProfile(val userId: UserId) : ModerationAction
    data class KickUser(val userId: UserId) : ModerationAction
    data class BanUser(val userId: UserId) : ModerationAction
}
