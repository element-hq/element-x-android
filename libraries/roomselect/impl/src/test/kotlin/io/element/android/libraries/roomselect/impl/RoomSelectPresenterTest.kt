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
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.libraries.roomselect.api.RoomSelectMode
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class RoomSelectPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createRoomSelectPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.selectedRooms).isEmpty()
            assertThat(initialState.resultState).isInstanceOf(SearchBarResultState.Initial::class.java)
            assertThat(initialState.isSearchActive).isFalse()
        }
    }

    @Test
    fun `present - toggle search active`() = runTest {
        val presenter = createRoomSelectPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(RoomSelectEvents.ToggleSearchActive)
            assertThat(awaitItem().isSearchActive).isTrue()
            initialState.eventSink(RoomSelectEvents.ToggleSearchActive)
            assertThat(awaitItem().isSearchActive).isFalse()
        }
    }

    @Test
    fun `present - update query`() = runTest {
        val roomListService = FakeRoomListService().apply {
            postAllRooms(listOf(aRoomSummary()))
        }
        val presenter = createRoomSelectPresenter(
            roomListService = roomListService
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(awaitItem().resultState as? SearchBarResultState.Results).isEqualTo(SearchBarResultState.Results(listOf(aRoomSummary())))
            initialState.eventSink(RoomSelectEvents.ToggleSearchActive)
            skipItems(1)
            initialState.eventSink(RoomSelectEvents.UpdateQuery("string not contained"))
            assertThat(
                roomListService.allRooms.currentFilter.value
            ).isEqualTo(
                RoomListFilter.NormalizedMatchRoomName("string not contained")
            )
            assertThat(awaitItem().query).isEqualTo("string not contained")
            roomListService.postAllRooms(
                emptyList()
            )
            assertThat(awaitItem().resultState).isInstanceOf(SearchBarResultState.NoResultsFound::class.java)
        }
    }

    @Test
    fun `present - select and remove a room`() = runTest {
        val roomListService = FakeRoomListService().apply {
            postAllRooms(listOf(aRoomSummary()))
        }
        val presenter = createRoomSelectPresenter(
            roomListService = roomListService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val summary = aRoomSummary()
            initialState.eventSink(RoomSelectEvents.SetSelectedRoom(summary))
            assertThat(awaitItem().selectedRooms).isEqualTo(persistentListOf(summary))
            initialState.eventSink(RoomSelectEvents.RemoveSelectedRoom)
            assertThat(awaitItem().selectedRooms).isEmpty()
            cancel()
        }
    }

    private fun TestScope.createRoomSelectPresenter(
        mode: RoomSelectMode = RoomSelectMode.Forward,
        roomListService: RoomListService = FakeRoomListService(),
    ) = RoomSelectPresenter(
        mode = mode,
        dataSource = RoomSelectSearchDataSource(
            roomListService = roomListService,
            coroutineDispatchers = testCoroutineDispatchers(),
        ),
    )
}
