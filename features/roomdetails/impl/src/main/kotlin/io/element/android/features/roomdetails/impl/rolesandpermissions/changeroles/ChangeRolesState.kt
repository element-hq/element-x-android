/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions.changeroles

import io.element.android.features.roomdetails.impl.members.PowerLevelRoomMemberComparator
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class ChangeRolesState(
    val isDebugBuild: Boolean,
    val role: RoomMember.Role,
    val query: String?,
    val isSearchActive: Boolean,
    val searchResults: SearchBarResultState<MembersByRole>,
    val selectedUsers: ImmutableList<MatrixUser>,
    val hasPendingChanges: Boolean,
    val exitState: AsyncAction<Unit>,
    val savingState: AsyncAction<Unit>,
    val canChangeMemberRole: (UserId) -> Boolean,
    val eventSink: (ChangeRolesEvent) -> Unit,
)

data class MembersByRole(
    val admins: ImmutableList<RoomMember>,
    val moderators: ImmutableList<RoomMember>,
    val members: ImmutableList<RoomMember>,
) {
    constructor(members: List<RoomMember>) : this(
            admins = members.filter { it.role == RoomMember.Role.ADMIN }.sorted(),
            moderators = members.filter { it.role == RoomMember.Role.MODERATOR }.sorted(),
            members = members.filter { it.role == RoomMember.Role.USER }.sorted(),
    )

    fun isEmpty() = admins.isEmpty() && moderators.isEmpty() && members.isEmpty()
}

private fun Iterable<RoomMember>.sorted(): ImmutableList<RoomMember> {
    return sortedWith(PowerLevelRoomMemberComparator()).toImmutableList()
}
