/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions.changeroles

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.roomdetails.impl.members.aRoomMemberList
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class ChangeRolesStateProvider : PreviewParameterProvider<ChangeRolesState> {
    override val values: Sequence<ChangeRolesState>
        get() = sequenceOf(
            aChangeRolesState(),
            aChangeRolesState(role = RoomMember.Role.MODERATOR),
            aChangeRolesStateWithSelectedUsers().copy(hasPendingChanges = false),
            aChangeRolesStateWithSelectedUsers(),
            aChangeRolesStateWithSelectedUsers().copy(
                selectedUsers = aMatrixUserList().take(2).toImmutableList(),
            ),
            aChangeRolesStateWithSelectedUsers().copy(
                query = "Alice",
                isSearchActive = true,
                searchResults = SearchBarResultState.Results(aRoomMemberList().take(1).toImmutableList()),
                selectedUsers = aMatrixUserList().take(1).toImmutableList(),
            ),
            aChangeRolesStateWithSelectedUsers().copy(exitState = AsyncAction.Confirming),
            aChangeRolesStateWithSelectedUsers().copy(savingState = AsyncAction.Confirming),
            aChangeRolesStateWithSelectedUsers().copy(savingState = AsyncAction.Loading),
            aChangeRolesStateWithSelectedUsers().copy(savingState = AsyncAction.Success(Unit)),
            aChangeRolesStateWithSelectedUsers().copy(savingState = AsyncAction.Failure(Exception("boom"))),
        )
}

internal fun aChangeRolesState(
    role: RoomMember.Role = RoomMember.Role.ADMIN,
    query: String? = null,
    isSearchActive: Boolean = false,
    searchResults: SearchBarResultState<MembersByRole> = SearchBarResultState.NoResultsFound(),
    selectedUsers: ImmutableList<MatrixUser> = persistentListOf(),
    hasPendingChanges: Boolean = false,
    exitState: AsyncAction<Unit> = AsyncAction.Uninitialized,
    savingState: AsyncAction<Unit> = AsyncAction.Uninitialized,
    canRemoveMember: (UserId) -> Boolean = { true },
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
    eventSink = {},
)

internal fun aChangeRolesStateWithSelectedUsers() = aChangeRolesState(
    selectedUsers = aMatrixUserList().toImmutableList(),
    searchResults = SearchBarResultState.Results(aRoomMemberList().toImmutableList()),
    hasPendingChanges = true,
    canRemoveMember = { it != UserId("@alice:server.org") },
)
