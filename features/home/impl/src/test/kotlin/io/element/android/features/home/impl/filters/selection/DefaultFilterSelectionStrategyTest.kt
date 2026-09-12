/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.filters.selection

import com.google.common.truth.Truth.assertThat
import io.element.android.features.home.impl.filters.RoomListFilter
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultFilterSelectionStrategyTest {
    @Test
    fun `selecting a filter stores it`() = runTest {
        val store = InMemorySessionPreferencesStore()
        val strategy = createStrategy(store)
        strategy.select(RoomListFilter.Rooms)
        advanceUntilIdle()
        assertThat(store.getSelectedRoomListFilters().first()).containsExactly(RoomListFilter.Rooms.name)
    }

    @Test
    fun `deselecting a filter removes it from the store`() = runTest {
        val store = InMemorySessionPreferencesStore(selectedRoomListFilters = setOf(RoomListFilter.Rooms.name))
        val strategy = createStrategy(store)
        advanceUntilIdle()
        strategy.deselect(RoomListFilter.Rooms)
        advanceUntilIdle()
        assertThat(store.getSelectedRoomListFilters().first()).isEmpty()
    }

    @Test
    fun `the filters selected last time are restored`() = runTest {
        val store = InMemorySessionPreferencesStore(selectedRoomListFilters = setOf(RoomListFilter.Favourites.name))
        val strategy = createStrategy(store)
        advanceUntilIdle()
        assertThat(strategy.isSelected(RoomListFilter.Favourites)).isTrue()
    }

    @Test
    fun `a stored filter which is no longer known is ignored`() = runTest {
        val store = InMemorySessionPreferencesStore(selectedRoomListFilters = setOf("NotAFilter"))
        val strategy = createStrategy(store)
        advanceUntilIdle()
        assertThat(strategy.filterSelectionStates.value.none { it.isSelected }).isTrue()
    }

    @Test
    fun `incompatible stored filters do not both come back`() = runTest {
        val store = InMemorySessionPreferencesStore(
            selectedRoomListFilters = setOf(RoomListFilter.Rooms.name, RoomListFilter.People.name),
        )
        val strategy = createStrategy(store)
        advanceUntilIdle()
        assertThat(strategy.isSelected(RoomListFilter.Rooms) && strategy.isSelected(RoomListFilter.People)).isFalse()
    }

    private fun TestScope.createStrategy(store: InMemorySessionPreferencesStore) = DefaultFilterSelectionStrategy(
        sessionPreferencesStore = store,
        sessionCoroutineScope = this,
    )
}
