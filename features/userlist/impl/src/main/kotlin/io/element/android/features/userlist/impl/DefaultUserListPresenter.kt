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

package io.element.android.features.userlist.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import io.element.android.features.userlist.api.UserListDataSource
import io.element.android.features.userlist.api.UserListDataStore
import io.element.android.features.userlist.api.UserListEvents
import io.element.android.features.userlist.api.UserListPresenter
import io.element.android.features.userlist.api.UserListPresenterArgs
import io.element.android.features.userlist.api.UserListState
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.MatrixPatterns
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.ui.model.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class DefaultUserListPresenter @AssistedInject constructor(
    @Assisted val args: UserListPresenterArgs,
    @Assisted val userListDataSource: UserListDataSource,
    @Assisted val userListDataStore: UserListDataStore,
) : UserListPresenter {

    @AssistedFactory
    @ContributesBinding(SessionScope::class)
    interface DefaultUserListFactory : UserListPresenter.Factory {
        override fun create(
            args: UserListPresenterArgs,
            userListDataSource: UserListDataSource,
            userListDataStore: UserListDataStore,
        ): DefaultUserListPresenter
    }

    @Composable
    override fun present(): UserListState {
        var isSearchActive by rememberSaveable { mutableStateOf(false) }
        val selectedUsers = userListDataStore.selectedUsers().collectAsState(emptyList())
        var searchQuery by rememberSaveable { mutableStateOf("") }
        val searchResults: MutableState<ImmutableList<MatrixUser>> = remember {
            mutableStateOf(persistentListOf())
        }

        fun handleEvents(event: UserListEvents) {
            when (event) {
                is UserListEvents.OnSearchActiveChanged -> isSearchActive = event.active
                is UserListEvents.UpdateSearchQuery -> searchQuery = event.query
                is UserListEvents.AddToSelection -> userListDataStore.selectUser(event.matrixUser)
                is UserListEvents.RemoveFromSelection -> userListDataStore.removeUserFromSelection(event.matrixUser)
            }
        }

        LaunchedEffect(searchQuery) {
            // Clear the search results before performing the search, manually add a fake result with the matrixId, if any
            searchResults.value = if (MatrixPatterns.isUserId(searchQuery)) {
                persistentListOf(MatrixUser(UserId(searchQuery)))
            } else {
                persistentListOf()
            }
            // Perform the search asynchronously
            if (searchQuery.isNotEmpty()) {
                searchResults.value = performSearch(searchQuery)
            }
        }

        return UserListState(
            searchQuery = searchQuery,
            searchResults = searchResults.value,
            selectedUsers = selectedUsers.value.toImmutableList(),
            isSearchActive = isSearchActive,
            selectionMode = args.selectionMode,
            eventSink = ::handleEvents,
        )
    }

    private suspend fun performSearch(query: String): ImmutableList<MatrixUser> {
        val isMatrixId = MatrixPatterns.isUserId(query)
        val results = userListDataSource.search(query).toMutableList()
        if (isMatrixId && results.none { it.id.value == query }) {
            val getProfileResult: MatrixUser? = userListDataSource.getProfile(UserId(query))
            val profile = getProfileResult ?: MatrixUser(UserId(query))
            results.add(0, profile)
        }
        return results.toImmutableList()
    }
}
