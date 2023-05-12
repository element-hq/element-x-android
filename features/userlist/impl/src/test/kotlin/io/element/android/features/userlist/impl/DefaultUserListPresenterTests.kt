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

import androidx.compose.foundation.lazy.LazyListState
import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.userlist.api.SelectionMode
import io.element.android.features.userlist.api.UserListDataStore
import io.element.android.features.userlist.api.UserListEvents
import io.element.android.features.userlist.api.UserListPresenterArgs
import io.element.android.features.userlist.api.UserSearchResultState
import io.element.android.features.userlist.test.FakeUserListDataSource
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.mockk.coJustRun
import io.mockk.mockkConstructor
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultUserListPresenterTests {

    private val userListDataSource = FakeUserListDataSource()

    @Test
    fun `present - initial state for single selection`() = runTest {
        val presenter = DefaultUserListPresenter(
            UserListPresenterArgs(selectionMode = SelectionMode.Single),
            userListDataSource,
            UserListDataStore(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.searchQuery).isEmpty()
            assertThat(initialState.isMultiSelectionEnabled).isFalse()
            assertThat(initialState.isSearchActive).isFalse()
            assertThat(initialState.selectedUsers).isEmpty()
            assertThat(initialState.searchResults).isEqualTo(UserSearchResultState.NotSearching)
        }
    }

    @Test
    fun `present - initial state for multiple selection`() = runTest {
        val presenter = DefaultUserListPresenter(
            UserListPresenterArgs(selectionMode = SelectionMode.Multiple),
            userListDataSource,
            UserListDataStore(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.searchQuery).isEmpty()
            assertThat(initialState.isMultiSelectionEnabled).isTrue()
            assertThat(initialState.isSearchActive).isFalse()
            assertThat(initialState.selectedUsers).isEmpty()
            assertThat(initialState.searchResults).isEqualTo(UserSearchResultState.NotSearching)
        }
    }

    @Test
    fun `present - update search query`() = runTest {
        val presenter = DefaultUserListPresenter(
            UserListPresenterArgs(selectionMode = SelectionMode.Single),
            userListDataSource,
            UserListDataStore(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            initialState.eventSink(UserListEvents.OnSearchActiveChanged(true))
            assertThat(awaitItem().isSearchActive).isTrue()

            val matrixIdQuery = "@name:matrix.org"
            initialState.eventSink(UserListEvents.UpdateSearchQuery(matrixIdQuery))
            assertThat(awaitItem().searchQuery).isEqualTo(matrixIdQuery)
            assertThat(awaitItem().searchResults).isEqualTo(UserSearchResultState.Results(persistentListOf(MatrixUser(UserId(matrixIdQuery)))))

            val notMatrixIdQuery = "name"
            initialState.eventSink(UserListEvents.UpdateSearchQuery(notMatrixIdQuery))
            assertThat(awaitItem().searchQuery).isEqualTo(notMatrixIdQuery)
            assertThat(awaitItem().searchResults).isEqualTo(UserSearchResultState.NoResults)

            initialState.eventSink(UserListEvents.OnSearchActiveChanged(false))
            assertThat(awaitItem().isSearchActive).isFalse()
        }
    }

    @Test
    fun `present - searches when minimum length exceeded`() = runTest {
        val presenter = DefaultUserListPresenter(
            UserListPresenterArgs(selectionMode = SelectionMode.Single, minimumSearchLength = 3),
            userListDataSource,
            UserListDataStore(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            // When the search term is too short, nothing happens
            initialState.eventSink(UserListEvents.UpdateSearchQuery("al"))
            assertThat(awaitItem().searchResults).isEqualTo(UserSearchResultState.NotSearching)

            // When it reaches the minimum length, a search is performed asynchronously
            userListDataSource.givenSearchResult(listOf(aMatrixUser()))
            initialState.eventSink(UserListEvents.UpdateSearchQuery("alice"))
            assertThat(awaitItem().searchResults).isEqualTo(UserSearchResultState.NotSearching)
            assertThat(awaitItem().searchResults).isEqualTo(UserSearchResultState.Results(persistentListOf(aMatrixUser())))
        }
    }

    @Test
    fun `present - select a user`() = runTest {
        mockkConstructor(LazyListState::class)
        coJustRun { anyConstructed<LazyListState>().scrollToItem(index = any()) }

        val presenter = DefaultUserListPresenter(
            UserListPresenterArgs(selectionMode = SelectionMode.Single),
            userListDataSource,
            UserListDataStore(),
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            val userA = aMatrixUser("@userA:domain", "A")
            val userB = aMatrixUser("@userB:domain", "B")
            val userABis = aMatrixUser("@userA:domain", "A")
            val userC = aMatrixUser("@userC:domain", "C")

            initialState.eventSink(UserListEvents.AddToSelection(userA))
            assertThat(awaitItem().selectedUsers).containsExactly(userA)

            initialState.eventSink(UserListEvents.AddToSelection(userB))
            // the last added user should be presented first
            assertThat(awaitItem().selectedUsers).containsExactly(userB, userA)

            initialState.eventSink(UserListEvents.AddToSelection(userABis))
            initialState.eventSink(UserListEvents.AddToSelection(userC))
            // duplicated users should be ignored
            assertThat(awaitItem().selectedUsers).containsExactly(userC, userB, userA)

            initialState.eventSink(UserListEvents.RemoveFromSelection(userB))
            assertThat(awaitItem().selectedUsers).containsExactly(userC, userA)
            initialState.eventSink(UserListEvents.RemoveFromSelection(userA))
            assertThat(awaitItem().selectedUsers).containsExactly(userC)
            initialState.eventSink(UserListEvents.RemoveFromSelection(userC))
            assertThat(awaitItem().selectedUsers).isEmpty()
        }
    }
}
