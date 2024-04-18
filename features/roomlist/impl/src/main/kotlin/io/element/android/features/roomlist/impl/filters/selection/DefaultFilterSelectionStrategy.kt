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

package io.element.android.features.roomlist.impl.filters.selection

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.roomlist.impl.filters.RoomListFilter
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
        val selectedFilterStates = selectedFilters.map {
            FilterSelectionState(
                filter = it,
                isSelected = true
            )
        }
        val unselectedFilters = RoomListFilter.entries - selectedFilters - selectedFilters.flatMap { it.incompatibleFilters }.toSet()
        val unselectedFilterStates = unselectedFilters.map {
            FilterSelectionState(
                filter = it,
                isSelected = false
            )
        }
        return (selectedFilterStates + unselectedFilterStates).toSet()
    }
}
