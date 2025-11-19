/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommembermoderation.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.roommembermoderation.api.ModerationAction
import io.element.android.features.roommembermoderation.api.ModerationActionState
import io.element.android.features.roommembermoderation.api.RoomMemberModerationEvents
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.toImmutableList

class InternalRoomMemberModerationStateProvider : PreviewParameterProvider<InternalRoomMemberModerationState> {
    override val values: Sequence<InternalRoomMemberModerationState>
        get() = sequenceOf(
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                actions = listOf(
                    ModerationActionState(action = ModerationAction.DisplayProfile, isEnabled = true),
                ),
            ),
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                actions = listOf(
                    ModerationActionState(action = ModerationAction.DisplayProfile, isEnabled = true),
                    ModerationActionState(action = ModerationAction.KickUser, isEnabled = true),
                ),
            ),
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                actions = listOf(
                    ModerationActionState(action = ModerationAction.DisplayProfile, isEnabled = true),
                    ModerationActionState(action = ModerationAction.KickUser, isEnabled = false),
                    ModerationActionState(action = ModerationAction.BanUser, isEnabled = true),
                    ),
            ),
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                actions = listOf(
                    ModerationActionState(action = ModerationAction.DisplayProfile, isEnabled = true),
                    ModerationActionState(action = ModerationAction.KickUser, isEnabled = false),
                    ModerationActionState(action = ModerationAction.UnbanUser, isEnabled = true),
                ),
            ),
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                kickUserAsyncAction = AsyncAction.ConfirmingNoParams,
            ),
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                kickUserAsyncAction = AsyncAction.Loading,
            ),
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                banUserAsyncAction = AsyncAction.ConfirmingNoParams,
            ),
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                banUserAsyncAction = AsyncAction.Loading,
            ),
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                unbanUserAsyncAction = AsyncAction.ConfirmingNoParams,
            ),
            aRoomMembersModerationState(
                selectedUser = anAlice(),
                unbanUserAsyncAction = AsyncAction.Loading,
            ),
        )
}

fun anAlice() = MatrixUser(
    UserId(value = "@alice:server.org"),
    displayName = "Alice",
    avatarUrl = null,
)

fun aRoomMembersModerationState(
    canKick: Boolean = false,
    canBan: Boolean = false,
    selectedUser: MatrixUser? = null,
    actions: List<ModerationActionState> = emptyList(),
    kickUserAsyncAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    banUserAsyncAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    unbanUserAsyncAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (RoomMemberModerationEvents) -> Unit = {},
) = InternalRoomMemberModerationState(
    canKick = canKick,
    canBan = canBan,
    selectedUser = selectedUser,
    actions = actions.toImmutableList(),
    kickUserAsyncAction = kickUserAsyncAction,
    banUserAsyncAction = banUserAsyncAction,
    unbanUserAsyncAction = unbanUserAsyncAction,
    eventSink = eventSink,
)
