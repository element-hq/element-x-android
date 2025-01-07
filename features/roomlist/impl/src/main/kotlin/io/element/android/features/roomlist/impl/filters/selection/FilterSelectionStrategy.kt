/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.filters.selection

import io.element.android.features.roomlist.impl.filters.RoomListFilter
import kotlinx.coroutines.flow.StateFlow

interface FilterSelectionStrategy {
    val filterSelectionStates: StateFlow<Set<FilterSelectionState>>

    fun select(filter: RoomListFilter)
    fun deselect(filter: RoomListFilter)
    fun isSelected(filter: RoomListFilter): Boolean
    fun clear()

    fun toggle(filter: RoomListFilter) {
        if (isSelected(filter)) {
            deselect(filter)
        } else {
            select(filter)
        }
    }
}
