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

package io.element.android.libraries.roomselect.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.roomselect.api.RoomSelectMode
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.aRoomSummaryDetail
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class RoomSelectPresenterTests {

    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = aPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.selectedRooms).isEmpty()
            assertThat(initialState.resultState).isInstanceOf(SearchBarResultState.NotSearching::class.java)
            assertThat(initialState.isSearchActive).isFalse()
            // Search is run automatically
            val searchState = awaitItem()
            assertThat(searchState.resultState).isInstanceOf(SearchBarResultState.NoResults::class.java)
        }
    }

    @Test
    fun `present - toggle search active`() = runTest {
        val presenter = aPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)

            initialState.eventSink(RoomSelectEvents.ToggleSearchActive)
            assertThat(awaitItem().isSearchActive).isTrue()

            initialState.eventSink(RoomSelectEvents.ToggleSearchActive)
            assertThat(awaitItem().isSearchActive).isFalse()
        }
    }

    @Test
    fun `present - update query`() = runTest {
        val roomListService = FakeRoomListService().apply {
            postAllRooms(listOf(RoomSummary.Filled(aRoomSummaryDetail())))
        }
        val client = FakeMatrixClient(roomListService = roomListService)
        val presenter = aPresenter(client = client)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(awaitItem().resultState as? SearchBarResultState.Results).isEqualTo(SearchBarResultState.Results(listOf(aRoomSummaryDetail())))

            initialState.eventSink(RoomSelectEvents.UpdateQuery("string not contained"))
            assertThat(awaitItem().query).isEqualTo("string not contained")
            assertThat(awaitItem().resultState).isInstanceOf(SearchBarResultState.NoResults::class.java)
        }
    }

    @Test
    fun `present - select and remove a room`() = runTest {
        val presenter = aPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            skipItems(1)
            val summary = aRoomSummaryDetail()

            initialState.eventSink(RoomSelectEvents.SetSelectedRoom(summary))
            assertThat(awaitItem().selectedRooms).isEqualTo(persistentListOf(summary))

            initialState.eventSink(RoomSelectEvents.RemoveSelectedRoom)
            assertThat(awaitItem().selectedRooms).isEmpty()
        }
    }

    private fun aPresenter(
        mode: RoomSelectMode = RoomSelectMode.Forward,
        client: FakeMatrixClient = FakeMatrixClient(),
    ) = RoomSelectPresenter(
        mode = mode,
        client = client,
    )
}
