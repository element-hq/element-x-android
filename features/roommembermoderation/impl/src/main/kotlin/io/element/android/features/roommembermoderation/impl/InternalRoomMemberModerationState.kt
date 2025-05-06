/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommembermoderation.impl

import io.element.android.features.roommembermoderation.api.ModerationAction
import io.element.android.features.roommembermoderation.api.RoomMemberModerationEvents
import io.element.android.features.roommembermoderation.api.RoomMemberModerationState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.collections.immutable.ImmutableList

data class InternalRoomMemberModerationState(
    override val canKick: Boolean,
    override val canBan: Boolean,
    val selectedRoomMember: AsyncData<RoomMember>,
    val actions: ImmutableList<ModerationAction>,
    val kickUserAsyncAction: AsyncAction<Unit>,
    val banUserAsyncAction: AsyncAction<Unit>,
    val unbanUserAsyncAction: AsyncAction<Unit>,
    override val eventSink: (RoomMemberModerationEvents) -> Unit,
) : RoomMemberModerationState {

    val canOnlyDisplayProfile = actions.size == 1 && actions.first() is ModerationAction.DisplayProfile
    val canDisplayActions = actions.isNotEmpty() && !canOnlyDisplayProfile
}

