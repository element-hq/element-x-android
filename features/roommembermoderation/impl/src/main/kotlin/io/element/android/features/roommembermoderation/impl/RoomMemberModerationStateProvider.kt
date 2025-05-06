/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommembermoderation.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.roommembermoderation.api.ModerationAction
import io.element.android.features.roommembermoderation.api.RoomMemberModerationEvents
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import kotlinx.collections.immutable.toPersistentList

class RoomMemberModerationStateProvider : PreviewParameterProvider<InternalRoomMemberModerationState> {
    override val values: Sequence<InternalRoomMemberModerationState>
        get() = sequenceOf(
            aRoomMembersModerationState(
                selectedRoomMember = AsyncData.Success(anAlice()),
                actions = listOf(
                    ModerationAction.DisplayProfile(anAlice()),
                ),
            ),
            aRoomMembersModerationState(
                selectedRoomMember = AsyncData.Success(anAlice()),
                actions = listOf(
                    ModerationAction.DisplayProfile(anAlice()),
                    ModerationAction.KickUser(anAlice()),
                ),
            ),
            aRoomMembersModerationState(
                selectedRoomMember = AsyncData.Success(anAlice()),
                actions = listOf(
                    ModerationAction.DisplayProfile(anAlice()),
                    ModerationAction.KickUser(anAlice()),
                    ModerationAction.BanUser(anAlice()),
                    ),
            ),
            aRoomMembersModerationState(
                selectedRoomMember = AsyncData.Success(anAlice()),
                kickUserAsyncAction = AsyncAction.ConfirmingNoParams,
            ),
            aRoomMembersModerationState(
                selectedRoomMember = AsyncData.Success(anAlice()),
                kickUserAsyncAction = AsyncAction.Loading,
            ),
            aRoomMembersModerationState(
                selectedRoomMember = AsyncData.Success(anAlice()),
                banUserAsyncAction = AsyncAction.ConfirmingNoParams,
            ),
            aRoomMembersModerationState(
                selectedRoomMember = AsyncData.Success(anAlice()),
                banUserAsyncAction = AsyncAction.Loading,
            ),
        )
}

fun anAlice() = RoomMember(
    UserId(value = "@alice:server.org"),
    displayName = "Alice",
    avatarUrl = null,
    role = RoomMember.Role.forPowerLevel(100L),
    membership = RoomMembershipState.JOIN,
    isNameAmbiguous = false,
    powerLevel = 100L,
    normalizedPowerLevel = 100L,
    isIgnored = false,
    membershipChangeReason = null,
)

fun aRoomMembersModerationState(
    canKick: Boolean = false,
    canBan: Boolean = false,
    selectedRoomMember: AsyncData<RoomMember> = AsyncData.Uninitialized,
    actions: List<ModerationAction> = emptyList(),
    kickUserAsyncAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    banUserAsyncAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    unbanUserAsyncAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (RoomMemberModerationEvents) -> Unit = {},
) = InternalRoomMemberModerationState(
    canKick = canKick,
    canBan = canBan,
    selectedRoomMember = selectedRoomMember,
    actions = actions.toPersistentList(),
    kickUserAsyncAction = kickUserAsyncAction,
    banUserAsyncAction = banUserAsyncAction,
    unbanUserAsyncAction = unbanUserAsyncAction,
    eventSink = eventSink,
)
