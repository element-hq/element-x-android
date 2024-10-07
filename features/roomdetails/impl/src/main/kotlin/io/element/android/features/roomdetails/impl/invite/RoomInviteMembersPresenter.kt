/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.usersearch.api.UserRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RoomInviteMembersPresenter @Inject constructor(
    private val buildMeta: BuildMeta,
    private val userRepository: UserRepository,
    private val roomMemberListDataSource: RoomMemberListDataSource,
    private val coroutineDispatchers: CoroutineDispatchers,
) : Presenter<RoomInviteMembersState> {
    @Composable
    override fun present(): RoomInviteMembersState {
        val roomMembers = remember { mutableStateOf<AsyncData<ImmutableList<RoomMember>>>(AsyncData.Loading()) }
        val selectedUsers = remember { mutableStateOf<ImmutableList<MatrixUser>>(persistentListOf()) }
        val searchResults = remember { mutableStateOf<SearchBarResultState<ImmutableList<InvitableUser>>>(SearchBarResultState.Initial()) }
        var searchQuery by rememberSaveable { mutableStateOf("") }
        var searchActive by rememberSaveable { mutableStateOf(false) }
        val showSearchLoader = rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            fetchMembers(roomMembers)
        }
        LaunchedEffect(searchQuery, roomMembers) {
            performSearch(
                searchResults = searchResults,
                roomMembers = roomMembers,
                selectedUsers = selectedUsers,
                showSearchLoader = showSearchLoader,
                searchQuery = searchQuery
            )
        }

        return RoomInviteMembersState(
            isDebugBuild = buildMeta.isDebuggable,
            canInvite = selectedUsers.value.isNotEmpty(),
            selectedUsers = selectedUsers.value,
            searchQuery = searchQuery,
            isSearchActive = searchActive,
            searchResults = searchResults.value,
            showSearchLoader = showSearchLoader.value,
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
            value.filterNot { it.userId == user.userId }
        } else {
            value + user
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
        roomMembers: MutableState<AsyncData<ImmutableList<RoomMember>>>,
        selectedUsers: MutableState<ImmutableList<MatrixUser>>,
        showSearchLoader: MutableState<Boolean>,
        searchQuery: String,
    ) = withContext(coroutineDispatchers.io) {
        searchResults.value = SearchBarResultState.Initial()
        showSearchLoader.value = false
        val joinedMembers = roomMembers.value.dataOrNull().orEmpty()

        userRepository.search(searchQuery).onEach { state ->
            showSearchLoader.value = state.isSearching
            searchResults.value = when {
                state.results.isEmpty() && state.isSearching -> SearchBarResultState.Initial()
                state.results.isEmpty() && !state.isSearching -> SearchBarResultState.NoResultsFound()
                else -> SearchBarResultState.Results(state.results.map { result ->
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
        }.launchIn(this)
    }

    private suspend fun fetchMembers(roomMembers: MutableState<AsyncData<ImmutableList<RoomMember>>>) {
        suspend {
            withContext(coroutineDispatchers.io) {
                roomMemberListDataSource.search("").toImmutableList()
            }
        }.runCatchingUpdatingState(roomMembers)
    }
}
