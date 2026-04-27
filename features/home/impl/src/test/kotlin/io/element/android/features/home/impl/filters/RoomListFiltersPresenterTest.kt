/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.filters

import com.google.common.truth.Truth.assertThat
import io.element.android.features.home.impl.filters.selection.DefaultFilterSelectionStrategy
import io.element.android.features.home.impl.filters.selection.FilterSelectionState
import io.element.android.tests.testutils.awaitLastSequentialItem
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomListFiltersPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createRoomListFiltersPresenter()
        presenter.test {
            awaitItem().let { state ->
                assertThat(state.hasAnyFilterSelected).isFalse()
                assertThat(state.filterSelectionStates).containsExactly(
                    filterSelectionState(RoomListFilter.Unread, false),
                    filterSelectionState(RoomListFilter.People, false),
                    filterSelectionState(RoomListFilter.Rooms, false),
                    filterSelectionState(RoomListFilter.Favourites, false),
                    filterSelectionState(RoomListFilter.LowPriority, false),
                    filterSelectionState(RoomListFilter.Invites, false),
                )
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `present - toggle rooms filter`() = runTest {
        val presenter = createRoomListFiltersPresenter()
        presenter.test {
            awaitItem().eventSink.invoke(RoomListFiltersEvent.ToggleFilter(RoomListFilter.Rooms))
            awaitLastSequentialItem().let { state ->
                assertThat(state.hasAnyFilterSelected).isTrue()
                assertThat(state.filterSelectionStates).containsExactly(
                    filterSelectionState(RoomListFilter.Rooms, true),
                    filterSelectionState(RoomListFilter.Unread, false),
                    filterSelectionState(RoomListFilter.Favourites, false),
                    filterSelectionState(RoomListFilter.LowPriority, false),
                ).inOrder()

                assertThat(state.selectedFilters()).containsExactly(
                    RoomListFilter.Rooms,
                )
                state.eventSink.invoke(RoomListFiltersEvent.ToggleFilter(RoomListFilter.Rooms))
            }
            advanceUntilIdle()
            awaitLastSequentialItem().let { state ->
                assertThat(state.hasAnyFilterSelected).isFalse()
                assertThat(state.filterSelectionStates).containsExactly(
                    filterSelectionState(RoomListFilter.Unread, false),
                    filterSelectionState(RoomListFilter.People, false),
                    filterSelectionState(RoomListFilter.Rooms, false),
                    filterSelectionState(RoomListFilter.Favourites, false),
                    filterSelectionState(RoomListFilter.LowPriority, false),
                    filterSelectionState(RoomListFilter.Invites, false),
                ).inOrder()
                assertThat(state.selectedFilters()).isEmpty()
            }
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `present - clear filters event`() = runTest {
        val presenter = createRoomListFiltersPresenter()
        presenter.test {
            awaitItem().eventSink.invoke(RoomListFiltersEvent.ToggleFilter(RoomListFilter.Rooms))
            awaitLastSequentialItem().let { state ->
                assertThat(state.hasAnyFilterSelected).isTrue()
                state.eventSink.invoke(RoomListFiltersEvent.ClearSelectedFilters)
            }
            advanceUntilIdle()
            awaitLastSequentialItem().let { state ->
                assertThat(state.hasAnyFilterSelected).isFalse()
            }
        }
    }

    @Test
    fun `present - selecting low priority excludes favourites and invites`() = runTest {
        val presenter = createRoomListFiltersPresenter()
        presenter.test {
            awaitItem().eventSink.invoke(RoomListFiltersEvent.ToggleFilter(RoomListFilter.LowPriority))
            awaitLastSequentialItem().let { state ->
                assertThat(state.hasAnyFilterSelected).isTrue()
                assertThat(state.selectedFilters()).containsExactly(RoomListFilter.LowPriority)

                // Verify that Favourites and Invites are not in the filter selection states
                val filterStates = state.filterSelectionStates
                assertThat(filterStates.none { it.filter == RoomListFilter.Favourites }).isTrue()
                assertThat(filterStates.none { it.filter == RoomListFilter.Invites }).isTrue()

                // Verify LowPriority is selected
                assertThat(filterStates.any { it.filter == RoomListFilter.LowPriority && it.isSelected }).isTrue()
            }
        }
    }

    @Test
    fun `present - selecting favourites excludes low priority`() = runTest {
        val presenter = createRoomListFiltersPresenter()
        presenter.test {
            awaitItem().eventSink.invoke(RoomListFiltersEvent.ToggleFilter(RoomListFilter.Favourites))
            awaitLastSequentialItem().let { state ->
                assertThat(state.hasAnyFilterSelected).isTrue()
                assertThat(state.selectedFilters()).containsExactly(RoomListFilter.Favourites)

                // Verify that LowPriority is not in the filter selection states
                val filterStates = state.filterSelectionStates
                assertThat(filterStates.none { it.filter == RoomListFilter.LowPriority }).isTrue()

                // Verify Favourites is selected
                assertThat(filterStates.any { it.filter == RoomListFilter.Favourites && it.isSelected }).isTrue()
            }
        }
    }
}

private fun filterSelectionState(filter: RoomListFilter, selected: Boolean) = FilterSelectionState(
    filter = filter,
    isSelected = selected,
)

private fun TestScope.createRoomListFiltersPresenter(): RoomListFiltersPresenter {
    return RoomListFiltersPresenter(
        filterSelectionStrategy = DefaultFilterSelectionStrategy(),
    )
}
