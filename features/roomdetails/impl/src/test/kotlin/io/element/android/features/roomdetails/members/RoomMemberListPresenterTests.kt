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

package io.element.android.features.roomdetails.members

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth
import io.element.android.features.roomdetails.impl.members.RoomMemberListPresenter
import io.element.android.features.userlist.api.SelectionMode
import io.element.android.features.userlist.api.UserListDataSource
import io.element.android.features.userlist.api.UserListDataStore
import io.element.android.features.userlist.api.UserListPresenter
import io.element.android.features.userlist.api.UserListPresenterArgs
import io.element.android.features.userlist.api.UserSearchResultState
import io.element.android.features.userlist.impl.DefaultUserListPresenter
import io.element.android.features.userlist.test.FakeUserListDataSource
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.ui.components.aMatrixUser
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.internal.toImmutableList
import org.junit.Test

@ExperimentalCoroutinesApi
class RoomMemberListPresenterTests {

    private val testCoroutineDispatchers = testCoroutineDispatchers()

    @Test
    fun `present - search is done automatically on start, but is async`() = runTest {
        val searchResult = listOf(aMatrixUser())
        val userListDataSource = FakeUserListDataSource().apply {
            givenSearchResult(searchResult)
        }
        val userListDataStore = UserListDataStore()
        val userListFactory = object : UserListPresenter.Factory {
            override fun create(
                args: UserListPresenterArgs,
                userListDataSource: UserListDataSource,
                userListDataStore: UserListDataStore,
            ) = DefaultUserListPresenter(args, userListDataSource, userListDataStore)
        }
        val fakeRoom = FakeMatrixRoom()
        val presenter = RoomMemberListPresenter(
            memberListPresenterFactory = userListFactory,
            memberListDataSource = userListDataSource,
            memberListDataStore = userListDataStore,
            room = fakeRoom,
            coroutineDispatchers = testCoroutineDispatchers
        )
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.allUsers).isInstanceOf(Async.Loading::class.java)
            Truth.assertThat(initialState.memberListState.isSearchActive).isFalse()
            Truth.assertThat(initialState.memberListState.searchResults).isEqualTo(UserSearchResultState.NotSearching)
            Truth.assertThat(initialState.memberListState.selectionMode).isEqualTo(SelectionMode.Single)

            val loadedState = awaitItem()
            Truth.assertThat((loadedState.allUsers as? Async.Success)?.state).isEqualTo(searchResult.toImmutableList())
        }
    }
}
