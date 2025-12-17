/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rolesandpermissions.impl.roles

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import io.element.android.libraries.matrix.ui.room.PowerLevelRoomMemberComparator
import io.element.android.libraries.previewutils.room.aRoomMember
import io.element.android.libraries.previewutils.room.aRoomMemberList
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class ChangeRolesStateProvider : PreviewParameterProvider<ChangeRolesState> {
    override val values: Sequence<ChangeRolesState>
        get() = sequenceOf(
            aChangeRolesState(),
            aChangeRolesStateWithSelectedUsers().copy(role = RoomMember.Role.Moderator),
            aChangeRolesStateWithSelectedUsers().copy(hasPendingChanges = false),
            aChangeRolesStateWithSelectedUsers(),
            aChangeRolesStateWithSelectedUsers().copy(
                selectedUsers = aMatrixUserList().take(2).toImmutableList(),
            ),
            aChangeRolesStateWithSelectedUsers().copy(
                query = "Alice",
                isSearchActive = true,
                searchResults = SearchBarResultState.Results(
                    MembersByRole(
                        members = aRoomMemberList().take(1),
                        comparator = PowerLevelRoomMemberComparator(),
                    )
                ),
                selectedUsers = aMatrixUserList().take(1).toImmutableList(),
            ),
            aChangeRolesStateWithSelectedUsers().copy(savingState = AsyncAction.ConfirmingCancellation),
            aChangeRolesStateWithSelectedUsers().copy(savingState = ConfirmingModifyingAdmins),
            aChangeRolesStateWithSelectedUsers().copy(savingState = AsyncAction.Loading),
            aChangeRolesStateWithSelectedUsers().copy(savingState = AsyncAction.Success(true)),
            aChangeRolesStateWithSelectedUsers().copy(savingState = AsyncAction.Failure(Exception("boom"))),
            aChangeRolesStateWithOwners(role = RoomMember.Role.Admin),
            aChangeRolesStateWithOwners(role = RoomMember.Role.Owner(isCreator = false)),
            aChangeRolesStateWithOwners(role = RoomMember.Role.Owner(isCreator = false))
                .copy(savingState = ConfirmingModifyingOwners),
        )
}

internal fun aChangeRolesState(
    role: RoomMember.Role = RoomMember.Role.Admin,
    query: String? = null,
    isSearchActive: Boolean = false,
    searchResults: SearchBarResultState<MembersByRole> = SearchBarResultState.NoResultsFound(),
    selectedUsers: ImmutableList<MatrixUser> = persistentListOf(),
    hasPendingChanges: Boolean = false,
    savingState: AsyncAction<Boolean> = AsyncAction.Uninitialized,
    canRemoveMember: (UserId) -> Boolean = { true },
    eventSink: (ChangeRolesEvent) -> Unit = {},
) = ChangeRolesState(
    role = role,
    query = query,
    isSearchActive = isSearchActive,
    searchResults = searchResults,
    selectedUsers = selectedUsers,
    hasPendingChanges = hasPendingChanges,
    savingState = savingState,
    canChangeMemberRole = canRemoveMember,
    eventSink = eventSink,
)

internal fun aChangeRolesStateWithSelectedUsers() = aChangeRolesState(
    selectedUsers = aMatrixUserList().toImmutableList(),
    searchResults = SearchBarResultState.Results(
        MembersByRole(
            members = aRoomMemberList().mapIndexed { index, roomMember ->
                if (index % 2 == 0) {
                    roomMember.copy(membership = RoomMembershipState.INVITE)
                } else {
                    roomMember
                }
            },
            comparator = PowerLevelRoomMemberComparator(),
        )
    ),
    hasPendingChanges = true,
    canRemoveMember = { it != UserId("@alice:server.org") },
)

internal fun aChangeRolesStateWithOwners(
    role: RoomMember.Role = RoomMember.Role.Admin,
    selectedUsers: List<MatrixUser> = listOf(
        aMatrixUser(id = "@alice:server.org", displayName = "Alice"),
        aMatrixUser(id = "@bob:server.org", displayName = "Bob"),
        aMatrixUser(id = "@carol:server.org", displayName = "Carol"),
    ),
) = aChangeRolesState(
    role = role,
    searchResults = SearchBarResultState.Results(
        MembersByRole(
            members = persistentListOf(
                aRoomMember(
                    userId = UserId("@alice:server.org"),
                    displayName = "Alice",
                    role = RoomMember.Role.Owner(isCreator = true),
                ),
                aRoomMember(
                    userId = UserId("@bob:server.org"),
                    displayName = "Bob",
                    role = RoomMember.Role.Owner(isCreator = false),
                ),
                aRoomMember(
                    userId = UserId("@carol:server.org"),
                    displayName = "Carol",
                    role = RoomMember.Role.Admin,
                ),
                aRoomMember(
                    userId = UserId("@david:server.org"),
                    displayName = "David",
                    role = RoomMember.Role.User,
                ),
            ),
            comparator = PowerLevelRoomMemberComparator(),
        ),
    ),
    canRemoveMember = { userId ->
        when (userId) {
            UserId("@alice:server.org") -> false // Owner - creator
            UserId("@bob:server.org") -> false // Owner - super admin
            UserId("@carol:server.org") -> true // Admin
            UserId("@david:server.org") -> true // User
            else -> false
        }
    },
    selectedUsers = selectedUsers.toImmutableList(),
)
