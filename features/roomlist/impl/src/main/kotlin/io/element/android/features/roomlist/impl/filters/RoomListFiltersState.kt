/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.filters

import io.element.android.features.roomlist.impl.filters.selection.FilterSelectionState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

data class RoomListFiltersState(
    val filterSelectionStates: ImmutableList<FilterSelectionState>,
    val eventSink: (RoomListFiltersEvents) -> Unit,
) {
    val hasAnyFilterSelected = filterSelectionStates.any { it.isSelected }

    fun selectedFilters(): ImmutableList<RoomListFilter> {
        return filterSelectionStates
            .filter { it.isSelected }
            .map { it.filter }
            .toPersistentList()
    }
}
