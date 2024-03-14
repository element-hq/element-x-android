/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.roomlist.impl.filters

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.roomlist.impl.filters.selection.DefaultFilterSelectionStrategy
import io.element.android.features.roomlist.impl.filters.selection.FilterSelectionState
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.tests.testutils.awaitLastSequentialItem
import kotlinx.coroutines.test.runTest
import org.junit.Test
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter as MatrixRoomListFilter

class RoomListFiltersPresenterTests {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createRoomListFiltersPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().let { state ->
                assertThat(state.hasAnyFilterSelected).isFalse()
                assertThat(state.filterSelectionStates).containsExactly(
                    filterSelectionState(RoomListFilter.Unread, false),
                    filterSelectionState(RoomListFilter.People, false),
                    filterSelectionState(RoomListFilter.Rooms, false),
                    filterSelectionState(RoomListFilter.Favourites, false),
                )
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - toggle rooms filter`() = runTest {
        val roomListService = FakeRoomListService()
        val presenter = createRoomListFiltersPresenter(roomListService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink.invoke(RoomListFiltersEvents.ToggleFilter(RoomListFilter.Rooms))
            awaitLastSequentialItem().let { state ->

                assertThat(state.hasAnyFilterSelected).isTrue()
                assertThat(state.filterSelectionStates).containsExactly(
                    filterSelectionState(RoomListFilter.Rooms, true),
                    filterSelectionState(RoomListFilter.Unread, false),
                    filterSelectionState(RoomListFilter.Favourites, false),
                ).inOrder()

                assertThat(state.selectedFilters()).containsExactly(
                    RoomListFilter.Rooms,
                )
                val roomListCurrentFilter = roomListService.allRooms.currentFilter.value as MatrixRoomListFilter.All
                assertThat(roomListCurrentFilter.filters).containsExactly(
                    MatrixRoomListFilter.Category.Group,
                )
                state.eventSink.invoke(RoomListFiltersEvents.ToggleFilter(RoomListFilter.Rooms))
            }
            awaitLastSequentialItem().let { state ->
                assertThat(state.hasAnyFilterSelected).isFalse()
                assertThat(state.filterSelectionStates).containsExactly(
                    filterSelectionState(RoomListFilter.Unread, false),
                    filterSelectionState(RoomListFilter.People, false),
                    filterSelectionState(RoomListFilter.Rooms, false),
                    filterSelectionState(RoomListFilter.Favourites, false),
                ).inOrder()
                assertThat(state.selectedFilters()).isEmpty()
                val roomListCurrentFilter = roomListService.allRooms.currentFilter.value as MatrixRoomListFilter.All
                assertThat(roomListCurrentFilter.filters).isEmpty()
            }
        }
    }

    @Test
    fun `present - clear filters event`() = runTest {
        val roomListService = FakeRoomListService()
        val presenter = createRoomListFiltersPresenter(roomListService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink.invoke(RoomListFiltersEvents.ToggleFilter(RoomListFilter.Rooms))
            awaitLastSequentialItem().let { state ->
                assertThat(state.hasAnyFilterSelected).isTrue()
                state.eventSink.invoke(RoomListFiltersEvents.ClearSelectedFilters)
            }
            awaitLastSequentialItem().let { state ->
                assertThat(state.hasAnyFilterSelected).isFalse()
            }
        }
    }
}

private fun filterSelectionState(filter: RoomListFilter, selected: Boolean) = FilterSelectionState(
    filter = filter,
    isSelected = selected,
)

private fun createRoomListFiltersPresenter(
    roomListService: RoomListService = FakeRoomListService(),
    featureFlagService: FeatureFlagService = FakeFeatureFlagService(),
): RoomListFiltersPresenter {
    return RoomListFiltersPresenter(
        roomListService = roomListService,
        featureFlagService = featureFlagService,
        filterSelectionStrategy = DefaultFilterSelectionStrategy(),
    )
}
