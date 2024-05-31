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

package io.element.android.features.createroom.impl.userlist

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.components.aMatrixUserList
import io.element.android.libraries.usersearch.api.UserSearchResult
import io.element.android.libraries.usersearch.api.UserSearchResultState
import io.element.android.libraries.usersearch.test.FakeUserRepository
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DefaultUserListPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val userRepository = FakeUserRepository()

    @Test
    fun `present - initial state for single selection`() = runTest {
        val presenter =
            DefaultUserListPresenter(
                UserListPresenterArgs(selectionMode = SelectionMode.Single),
                userRepository,
                UserListDataStore(),
                FakeMatrixClient(),
            )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.searchQuery).isEmpty()
            assertThat(initialState.isMultiSelectionEnabled).isFalse()
            assertThat(initialState.isSearchActive).isFalse()
            assertThat(initialState.selectedUsers).isEmpty()
            assertThat(initialState.searchResults).isInstanceOf(SearchBarResultState.Initial::class.java)
        }
    }

    @Test
    fun `present - initial state for multiple selection`() = runTest {
        val presenter =
            DefaultUserListPresenter(
                UserListPresenterArgs(selectionMode = SelectionMode.Multiple),
                userRepository,
                UserListDataStore(),
                FakeMatrixClient(),
            )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()
            assertThat(initialState.searchQuery).isEmpty()
            assertThat(initialState.isMultiSelectionEnabled).isTrue()
            assertThat(initialState.isSearchActive).isFalse()
            assertThat(initialState.selectedUsers).isEmpty()
            assertThat(initialState.searchResults).isInstanceOf(SearchBarResultState.Initial::class.java)
        }
    }

    @Test
    fun `present - update search query`() = runTest {
        val presenter =
            DefaultUserListPresenter(
                UserListPresenterArgs(selectionMode = SelectionMode.Single),
                userRepository,
                UserListDataStore(),
                FakeMatrixClient(),
            )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()

            initialState.eventSink(UserListEvents.OnSearchActiveChanged(true))
            assertThat(awaitItem().isSearchActive).isTrue()

            val matrixIdQuery = "@name:matrix.org"
            initialState.eventSink(UserListEvents.UpdateSearchQuery(matrixIdQuery))
            assertThat(awaitItem().searchQuery).isEqualTo(matrixIdQuery)
            assertThat(userRepository.providedQuery).isEqualTo(matrixIdQuery)
            skipItems(1)

            val notMatrixIdQuery = "name"
            initialState.eventSink(UserListEvents.UpdateSearchQuery(notMatrixIdQuery))
            assertThat(awaitItem().searchQuery).isEqualTo(notMatrixIdQuery)
            assertThat(userRepository.providedQuery).isEqualTo(notMatrixIdQuery)
            skipItems(1)

            initialState.eventSink(UserListEvents.OnSearchActiveChanged(false))
            assertThat(awaitItem().isSearchActive).isFalse()
        }
    }

    @Test
    fun `present - presents search results`() = runTest {
        val presenter =
            DefaultUserListPresenter(
                UserListPresenterArgs(
                    selectionMode = SelectionMode.Single,
                ),
                userRepository,
                UserListDataStore(),
                FakeMatrixClient(),
            )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()

            initialState.eventSink(UserListEvents.UpdateSearchQuery("alice"))
            assertThat(initialState.searchResults).isInstanceOf(SearchBarResultState.Initial::class.java)
            assertThat(userRepository.providedQuery).isEqualTo("alice")
            skipItems(2)

            // When the user repository emits a result, it's copied to the state
            val result = UserSearchResultState(
                results = listOf(UserSearchResult(aMatrixUser())),
                isSearching = false,
            )
            userRepository.emitState(result)
            awaitItem().also { state ->
                assertThat(state.searchResults).isEqualTo(
                    SearchBarResultState.Results(
                        persistentListOf(UserSearchResult(aMatrixUser()))
                    )
                )
                assertThat(state.showSearchLoader).isFalse()
            }
            // When the user repository emits another result, it replaces the previous value
            val newResult = UserSearchResultState(
                results = aMatrixUserList().map { UserSearchResult(it) },
                isSearching = false,
            )
            userRepository.emitState(newResult)
            awaitItem().also { state ->
                assertThat(state.searchResults).isEqualTo(
                    SearchBarResultState.Results(
                        aMatrixUserList().map { UserSearchResult(it) }
                    )
                )
                assertThat(state.showSearchLoader).isFalse()
            }
        }
    }

    @Test
    fun `present - presents search results when not found`() = runTest {
        val presenter =
            DefaultUserListPresenter(
                UserListPresenterArgs(
                    selectionMode = SelectionMode.Single,
                ),
                userRepository,
                UserListDataStore(),
                FakeMatrixClient(),
            )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()

            initialState.eventSink(UserListEvents.UpdateSearchQuery("alice"))
            assertThat(initialState.searchResults).isInstanceOf(SearchBarResultState.Initial::class.java)
            assertThat(userRepository.providedQuery).isEqualTo("alice")
            skipItems(2)

            // When the results list is empty, the state is set to NoResults
            userRepository.emitState(UserSearchResultState(results = emptyList(), isSearching = false))
            assertThat(awaitItem().searchResults).isInstanceOf(SearchBarResultState.NoResultsFound::class.java)
        }
    }

    @Test
    fun `present - select a user`() = runTest {
        val presenter =
            DefaultUserListPresenter(
                UserListPresenterArgs(selectionMode = SelectionMode.Single),
                userRepository,
                UserListDataStore(),
                FakeMatrixClient(),
            )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialState = awaitItem()

            val userA = aMatrixUser("@userA:domain", "A")
            val userB = aMatrixUser("@userB:domain", "B")
            val userABis = aMatrixUser("@userA:domain", "A")
            val userC = aMatrixUser("@userC:domain", "C")

            initialState.eventSink(UserListEvents.AddToSelection(userA))
            assertThat(awaitItem().selectedUsers).containsExactly(userA)

            initialState.eventSink(UserListEvents.AddToSelection(userB))
            assertThat(awaitItem().selectedUsers).containsExactly(userA, userB)

            initialState.eventSink(UserListEvents.AddToSelection(userABis))
            initialState.eventSink(UserListEvents.AddToSelection(userC))
            // duplicated users should be ignored
            assertThat(awaitItem().selectedUsers).containsExactly(userA, userB, userC)

            initialState.eventSink(UserListEvents.RemoveFromSelection(userB))
            assertThat(awaitItem().selectedUsers).containsExactly(userA, userC)
            initialState.eventSink(UserListEvents.RemoveFromSelection(userA))
            assertThat(awaitItem().selectedUsers).containsExactly(userC)
            initialState.eventSink(UserListEvents.RemoveFromSelection(userC))
            assertThat(awaitItem().selectedUsers).isEmpty()
        }
    }
}
