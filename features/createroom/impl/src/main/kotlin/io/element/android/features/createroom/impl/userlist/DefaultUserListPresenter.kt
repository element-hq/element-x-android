/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.userlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.squareup.anvil.annotations.ContributesBinding
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.room.recent.RecentDirectRoom
import io.element.android.libraries.matrix.api.room.recent.getRecentDirectRooms
import io.element.android.libraries.usersearch.api.UserRepository
import io.element.android.libraries.usersearch.api.UserSearchResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class DefaultUserListPresenter @AssistedInject constructor(
    @Assisted val args: UserListPresenterArgs,
    @Assisted val userRepository: UserRepository,
    @Assisted val userListDataStore: UserListDataStore,
    private val buildMeta: BuildMeta,
    private val matrixClient: MatrixClient,
) : UserListPresenter {
    @AssistedFactory
    @ContributesBinding(SessionScope::class)
    interface DefaultUserListFactory : UserListPresenter.Factory {
        override fun create(
            args: UserListPresenterArgs,
            userRepository: UserRepository,
            userListDataStore: UserListDataStore,
        ): DefaultUserListPresenter
    }

    @Composable
    override fun present(): UserListState {
        var recentDirectRooms by remember { mutableStateOf(emptyList<RecentDirectRoom>()) }
        LaunchedEffect(Unit) {
            recentDirectRooms = matrixClient.getRecentDirectRooms()
        }
        var isSearchActive by rememberSaveable { mutableStateOf(false) }
        val selectedUsers by userListDataStore.selectedUsers().collectAsState(emptyList())
        var searchQuery by rememberSaveable { mutableStateOf("") }
        var searchResults: SearchBarResultState<ImmutableList<UserSearchResult>> by remember {
            mutableStateOf(SearchBarResultState.Initial())
        }
        var showSearchLoader by remember { mutableStateOf(false) }

        LaunchedEffect(searchQuery) {
            searchResults = SearchBarResultState.Initial()
            showSearchLoader = false
            userRepository.search(searchQuery).onEach { state ->
                showSearchLoader = state.isSearching
                searchResults = when {
                    state.results.isEmpty() && state.isSearching -> SearchBarResultState.Initial()
                    state.results.isEmpty() && !state.isSearching -> SearchBarResultState.NoResultsFound()
                    else -> SearchBarResultState.Results(state.results.toImmutableList())
                }
            }.launchIn(this)
        }

        return UserListState(
            isDebugBuild = buildMeta.isDebuggable,
            searchQuery = searchQuery,
            searchResults = searchResults,
            selectedUsers = selectedUsers.toImmutableList(),
            isSearchActive = isSearchActive,
            showSearchLoader = showSearchLoader,
            selectionMode = args.selectionMode,
            recentDirectRooms = recentDirectRooms.toImmutableList(),
            eventSink = { event ->
                when (event) {
                    is UserListEvents.OnSearchActiveChanged -> isSearchActive = event.active
                    is UserListEvents.UpdateSearchQuery -> searchQuery = event.query
                    is UserListEvents.AddToSelection -> userListDataStore.selectUser(event.matrixUser)
                    is UserListEvents.RemoveFromSelection -> userListDataStore.removeUserFromSelection(event.matrixUser)
                }
            },
        )
    }
}
