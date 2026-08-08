/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.filters.selection

import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.home.impl.filters.RoomListFilter
import io.element.android.libraries.di.SessionScope
import kotlinx.coroutines.flow.MutableStateFlow

@ContributesBinding(SessionScope::class)
class DefaultFilterSelectionStrategy : FilterSelectionStrategy {
    private val selectedFilters = LinkedHashSet<RoomListFilter>()
    private val availableFilters
        get() = RoomListFilter.entries.toSet()

    override val filterSelectionStates = MutableStateFlow(buildFilters())

    override fun select(filter: RoomListFilter) {
        // Incompatible filters are hidden from the UI rather than disabled, so a filter can still be
        // selected while its chip is on its way out, typically when two chips are tapped in the same
        // frame. Ignore it, the currently applied filters win.
        // See https://github.com/element-hq/element-x-android/issues/5383
        if (selectedFilters.any { it in filter.incompatibleFilters }) return
        selectedFilters.add(filter)
        filterSelectionStates.value = buildFilters()
    }

    override fun deselect(filter: RoomListFilter) {
        selectedFilters.remove(filter)
        filterSelectionStates.value = buildFilters()
    }

    override fun isSelected(filter: RoomListFilter): Boolean {
        return selectedFilters.contains(filter)
    }

    override fun clear() {
        selectedFilters.clear()
        filterSelectionStates.value = buildFilters()
    }

    private fun buildFilters(): Set<FilterSelectionState> {
        val selectedFilterStates = selectedFilters.map {
            FilterSelectionState(
                filter = it,
                isSelected = true
            )
        }
        val unselectedFilters = availableFilters - selectedFilters - selectedFilters.flatMap { it.incompatibleFilters }.toSet()
        val unselectedFilterStates = unselectedFilters.map {
            FilterSelectionState(
                filter = it,
                isSelected = false
            )
        }
        return (selectedFilterStates + unselectedFilterStates).toSet()
    }
}
