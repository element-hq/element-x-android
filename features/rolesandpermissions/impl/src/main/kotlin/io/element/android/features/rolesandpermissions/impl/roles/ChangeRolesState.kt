/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rolesandpermissions.impl.roles

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

data class ChangeRolesState(
    val role: RoomMember.Role,
    val query: String?,
    val isSearchActive: Boolean,
    val searchResults: SearchBarResultState<MembersByRole>,
    val selectedUsers: ImmutableList<MatrixUser>,
    val hasPendingChanges: Boolean,
    val savingState: AsyncAction<Boolean>,
    val canChangeMemberRole: (UserId) -> Boolean,
    val eventSink: (ChangeRolesEvent) -> Unit,
)

data class MembersByRole(
    val owners: ImmutableList<RoomMember> = persistentListOf(),
    val admins: ImmutableList<RoomMember> = persistentListOf(),
    val moderators: ImmutableList<RoomMember> = persistentListOf(),
    val members: ImmutableList<RoomMember> = persistentListOf(),
) {
    constructor(members: List<RoomMember>, comparator: Comparator<RoomMember>) : this(
        owners = members.filterAndSort(comparator) { it.role is RoomMember.Role.Owner },
        admins = members.filterAndSort(comparator) { it.role == RoomMember.Role.Admin },
        moderators = members.filterAndSort(comparator) { it.role == RoomMember.Role.Moderator },
        members = members.filterAndSort(comparator) { it.role == RoomMember.Role.User },
    )

    fun isEmpty() = owners.isEmpty() && admins.isEmpty() && moderators.isEmpty() && members.isEmpty()
}

private fun Iterable<RoomMember>.filterAndSort(
    comparator: Comparator<RoomMember>,
    predicate: (RoomMember) -> Boolean,
): ImmutableList<RoomMember> {
    return filter(predicate).sortedWith(comparator).toImmutableList()
}
