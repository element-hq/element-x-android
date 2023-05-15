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
import io.element.android.features.roomdetails.aMatrixRoom
import io.element.android.features.roomdetails.impl.members.RoomMemberListDataSource
import io.element.android.features.roomdetails.impl.members.RoomMemberListEvents
import io.element.android.features.roomdetails.impl.members.RoomMemberListPresenter
import io.element.android.features.roomdetails.impl.members.RoomMemberSearchResultState
import io.element.android.features.roomdetails.impl.members.aRoomMemberList
import io.element.android.features.roomdetails.impl.members.aVictor
import io.element.android.features.roomdetails.impl.members.aWalter
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class RoomMemberListPresenterTests {

    @Test
    fun `search is done automatically on start, but is async`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            Truth.assertThat(initialState.roomMembers).isInstanceOf(Async.Loading::class.java)
            Truth.assertThat(initialState.searchQuery).isEmpty()
            Truth.assertThat(initialState.searchResults).isEqualTo(RoomMemberSearchResultState.NotSearching)
            Truth.assertThat(initialState.isSearchActive).isFalse()

            val loadedState = awaitItem()
            Truth.assertThat(loadedState.roomMembers).isInstanceOf(Async.Success::class.java)
            Truth.assertThat((loadedState.roomMembers as Async.Success).state.invited).isEqualTo(listOf(aVictor(), aWalter()))
            Truth.assertThat((loadedState.roomMembers as Async.Success).state.joined).isNotEmpty()
        }
    }

    @Test
    fun `open search`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val loadedState = awaitItem()

            loadedState.eventSink(RoomMemberListEvents.OnSearchActiveChanged(true))

            val searchActiveState = awaitItem()
            Truth.assertThat((searchActiveState.isSearchActive)).isTrue()
        }
    }

    @Test
    fun `search for something which is not found`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val loadedState = awaitItem()
            loadedState.eventSink(RoomMemberListEvents.OnSearchActiveChanged(true))
            val searchActiveState = awaitItem()
            loadedState.eventSink(RoomMemberListEvents.UpdateSearchQuery("something"))
            val searchQueryUpdatedState = awaitItem()
            Truth.assertThat((searchQueryUpdatedState.searchQuery)).isEqualTo("something")
            val searchSearchResultDelivered = awaitItem()
            Truth.assertThat((searchSearchResultDelivered.searchResults)).isInstanceOf(RoomMemberSearchResultState.NoResults::class.java)
        }
    }

    @Test
    fun `search for something which is found`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val loadedState = awaitItem()
            loadedState.eventSink(RoomMemberListEvents.OnSearchActiveChanged(true))
            val searchActiveState = awaitItem()
            loadedState.eventSink(RoomMemberListEvents.UpdateSearchQuery("Alice"))
            val searchQueryUpdatedState = awaitItem()
            Truth.assertThat((searchQueryUpdatedState.searchQuery)).isEqualTo("Alice")
            val searchSearchResultDelivered = awaitItem()
            Truth.assertThat((searchSearchResultDelivered.searchResults)).isInstanceOf(RoomMemberSearchResultState.Results::class.java)
            Truth.assertThat((searchSearchResultDelivered.searchResults as RoomMemberSearchResultState.Results).results.joined.first().displayName)
                .isEqualTo("Alice")

        }
    }
}

@ExperimentalCoroutinesApi
private fun createDataSource(
    matrixRoom: MatrixRoom = aMatrixRoom().apply {
        givenRoomMembersState(MatrixRoomMembersState.Ready(aRoomMemberList()))
    },
    coroutineDispatchers: CoroutineDispatchers = testCoroutineDispatchers()
) = RoomMemberListDataSource(matrixRoom, coroutineDispatchers)

@ExperimentalCoroutinesApi
private fun createPresenter(
    roomMemberListDataSource: RoomMemberListDataSource = createDataSource(),
    coroutineDispatchers: CoroutineDispatchers = testCoroutineDispatchers()
) = RoomMemberListPresenter(roomMemberListDataSource, coroutineDispatchers)
