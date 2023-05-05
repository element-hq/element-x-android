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

package io.element.android.features.roomdetails.impl.members.search

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
import io.element.android.features.roomdetails.impl.members.MemberListDataSource
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.MatrixPatterns
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay

class MemberListPresenter @AssistedInject constructor(
    @Assisted val args: MemberListPresenterArgs,
    @Assisted val memberListDataSource: MemberListDataSource,
    @Assisted val memberListDataStore: MemberListDataStore,
) : Presenter<MemberListState> {

    @AssistedFactory
    @ContributesBinding(SessionScope::class)
    interface Factory {
        fun create(
            args: MemberListPresenterArgs,
            memberListDataSource: MemberListDataSource,
            memberListDataStore: MemberListDataStore,
        ): MemberListPresenter
    }

    @Composable
    override fun present(): MemberListState {
        var isSearchActive by rememberSaveable { mutableStateOf(false) }
        val selectedUsers by memberListDataStore.selectedUsers().collectAsState(emptyList())
        var searchQuery by rememberSaveable { mutableStateOf("") }
        var searchResults: UserSearchResultState by remember {
            mutableStateOf(UserSearchResultState.NotSearching)
        }

        LaunchedEffect(searchQuery) {
            // Clear the search results before performing the search, manually add a fake result with the matrixId, if any
            searchResults = if (MatrixPatterns.isUserId(searchQuery)) {
                UserSearchResultState.Results(persistentListOf(MatrixUser(UserId(searchQuery))))
            } else {
                UserSearchResultState.NotSearching
            }

            // Debounce
            delay(args.searchDebouncePeriodMillis)

            // Perform the search asynchronously
            if (searchQuery.length >= args.minimumSearchLength) {
                searchResults = performSearch(searchQuery)
            }
        }

        return MemberListState(
            searchQuery = searchQuery,
            searchResults = searchResults,
            selectedUsers = selectedUsers.toImmutableList(),
            isSearchActive = isSearchActive,
            selectionMode = args.selectionMode,
            eventSink = { event ->
                when (event) {
                    is MemberListEvents.OnSearchActiveChanged -> isSearchActive = event.active
                    is MemberListEvents.UpdateSearchQuery -> searchQuery = event.query
                    is MemberListEvents.AddToSelection -> memberListDataStore.selectUser(event.matrixUser)
                    is MemberListEvents.RemoveFromSelection -> memberListDataStore.removeUserFromSelection(event.matrixUser)
                }
            },
        )
    }

    private suspend fun performSearch(query: String): UserSearchResultState {
        val isMatrixId = MatrixPatterns.isUserId(query)
        val results = memberListDataSource.search(query).toMutableList()
        if (isMatrixId && results.none { it.userId.value == query }) {
            results.add(0, MatrixUser(UserId(query)))
        }
        return if (results.isEmpty()) UserSearchResultState.NoResults else UserSearchResultState.Results(results.toImmutableList())
    }
}
