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
                assertThat(state.selectedFilters).isEmpty()
                assertThat(state.hasAnyFilterSelected).isFalse()
                assertThat(state.unselectedFilters).containsExactly(
                    RoomListFilter.Rooms,
                    RoomListFilter.People,
                    RoomListFilter.Unread,
                    RoomListFilter.Favourites,
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

                assertThat(state.selectedFilters).containsExactly(RoomListFilter.Rooms)
                assertThat(state.hasAnyFilterSelected).isTrue()
                assertThat(state.unselectedFilters).containsExactly(
                    RoomListFilter.Unread,
                    RoomListFilter.Favourites,
                )
                val roomListCurrentFilter = roomListService.allRooms.currentFilter.value as MatrixRoomListFilter.All
                assertThat(roomListCurrentFilter.filters).containsExactly(
                    MatrixRoomListFilter.NonLeft,
                    MatrixRoomListFilter.Category.Group,
                )

                state.eventSink.invoke(RoomListFiltersEvents.ToggleFilter(RoomListFilter.Rooms))
            }

            awaitLastSequentialItem().let { state ->
                assertThat(state.selectedFilters).isEmpty()
                assertThat(state.hasAnyFilterSelected).isFalse()
                assertThat(state.unselectedFilters).containsExactly(
                    RoomListFilter.Rooms,
                    RoomListFilter.People,
                    RoomListFilter.Unread,
                    RoomListFilter.Favourites,
                )
                val roomListCurrentFilter = roomListService.allRooms.currentFilter.value as MatrixRoomListFilter.All
                assertThat(roomListCurrentFilter.filters).containsExactly(
                    MatrixRoomListFilter.NonLeft,
                )
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
                assertThat(state.selectedFilters).isNotEmpty()
                assertThat(state.hasAnyFilterSelected).isTrue()
                state.eventSink.invoke(RoomListFiltersEvents.ClearSelectedFilters)
            }
            awaitLastSequentialItem().let { state ->
                assertThat(state.selectedFilters).isEmpty()
                assertThat(state.hasAnyFilterSelected).isFalse()
            }
        }
    }
}

fun createRoomListFiltersPresenter(
    roomListService: RoomListService = FakeRoomListService(),
    featureFlagService: FeatureFlagService = FakeFeatureFlagService(),
): RoomListFiltersPresenter {
    return RoomListFiltersPresenter(
        roomListService = roomListService,
        featureFlagService = featureFlagService,
    )
}
