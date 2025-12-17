/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.filters

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.home.impl.filters.selection.FilterSelectionState
import kotlinx.collections.immutable.toImmutableList

class RoomListFiltersStateProvider : PreviewParameterProvider<RoomListFiltersState> {
    override val values: Sequence<RoomListFiltersState>
        get() = sequenceOf(
            aRoomListFiltersState(),
            aRoomListFiltersState(
                filterSelectionStates = RoomListFilter.entries.map { FilterSelectionState(it, isSelected = true) }
            ),
        )
}

fun aRoomListFiltersState(
    filterSelectionStates: List<FilterSelectionState> = RoomListFilter.entries.map { FilterSelectionState(it, isSelected = false) },
    eventSink: (RoomListFiltersEvents) -> Unit = {},
) = RoomListFiltersState(
    filterSelectionStates = filterSelectionStates.toImmutableList(),
    eventSink = eventSink,
)
