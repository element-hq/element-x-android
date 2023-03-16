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

package io.element.android.features.selectusers.impl

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.selectusers.api.SelectUsersEvents
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.libraries.matrix.ui.model.MatrixUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultSelectSingleUserPresenterTests {

    @Test
    fun `present - initial state for single selection`() = runTest {
        val presenter = DefaultSelectUsersPresenter(isMultiSelectionEnabled = false)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.searchQuery).isEmpty()
            assertThat(initialState.isMultiSelectionEnabled).isFalse()
            assertThat(initialState.isSearchActive).isFalse()
            assertThat(initialState.selectedUsers).isEmpty()
            assertThat(initialState.searchResults).isEmpty()
        }
    }

    @Test
    fun `present - initial state for multiple selection`() = runTest {
        val presenter = DefaultSelectUsersPresenter(isMultiSelectionEnabled = true)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.searchQuery).isEmpty()
            assertThat(initialState.isMultiSelectionEnabled).isTrue()
            assertThat(initialState.isSearchActive).isFalse()
            assertThat(initialState.selectedUsers).isEmpty()
            assertThat(initialState.searchResults).isEmpty()
        }
    }

    @Test
    fun `present - update search query`() = runTest {
        val presenter = DefaultSelectUsersPresenter(isMultiSelectionEnabled = false)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            initialState.eventSink(SelectUsersEvents.OnSearchActiveChanged(true))
            assertThat(awaitItem().isSearchActive).isTrue()

            val matrixIdQuery = "@name:matrix.org"
            initialState.eventSink(SelectUsersEvents.UpdateSearchQuery(matrixIdQuery))
            assertThat(awaitItem().searchQuery).isEqualTo(matrixIdQuery)
            assertThat(awaitItem().searchResults).containsExactly(MatrixUser(UserId(matrixIdQuery)))

            val notMatrixIdQuery = "name"
            initialState.eventSink(SelectUsersEvents.UpdateSearchQuery(notMatrixIdQuery))
            assertThat(awaitItem().searchQuery).isEqualTo(notMatrixIdQuery)
            assertThat(awaitItem().searchResults).isEmpty()

            initialState.eventSink(SelectUsersEvents.OnSearchActiveChanged(false))
            assertThat(awaitItem().isSearchActive).isFalse()
        }
    }

    @Test
    fun `present - select a user`() = runTest {
        val presenter = DefaultSelectUsersPresenter(isMultiSelectionEnabled = false)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()

            val userA = aMatrixUser("userA", "A")
            val userB = aMatrixUser("userB", "B")
            initialState.eventSink(SelectUsersEvents.AddToSelection(userA))
            val selectionAfterAddingA = awaitItem().selectedUsers
            assertThat(selectionAfterAddingA).hasSize(1)
            assertThat(selectionAfterAddingA).contains(userA)

            initialState.eventSink(SelectUsersEvents.AddToSelection(userB))
            val selectionAfterAddingB = awaitItem().selectedUsers
            assertThat(selectionAfterAddingB).hasSize(2)
            assertThat(selectionAfterAddingB).contains(userA)
            assertThat(selectionAfterAddingB).contains(userB)
            // the last added user should be presented first
            assertThat(selectionAfterAddingB.first()).isEqualTo(userB)

            initialState.eventSink(SelectUsersEvents.RemoveFromSelection(userB))
            val selectionAfterRemovingB = awaitItem().selectedUsers
            assertThat(selectionAfterRemovingB).hasSize(1)
            assertThat(selectionAfterRemovingB).contains(userA)

            initialState.eventSink(SelectUsersEvents.RemoveFromSelection(userA))
            val selectionAfterRemovingA = awaitItem().selectedUsers
            assertThat(selectionAfterRemovingA).isEmpty()
        }
    }
}
