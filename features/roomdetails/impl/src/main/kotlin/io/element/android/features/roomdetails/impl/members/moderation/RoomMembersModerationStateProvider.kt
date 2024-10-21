/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members.moderation

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.roomdetails.impl.members.anAlice
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.collections.immutable.toPersistentList

class RoomMembersModerationStateProvider : PreviewParameterProvider<RoomMembersModerationState> {
    override val values: Sequence<RoomMembersModerationState>
        get() = sequenceOf(
            aRoomMembersModerationState(
                selectedRoomMember = anAlice(),
                actions = listOf(
                    ModerationAction.DisplayProfile(anAlice().userId),
                ),
            ),
            aRoomMembersModerationState(
                selectedRoomMember = anAlice(),
                actions = listOf(
                    ModerationAction.DisplayProfile(anAlice().userId),
                    ModerationAction.KickUser(userId = anAlice().userId),
                ),
            ),
            aRoomMembersModerationState(
                selectedRoomMember = anAlice(),
                actions = listOf(
                    ModerationAction.DisplayProfile(anAlice().userId),
                    ModerationAction.KickUser(userId = anAlice().userId),
                    ModerationAction.BanUser(userId = anAlice().userId),
                ),
            ),
            aRoomMembersModerationState(
                selectedRoomMember = anAlice(),
                kickUserAsyncAction = AsyncAction.Loading,
            ),
            aRoomMembersModerationState(
                selectedRoomMember = anAlice(),
                banUserAsyncAction = AsyncAction.Loading,
            ),
            aRoomMembersModerationState(
                selectedRoomMember = anAlice(),
                unbanUserAsyncAction = AsyncAction.Loading,
            ),
            aRoomMembersModerationState(
                kickUserAsyncAction = AsyncAction.Failure(Exception("Failed to kick user")),
                banUserAsyncAction = AsyncAction.Failure(Exception("Failed to ban user")),
                unbanUserAsyncAction = AsyncAction.Failure(Exception("Failed to unban user")),
            ),
            aRoomMembersModerationState(
                selectedRoomMember = anAlice(),
                banUserAsyncAction = AsyncAction.ConfirmingNoParams,
            ),
            aRoomMembersModerationState(
                selectedRoomMember = anAlice(),
                unbanUserAsyncAction = ConfirmingRoomMemberAction(anAlice()),
            ),
            aRoomMembersModerationState(
                kickUserAsyncAction = AsyncAction.Success(Unit),
                banUserAsyncAction = AsyncAction.Success(Unit),
                unbanUserAsyncAction = AsyncAction.Success(Unit),
            ),
        )
}

fun aRoomMembersModerationState(
    canDisplayModerationActions: Boolean = false,
    selectedRoomMember: RoomMember? = null,
    actions: List<ModerationAction> = emptyList(),
    kickUserAsyncAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    banUserAsyncAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    unbanUserAsyncAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    canDisplayBannedUsers: Boolean = false,
    eventSink: (RoomMembersModerationEvents) -> Unit = {},
) = RoomMembersModerationState(
    canDisplayModerationActions = canDisplayModerationActions,
    selectedRoomMember = selectedRoomMember,
    actions = actions.toPersistentList(),
    kickUserAsyncAction = kickUserAsyncAction,
    banUserAsyncAction = banUserAsyncAction,
    unbanUserAsyncAction = unbanUserAsyncAction,
    canDisplayBannedUsers = canDisplayBannedUsers,
    eventSink = eventSink,
)
