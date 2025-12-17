/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.filters

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.home.impl.filters.selection.DefaultFilterSelectionStrategy
import io.element.android.features.home.impl.filters.selection.FilterSelectionState
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.tests.testutils.awaitLastSequentialItem
import kotlinx.coroutines.test.runTest
import org.junit.Test
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter as MatrixRoomListFilter

class RoomListFiltersPresenterTest {
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
                    filterSelectionState(RoomListFilter.Invites, false),
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
                    filterSelectionState(RoomListFilter.Invites, false),
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
): RoomListFiltersPresenter {
    return RoomListFiltersPresenter(
        roomListService = roomListService,
        filterSelectionStrategy = DefaultFilterSelectionStrategy(),
    )
}
