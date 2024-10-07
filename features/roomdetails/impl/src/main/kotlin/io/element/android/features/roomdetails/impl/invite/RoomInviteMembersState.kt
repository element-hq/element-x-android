/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.invite

import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList

data class RoomInviteMembersState(
    val isDebugBuild: Boolean,
    val canInvite: Boolean,
    val searchQuery: String,
    val showSearchLoader: Boolean,
    val searchResults: SearchBarResultState<ImmutableList<InvitableUser>>,
    val selectedUsers: ImmutableList<MatrixUser>,
    val isSearchActive: Boolean,
    val eventSink: (RoomInviteMembersEvents) -> Unit,
)

data class InvitableUser(
    val matrixUser: MatrixUser,
    val isSelected: Boolean = false,
    val isAlreadyJoined: Boolean = false,
    val isAlreadyInvited: Boolean = false,
    val isUnresolved: Boolean = false,
)
