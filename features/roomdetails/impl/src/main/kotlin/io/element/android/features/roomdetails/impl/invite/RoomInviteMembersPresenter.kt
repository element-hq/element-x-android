/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.roomdetails.impl.invite

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.features.roomdetails.impl.members.RoomMemberListDataSource
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.usersearch.api.UserRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RoomInviteMembersPresenter @Inject constructor(
    private val userRepository: UserRepository,
    private val roomMemberListDataSource: RoomMemberListDataSource,
    private val coroutineDispatchers: CoroutineDispatchers,
) : Presenter<RoomInviteMembersState> {

    @Composable
    override fun present(): RoomInviteMembersState {
        val roomMembers = remember { mutableStateOf<Async<ImmutableList<RoomMember>>>(Async.Loading()) }
        val selectedUsers = remember { mutableStateOf<ImmutableList<MatrixUser>>(persistentListOf()) }
        val searchResults = remember { mutableStateOf<SearchBarResultState<ImmutableList<InvitableUser>>>(SearchBarResultState.NotSearching()) }
        var searchQuery by rememberSaveable { mutableStateOf("") }
        var searchActive by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            fetchMembers(roomMembers)
        }

        LaunchedEffect(searchQuery, roomMembers) {
            performSearch(searchResults, roomMembers, selectedUsers, searchQuery)
        }

        return RoomInviteMembersState(
            canInvite = selectedUsers.value.isNotEmpty(),
            selectedUsers = selectedUsers.value,
            searchQuery = searchQuery,
            isSearchActive = searchActive,
            searchResults = searchResults.value,
            eventSink = {
                when (it) {
                    is RoomInviteMembersEvents.OnSearchActiveChanged -> {
                        searchActive = it.active
                        searchQuery = ""
                    }

                    is RoomInviteMembersEvents.UpdateSearchQuery -> {
                        searchQuery = it.query
                    }

                    is RoomInviteMembersEvents.ToggleUser -> {
                        selectedUsers.toggleUser(it.user)
                        searchResults.toggleUser(it.user)
                    }
                }
            }
        )
    }

    @JvmName("toggleUserInSelectedUsers")
    private fun MutableState<ImmutableList<MatrixUser>>.toggleUser(user: MatrixUser) {
        value = if (value.contains(user)) {
            value.filterNot { it == user }
        } else {
            (value + user)
        }.toImmutableList()
    }

    @JvmName("toggleUserInSearchResults")
    private fun MutableState<SearchBarResultState<ImmutableList<InvitableUser>>>.toggleUser(user: MatrixUser) {
        val existingResults = value
        if (existingResults is SearchBarResultState.Results) {
            value = SearchBarResultState.Results(
                existingResults.results.map { iu ->
                    if (iu.matrixUser == user) {
                        iu.copy(isSelected = !iu.isSelected)
                    } else {
                        iu
                    }
                }.toImmutableList()
            )
        }
    }

    private suspend fun performSearch(
        searchResults: MutableState<SearchBarResultState<ImmutableList<InvitableUser>>>,
        roomMembers: MutableState<Async<ImmutableList<RoomMember>>>,
        selectedUsers: MutableState<ImmutableList<MatrixUser>>,
        searchQuery: String,
    ) = withContext(coroutineDispatchers.io) {
        searchResults.value = SearchBarResultState.NotSearching()

        val joinedMembers = roomMembers.value.dataOrNull().orEmpty()

        userRepository.search(searchQuery).collect {
            searchResults.value = when {
                it.isEmpty() -> SearchBarResultState.NoResults()
                else -> SearchBarResultState.Results(it.map { result ->
                    val existingMembership = joinedMembers.firstOrNull { j -> j.userId == result.matrixUser.userId }?.membership
                    val isJoined = existingMembership == RoomMembershipState.JOIN
                    val isInvited = existingMembership == RoomMembershipState.INVITE
                    InvitableUser(
                        matrixUser = result.matrixUser,
                        isSelected = selectedUsers.value.contains(result.matrixUser) || isJoined || isInvited,
                        isAlreadyJoined = isJoined,
                        isAlreadyInvited = isInvited,
                        isUnresolved = result.isUnresolved,
                    )
                }.toImmutableList())
            }
        }
    }

    private suspend fun fetchMembers(roomMembers: MutableState<Async<ImmutableList<RoomMember>>>) {
        suspend {
            withContext(coroutineDispatchers.io) {
                roomMemberListDataSource.search("").toImmutableList()
            }
        }.runCatchingUpdatingState(roomMembers)
    }
}

