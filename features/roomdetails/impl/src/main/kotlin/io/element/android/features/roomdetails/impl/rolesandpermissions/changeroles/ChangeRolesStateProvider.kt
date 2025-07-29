/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions.changeroles

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.roomdetails.impl.members.aRoomMember
import io.element.android.features.roomdetails.impl.members.aRoomMemberList
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
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
                searchResults = SearchBarResultState.Results(MembersByRole(aRoomMemberList().take(1).toImmutableList())),
                selectedUsers = aMatrixUserList().take(1).toImmutableList(),
            ),
            aChangeRolesStateWithSelectedUsers().copy(exitState = AsyncAction.ConfirmingNoParams),
            aChangeRolesStateWithSelectedUsers().copy(savingState = AsyncAction.ConfirmingNoParams),
            aChangeRolesStateWithSelectedUsers().copy(savingState = AsyncAction.Loading),
            aChangeRolesStateWithSelectedUsers().copy(savingState = AsyncAction.Success(Unit)),
            aChangeRolesStateWithSelectedUsers().copy(savingState = AsyncAction.Failure(Exception("boom"))),
            aChangeRolesStateWithOwners(),
        )
}

internal fun aChangeRolesState(
    role: RoomMember.Role = RoomMember.Role.Admin,
    query: String? = null,
    isSearchActive: Boolean = false,
    searchResults: SearchBarResultState<MembersByRole> = SearchBarResultState.NoResultsFound(),
    selectedUsers: ImmutableList<MatrixUser> = persistentListOf(),
    hasPendingChanges: Boolean = false,
    exitState: AsyncAction<Unit> = AsyncAction.Uninitialized,
    savingState: AsyncAction<Unit> = AsyncAction.Uninitialized,
    canRemoveMember: (UserId) -> Boolean = { true },
    eventSink: (ChangeRolesEvent) -> Unit = {},
) = ChangeRolesState(
    role = role,
    query = query,
    isSearchActive = isSearchActive,
    searchResults = searchResults,
    selectedUsers = selectedUsers,
    hasPendingChanges = hasPendingChanges,
    exitState = exitState,
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
            }
        )
    ),
    hasPendingChanges = true,
    canRemoveMember = { it != UserId("@alice:server.org") },
)

internal fun aChangeRolesStateWithOwners() = aChangeRolesState(
    role = RoomMember.Role.Admin,
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
            )
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
    selectedUsers = persistentListOf(
        aMatrixUser(id = "@alice:server.org", displayName = "Alice"),
        aMatrixUser(id = "@bob:server.org", displayName = "Bob"),
        aMatrixUser(id = "@carol:server.org", displayName = "Carol"),
    )
)
