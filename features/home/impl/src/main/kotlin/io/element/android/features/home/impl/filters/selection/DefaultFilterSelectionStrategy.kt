/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.filters.selection

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.home.impl.filters.RoomListFilter
import io.element.android.libraries.di.SessionScope
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultFilterSelectionStrategy @Inject constructor() : FilterSelectionStrategy {
    private val selectedFilters = LinkedHashSet<RoomListFilter>()

    override val filterSelectionStates = MutableStateFlow(buildFilters())

    override fun select(filter: RoomListFilter) {
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
        val rest = (RoomListFilter.entries - RoomListFilter.All).map { FilterSelectionState(filter = it, isSelected = selectedFilters.contains(it)) }
        val noneSelected = !rest.any { it.isSelected }
        val all = FilterSelectionState(filter = RoomListFilter.All, isSelected = noneSelected)

        return setOf(all) + rest.toSet()
    }
}
