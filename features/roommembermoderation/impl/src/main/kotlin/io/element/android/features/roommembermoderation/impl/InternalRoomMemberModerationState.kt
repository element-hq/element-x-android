/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommembermoderation.impl

import io.element.android.features.roommembermoderation.api.ModerationActionState
import io.element.android.features.roommembermoderation.api.RoomMemberModerationEvents
import io.element.android.features.roommembermoderation.api.RoomMemberModerationState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList

data class InternalRoomMemberModerationState(
    override val canKick: Boolean,
    override val canBan: Boolean,
    val selectedUser: MatrixUser?,
    val actions: ImmutableList<ModerationActionState>,
    val kickUserAsyncAction: AsyncAction<Unit>,
    val banUserAsyncAction: AsyncAction<Unit>,
    val unbanUserAsyncAction: AsyncAction<Unit>,
    override val eventSink: (RoomMemberModerationEvents) -> Unit,
) : RoomMemberModerationState {
    val canDisplayActions = actions.isNotEmpty()
}
