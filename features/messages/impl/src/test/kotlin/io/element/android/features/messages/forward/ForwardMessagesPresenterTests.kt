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

package io.element.android.features.messages.forward

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.forward.ForwardMessagesEvents
import io.element.android.features.messages.impl.forward.ForwardMessagesPresenter
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.RoomSummary
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.FakeRoomSummaryDataSource
import io.element.android.libraries.matrix.test.room.aRoomSummaryDetail
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ForwardMessagesPresenterTests {

    @Test
    fun `present - initial state`() = runTest {
        val presenter = aPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.selectedRooms).isEmpty()
            assertThat(initialState.resultState).isInstanceOf(SearchBarResultState.NotSearching::class.java)
            assertThat(initialState.isSearchActive).isFalse()
            assertThat(initialState.isForwarding).isFalse()
            assertThat(initialState.error).isNull()
            assertThat(initialState.forwardingSucceeded).isNull()

            // Search is run automatically
            val searchState = awaitItem()
            assertThat(searchState.resultState).isInstanceOf(SearchBarResultState.NoResults::class.java)
        }
    }

    @Test
    fun `present - toggle search active`() = runTest {
        val presenter = aPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)
            val summary = aRoomSummaryDetail()

            initialState.eventSink(ForwardMessagesEvents.ToggleSearchActive)
            assertThat(awaitItem().isSearchActive).isTrue()

            initialState.eventSink(ForwardMessagesEvents.ToggleSearchActive)
            assertThat(awaitItem().isSearchActive).isFalse()
        }
    }

    @Test
    fun `present - update query`() = runTest {
        val roomSummaryDataSource = FakeRoomSummaryDataSource().apply {
            postRoomSummary(listOf(RoomSummary.Filled(aRoomSummaryDetail())))
        }
        val client = FakeMatrixClient(roomSummaryDataSource = roomSummaryDataSource)
        val presenter = aPresenter(client = client)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(awaitItem().resultState as? SearchBarResultState.Results).isEqualTo(SearchBarResultState.Results(listOf(aRoomSummaryDetail())))

            initialState.eventSink(ForwardMessagesEvents.UpdateQuery("string not contained"))
            assertThat(awaitItem().query).isEqualTo("string not contained")
            assertThat(awaitItem().resultState).isInstanceOf(SearchBarResultState.NoResults::class.java)
        }
    }

    @Test
    fun `present - select a room and forward successful`() = runTest {
        val presenter = aPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)
            val summary = aRoomSummaryDetail()

            initialState.eventSink(ForwardMessagesEvents.SetSelectedRoom(summary))
            awaitItem()

            // Test successful forwarding
            initialState.eventSink(ForwardMessagesEvents.ForwardEvent)

            val forwardingState = awaitItem()
            assertThat(forwardingState.isSearchActive).isFalse()
            assertThat(forwardingState.isForwarding).isTrue()

            val successfulForwardState = awaitItem()
            assertThat(successfulForwardState.isForwarding).isFalse()
            assertThat(successfulForwardState.forwardingSucceeded).isNotNull()
        }
    }

    @Test
    fun `present - select a room and forward failed, then clear`() = runTest {
        val room = FakeMatrixRoom()
        val presenter = aPresenter(fakeMatrixRoom = room)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)
            val summary = aRoomSummaryDetail()

            initialState.eventSink(ForwardMessagesEvents.SetSelectedRoom(summary))
            awaitItem()

            // Test failed forwarding
            room.givenForwardEventResult(Result.failure(Throwable("error")))
            initialState.eventSink(ForwardMessagesEvents.ForwardEvent)
            skipItems(1)

            val failedForwardState = awaitItem()
            assertThat(failedForwardState.isForwarding).isFalse()
            assertThat(failedForwardState.error).isNotNull()

            // Then clear error
            initialState.eventSink(ForwardMessagesEvents.ClearError)
            assertThat(awaitItem().error).isNull()
        }
    }

    @Test
    fun `present - select and remove a room`() = runTest {
        val presenter = aPresenter()
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)
            val summary = aRoomSummaryDetail()

            initialState.eventSink(ForwardMessagesEvents.SetSelectedRoom(summary))
            assertThat(awaitItem().selectedRooms).isEqualTo(persistentListOf(summary))

            initialState.eventSink(ForwardMessagesEvents.RemoveSelectedRoom)
            assertThat(awaitItem().selectedRooms).isEmpty()
        }
    }

        private fun CoroutineScope.aPresenter(
        eventId: EventId = AN_EVENT_ID,
        fakeMatrixRoom: FakeMatrixRoom = FakeMatrixRoom(),
        coroutineScope: CoroutineScope = this,
        client: FakeMatrixClient = FakeMatrixClient(),
    ) = ForwardMessagesPresenter(eventId.value, fakeMatrixRoom, coroutineScope, client)

}
