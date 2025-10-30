/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.changeroommemberroles.impl

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
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.RoomModeration
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.powerlevels.UserRoleChange
import io.element.android.libraries.matrix.api.room.powerlevels.usersWithRole
import io.element.android.libraries.matrix.api.room.toMatrixUser
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.model.roleOf
import io.element.android.libraries.matrix.ui.room.PowerLevelRoomMemberComparator
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AssistedInject
class ChangeRolesPresenter(
    @Assisted private val role: RoomMember.Role,
    private val room: JoinedRoom,
    private val dispatchers: CoroutineDispatchers,
    private val analyticsService: AnalyticsService,
) : Presenter<ChangeRolesState> {
    @AssistedFactory
    fun interface Factory {
        fun create(role: RoomMember.Role): ChangeRolesPresenter
    }

    @Composable
    override fun present(): ChangeRolesState {
        val coroutineScope = rememberCoroutineScope()
        val dataSource = remember { RoomMemberListDataSource(room, dispatchers) }
        var query by rememberSaveable { mutableStateOf<String?>(null) }
        var searchActive by rememberSaveable { mutableStateOf(false) }
        var searchResults by remember {
            mutableStateOf<SearchBarResultState<MembersByRole>>(SearchBarResultState.Initial())
        }
        val selectedUsers = remember {
            mutableStateOf<ImmutableList<MatrixUser>>(persistentListOf())
        }
        val saveState: MutableState<AsyncAction<Boolean>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        val usersWithRole = produceState<ImmutableList<MatrixUser>>(initialValue = persistentListOf()) {
            room.usersWithRole(role).map { members -> members.map { it.toMatrixUser() } }
                .onEach { users ->
                    val previous = value
                    value = users.toImmutableList()
                    // Users who were selected but didn't have the role, so their role change was pending
                    val toAdd = selectedUsers.value.filter { user -> users.none { it.userId == user.userId } && previous.none { it.userId == user.userId } }
                    // Users who no longer have the role
                    val toRemove = previous.filter { user -> users.none { it.userId == user.userId } }.toSet()
                    selectedUsers.value = (users + toAdd - toRemove).toImmutableList()
                }
                .launchIn(this)
        }

        val roomMemberState by room.membersStateFlow.collectAsState()

        // Update search results for every query change
        LaunchedEffect(query, roomMemberState) {
            val results = dataSource
                .search(query.orEmpty())
                .groupedByRole()

            searchResults = if (results.isEmpty()) {
                SearchBarResultState.NoResultsFound()
            } else {
                SearchBarResultState.Results(results)
            }
        }

        val hasPendingChanges = usersWithRole.value != selectedUsers.value

        val roomInfo by room.roomInfoFlow.collectAsState()
        fun canChangeMemberRole(userId: UserId): Boolean {
            // This is used to group the
            val currentUserRole = roomInfo.roleOf(room.sessionId)
            val otherUserRole = roomInfo.roleOf(userId)
            return currentUserRole.powerLevel > otherUserRole.powerLevel
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
                    val index = newList.indexOfFirst { it.userId == event.matrixUser.userId }
                    if (index >= 0) {
                        newList.removeAt(index)
                    } else {
                        newList.add(event.matrixUser)
                    }
                    selectedUsers.value = newList.toImmutableList()
                }
                is ChangeRolesEvent.Save -> {
                    val currentUserIsAdmin = roomInfo.roleOf(room.sessionId) == RoomMember.Role.Admin
                    val isModifyingAdmins = role == RoomMember.Role.Admin
                    val hasChanges = selectedUsers != usersWithRole
                    val isConfirming = saveState.value.isConfirming()
                    val modifyingOwners = role is RoomMember.Role.Owner

                    val needsConfirmation = (modifyingOwners || currentUserIsAdmin && isModifyingAdmins) && hasChanges && !isConfirming

                    when {
                        needsConfirmation -> {
                            // Confirm modifying users
                            saveState.value = AsyncAction.ConfirmingNoParams
                        }
                        !saveState.value.isLoading() -> {
                            coroutineScope.save(usersWithRole.value, selectedUsers, saveState)
                        }
                    }
                }
                is ChangeRolesEvent.Exit -> {
                    saveState.value = if (saveState.value.isUninitialized() && hasPendingChanges) {
                        // Has pending changes, confirm exit
                        AsyncAction.ConfirmingCancellation
                    } else {
                        // No pending changes, exit immediately
                        AsyncAction.Success(false)
                    }
                }
                is ChangeRolesEvent.CloseDialog -> {
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
            savingState = saveState.value,
            canChangeMemberRole = ::canChangeMemberRole,
            eventSink = ::handleEvent,
        )
    }

    private fun List<RoomMember>.groupedByRole(): MembersByRole {
        val groupedMembers = MembersByRole(this)
        return MembersByRole(
            owners = groupedMembers.owners.sorted(),
            admins = groupedMembers.admins.sorted(),
            moderators = groupedMembers.moderators.sorted(),
            members = groupedMembers.members.sorted(),
        )
    }

    private fun Iterable<RoomMember>.sorted(): ImmutableList<RoomMember> {
        return sortedWith(PowerLevelRoomMemberComparator()).toImmutableList()
    }

    private fun CoroutineScope.save(
        usersWithRole: ImmutableList<MatrixUser>,
        selectedUsers: MutableState<ImmutableList<MatrixUser>>,
        saveState: MutableState<AsyncAction<Boolean>>,
    ) = launch {
        saveState.value = AsyncAction.Loading

        val toAdd = selectedUsers.value - usersWithRole
        val toRemove = usersWithRole - selectedUsers.value

        val changes: List<UserRoleChange> = buildList {
            for (selectedUser in toAdd) {
                analyticsService.capture(RoomModeration(RoomModeration.Action.ChangeMemberRole, role.toAnalyticsMemberRole()))
                add(UserRoleChange(selectedUser.userId, role))
            }
            for (selectedUser in toRemove) {
                analyticsService.capture(RoomModeration(RoomModeration.Action.ChangeMemberRole, RoomModeration.Role.User))
                add(UserRoleChange(selectedUser.userId, RoomMember.Role.User))
            }
        }

        room.updateUsersRoles(changes)
            .onFailure {
                saveState.value = AsyncAction.Failure(it)
            }
            .onSuccess {
                saveState.value = AsyncAction.Success(true)
                // Asynchronously reload the room members
                launch { room.updateMembers() }
            }
    }
}

internal fun RoomMember.Role.toAnalyticsMemberRole(): RoomModeration.Role = when (this) {
    is RoomMember.Role.Owner -> RoomModeration.Role.Administrator // TODO - distinguish creator from admin
    RoomMember.Role.Admin -> RoomModeration.Role.Administrator
    RoomMember.Role.Moderator -> RoomModeration.Role.Moderator
    RoomMember.Role.User -> RoomModeration.Role.User
}
