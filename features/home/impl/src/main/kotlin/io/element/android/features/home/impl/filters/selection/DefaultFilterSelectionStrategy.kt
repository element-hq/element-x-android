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
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@ContributesBinding(SessionScope::class)
class DefaultFilterSelectionStrategy(
    private val sessionPreferencesStore: SessionPreferencesStore,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) : FilterSelectionStrategy {
    private val selectedFilters = LinkedHashSet<RoomListFilter>()
    private val availableFilters
        get() = RoomListFilter.entries.toSet()

    override val filterSelectionStates = MutableStateFlow(buildFilters())

    init {
        sessionCoroutineScope.launch {
            sessionPreferencesStore.getSelectedRoomListFilters().first()
                .mapNotNull { name -> tryOrNull { RoomListFilter.valueOf(name) } }
                .forEach { filter -> select(filter) }
        }
    }

    override fun select(filter: RoomListFilter) {
        if (selectedFilters.any { it in filter.incompatibleFilters }) return
        selectedFilters.add(filter)
        onSelectionChanged()
    }

    override fun deselect(filter: RoomListFilter) {
        selectedFilters.remove(filter)
        onSelectionChanged()
    }

    override fun isSelected(filter: RoomListFilter): Boolean {
        return selectedFilters.contains(filter)
    }

    override fun clear() {
        selectedFilters.clear()
        onSelectionChanged()
    }

    private fun onSelectionChanged() {
        filterSelectionStates.value = buildFilters()
        val names = selectedFilters.map { it.name }.toSet()
        sessionCoroutineScope.launch {
            sessionPreferencesStore.setSelectedRoomListFilters(names)
        }
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
