/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members

import io.element.android.features.roomdetails.impl.members.moderation.RoomMembersModerationState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.collections.immutable.ImmutableList

data class RoomMemberListState(
    val isDebugBuild: Boolean,
    val roomMembers: AsyncData<RoomMembers>,
    val searchQuery: String,
    val searchResults: SearchBarResultState<AsyncData<RoomMembers>>,
    val isSearchActive: Boolean,
    val canInvite: Boolean,
    val moderationState: RoomMembersModerationState,
    val eventSink: (RoomMemberListEvents) -> Unit,
)

data class RoomMembers(
    val invited: ImmutableList<RoomMember>,
    val joined: ImmutableList<RoomMember>,
    val banned: ImmutableList<RoomMember>,
)
