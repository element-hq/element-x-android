/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invitepeople.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.invitepeople.api.InvitePeopleEvents
import io.element.android.features.invitepeople.api.InvitePeoplePresenter
import io.element.android.features.invitepeople.api.InvitePeopleState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.map
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.filterMembers
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.usersearch.api.UserRepository
import io.element.android.services.apperror.api.AppErrorStateService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AssistedInject
class DefaultInvitePeoplePresenter(
    @Assisted private val joinedRoom: JoinedRoom?,
    @Assisted private val roomId: RoomId,
    private val userRepository: UserRepository,
    private val coroutineDispatchers: CoroutineDispatchers,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
    private val appErrorStateService: AppErrorStateService,
    private val matrixClient: MatrixClient,
) : InvitePeoplePresenter {
    @AssistedFactory
    @ContributesBinding(SessionScope::class)
    interface Factory : InvitePeoplePresenter.Factory {
        override fun create(joinedRoom: JoinedRoom?, roomId: RoomId): DefaultInvitePeoplePresenter
    }

    @Composable
    override fun present(): InvitePeopleState {
        val roomMembers = remember { mutableStateOf<AsyncData<ImmutableList<RoomMember>>>(AsyncData.Loading()) }
        val selectedUsers = remember { mutableStateOf<ImmutableList<MatrixUser>>(persistentListOf()) }
        val searchResults = remember { mutableStateOf<SearchBarResultState<ImmutableList<InvitableUser>>>(SearchBarResultState.Initial()) }
        var searchQuery by rememberSaveable { mutableStateOf("") }
        var searchActive by rememberSaveable { mutableStateOf(false) }
        val showSearchLoader = rememberSaveable { mutableStateOf(false) }
        val sendInvitesAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }

        val room by produceState(if (joinedRoom != null) AsyncData.Success(joinedRoom) else AsyncData.Loading()) {
            if (joinedRoom == null) {
                val result = matrixClient.getJoinedRoom(roomId)
                value = if (result == null) {
                    AsyncData.Failure(Exception("Room not found"))
                } else {
                    AsyncData.Success(result)
                }
            }
        }

        LaunchedEffect(room.isSuccess()) {
            room.dataOrNull()?.let {
                fetchMembers(it, roomMembers)
            }
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

        fun handleEvent(event: InvitePeopleEvents) {
            when (event) {
                is DefaultInvitePeopleEvents.OnSearchActiveChanged -> {
                    searchActive = event.active
                    searchQuery = ""
                }

                is DefaultInvitePeopleEvents.UpdateSearchQuery -> {
                    searchQuery = event.query
                }

                is DefaultInvitePeopleEvents.ToggleUser -> {
                    selectedUsers.toggleUser(event.user)
                    searchResults.toggleUser(event.user)
                }
                is InvitePeopleEvents.SendInvites -> {
                    room.dataOrNull()?.let {
                        sessionCoroutineScope.sendInvites(it, selectedUsers.value, sendInvitesAction)
                    }
                }
                is InvitePeopleEvents.CloseSearch -> {
                    searchActive = false
                    searchQuery = ""
                }
            }
        }

        return DefaultInvitePeopleState(
            room = room.map { },
            canInvite = selectedUsers.value.isNotEmpty() && !sendInvitesAction.value.isLoading(),
            selectedUsers = selectedUsers.value,
            searchQuery = searchQuery,
            isSearchActive = searchActive,
            searchResults = searchResults.value,
            showSearchLoader = showSearchLoader.value,
            sendInvitesAction = sendInvitesAction.value,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.sendInvites(
        room: JoinedRoom,
        selectedUsers: List<MatrixUser>,
        sendInvitesAction: MutableState<AsyncAction<Unit>>,
    ) = launch {
        sendInvitesAction.runUpdatingState {
            val anyInviteFailed = selectedUsers
                .map { room.inviteUserById(it.userId) }
                .any { it.isFailure }

            if (anyInviteFailed) {
                appErrorStateService.showError(
                    titleRes = CommonStrings.common_unable_to_invite_title,
                    bodyRes = CommonStrings.common_unable_to_invite_message,
                )
            }

            Result.success(Unit)
        }
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
                        isSelected = selectedUsers.value.contains(result.matrixUser),
                        isAlreadyJoined = isJoined,
                        isAlreadyInvited = isInvited,
                        isUnresolved = result.isUnresolved,
                    )
                }.toImmutableList())
            }
        }.launchIn(this)
    }

    private suspend fun fetchMembers(
        room: JoinedRoom,
        roomMembers: MutableState<AsyncData<ImmutableList<RoomMember>>>
    ) {
        suspend {
            room.filterMembers("", coroutineDispatchers.io).toImmutableList()
        }.runCatchingUpdatingState(roomMembers)
    }
}
