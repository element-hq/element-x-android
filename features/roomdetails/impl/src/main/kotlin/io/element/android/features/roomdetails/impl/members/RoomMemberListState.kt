/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members

import io.element.android.features.roommembermoderation.api.RoomMemberModerationState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.collections.immutable.ImmutableList

data class RoomMemberListState(
    val roomMembers: AsyncData<RoomMembers>,
    val searchQuery: String,
    val searchResults: SearchBarResultState<AsyncData<RoomMembers>>,
    val isSearchActive: Boolean,
    val canInvite: Boolean,
    val moderationState: RoomMemberModerationState,
    val eventSink: (RoomMemberListEvents) -> Unit,
)

data class RoomMembers(
    val invited: ImmutableList<RoomMemberWithIdentityState>,
    val joined: ImmutableList<RoomMemberWithIdentityState>,
    val banned: ImmutableList<RoomMemberWithIdentityState>,
)

data class RoomMemberWithIdentityState(
    val roomMember: RoomMember,
    val identityState: IdentityState?,
)
