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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.roomdetails.impl.members.PowerLevelRoomMemberComparator
import io.element.android.features.roomdetails.impl.members.RoomMemberListDataSource
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.powerlevels.UserRoleChange
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChangeRolesPresenter @AssistedInject constructor(
    @Assisted private val role: RoomMember.Role,
    private val room: MatrixRoom,
    private val dispatchers: CoroutineDispatchers,
) : Presenter<ChangeRolesState> {
    @AssistedFactory
    interface Factory {
        fun create(role: RoomMember.Role): ChangeRolesPresenter
    }

    @Composable
    override fun present(): ChangeRolesState {
        val coroutineScope = rememberCoroutineScope()
        val dataSource = remember { RoomMemberListDataSource(room, dispatchers) }
        var query by rememberSaveable { mutableStateOf<String?>(null) }
        var searchActive by rememberSaveable { mutableStateOf(false) }
        var searchResults by remember {
            mutableStateOf<SearchBarResultState<ImmutableList<RoomMember>>>(SearchBarResultState.Initial())
        }
        val selectedUsers = remember {
            mutableStateOf<ImmutableList<MatrixUser>>(persistentListOf())
        }
        val exitState: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        val saveState: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        val usersWithRole = produceState(initialValue = persistentListOf()) {
            room.usersWithRole(role)
                .map { members -> members.map { it.toMatrixUser() } }
                .onEach { users ->
                    val previous: PersistentList<MatrixUser> = value
                    value = users.toPersistentList()
                    // Users who were selected but didn't have the role, so their role change was pending
                    val toAdd = selectedUsers.value.filter { user -> users.none { it.userId == user.userId } && previous.none { it.userId == user.userId } }
                    // Users who no longer have the role
                    val toRemove = previous.filter { user -> users.none { it.userId == user.userId } }
                    selectedUsers.value = (users + toAdd - toRemove).toImmutableList()
                }
                .launchIn(this)
        }

        val roomMemberState by room.membersStateFlow.collectAsState()

        // Update search results for every query change
        LaunchedEffect(query, roomMemberState) {
            val results = dataSource
                .search(query.orEmpty())
                .sorted()

            searchResults = if (results.isEmpty()) {
                SearchBarResultState.NoResultsFound()
            } else {
                SearchBarResultState.Results(results)
            }
        }

        val hasPendingChanges = usersWithRole.value != selectedUsers.value

        val roomInfo by room.roomInfoFlow.collectAsState(initial = null)
        fun canChangeMemberRole(userId: UserId): Boolean {
            // An admin can't remove or demote another admin
            val powerLevel = roomInfo?.userPowerLevels?.get(userId) ?: 0L
            return RoomMember.Role.forPowerLevel(powerLevel) != RoomMember.Role.ADMIN
        }

        fun handleEvent(event: ChangeRolesEvent) {
            when (event) {
                is ChangeRolesEvent.ToggleSearchActive -> {
                    searchActive = !searchActive
                }
                is ChangeRolesEvent.QueryChanged -> {
                    query = event.query
                }
                is ChangeRolesEvent.UserSelectionToggled -> {
                    val newList = selectedUsers.value.toMutableList()
                    val index = newList.indexOfFirst { it.userId == event.roomMember.userId }
                    if (index >= 0) {
                        newList.removeAt(index)
                    } else {
                        newList.add(event.roomMember.toMatrixUser())
                    }
                    selectedUsers.value = newList.toImmutableList()
                }
                is ChangeRolesEvent.Save -> {
                    if (role == RoomMember.Role.ADMIN && selectedUsers != usersWithRole && !saveState.value.isConfirming()) {
                        // Confirm adding admin
                        saveState.value = AsyncAction.Confirming
                    } else if (!saveState.value.isLoading()) {
                        coroutineScope.save(usersWithRole.value, selectedUsers, saveState)
                    }
                }
                is ChangeRolesEvent.ClearError -> {
                    saveState.value = AsyncAction.Uninitialized
                }
                is ChangeRolesEvent.Exit -> {
                    exitState.value = if (exitState.value.isUninitialized() && hasPendingChanges) {
                        // Has pending changes, confirm exit
                        AsyncAction.Confirming
                    } else {
                        // No pending changes, exit immediately
                        AsyncAction.Success(Unit)
                    }
                }
                is ChangeRolesEvent.CancelExit -> {
                    exitState.value = AsyncAction.Uninitialized
                }
                is ChangeRolesEvent.CancelSave -> {
                    saveState.value = AsyncAction.Uninitialized
                }
            }
        }
        return ChangeRolesState(
            role = role,
            query = query,
            isSearchActive = searchActive,
            searchResults = searchResults,
            selectedUsers = selectedUsers.value,
            hasPendingChanges = hasPendingChanges,
            exitState = exitState.value,
            savingState = saveState.value,
            canChangeMemberRole = ::canChangeMemberRole,
            eventSink = ::handleEvent,
        )
    }

    private fun Iterable<RoomMember>.sorted(): ImmutableList<RoomMember> {
        return sortedWith(PowerLevelRoomMemberComparator()).toImmutableList()
    }

    private fun RoomMember.toMatrixUser() = MatrixUser(
        userId = userId,
        displayName = displayName,
        avatarUrl = avatarUrl,
    )

    private fun CoroutineScope.save(
        usersWithRole: ImmutableList<MatrixUser>,
        selectedUsers: MutableState<ImmutableList<MatrixUser>>,
        saveState: MutableState<AsyncAction<Unit>>,
    ) = launch {
        saveState.value = AsyncAction.Loading

        val toAdd = selectedUsers.value - usersWithRole
        val toRemove = usersWithRole - selectedUsers.value

        val changes: List<UserRoleChange> = buildList {
            for (selectedUser in toAdd) {
                add(UserRoleChange(selectedUser.userId, role))
            }
            for (selectedUser in toRemove) {
                add(UserRoleChange(selectedUser.userId, RoomMember.Role.USER))
            }
        }

        room.updateUsersRoles(changes)
            .onFailure {
                saveState.value = AsyncAction.Failure(it)
            }
            .onSuccess {
                saveState.value = AsyncAction.Success(Unit)
            }
    }
}
