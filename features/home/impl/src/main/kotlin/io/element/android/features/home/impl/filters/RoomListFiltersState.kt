/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.filters

import io.element.android.features.home.impl.filters.selection.FilterSelectionState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class RoomListFiltersState(
    val filterSelectionStates: ImmutableList<FilterSelectionState>,
    val eventSink: (RoomListFiltersEvents) -> Unit,
) {
    val hasAnyFilterSelected = filterSelectionStates.any { it.isSelected }

    fun selectedFilters(): ImmutableList<RoomListFilter> {
        return filterSelectionStates
            .filter { it.isSelected }
            .map { it.filter }
            .toImmutableList()
    }
}
