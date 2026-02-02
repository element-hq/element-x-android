/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.spacefilters

import io.element.android.libraries.matrix.api.spaces.SpaceServiceFilter
import kotlinx.collections.immutable.ImmutableList

sealed interface SpaceFiltersState {
    data object Disabled : SpaceFiltersState

    data class Unselected(
        val eventSink: (SpaceFiltersEvent.Unselected) -> Unit,
    ) : SpaceFiltersState

    data class Selecting(
        val availableFilters: ImmutableList<SpaceServiceFilter>,
        val eventSink: (SpaceFiltersEvent.Selecting) -> Unit,
    ) : SpaceFiltersState

    data class Selected(
        val selectedFilter: SpaceServiceFilter,
        val eventSink: (SpaceFiltersEvent.Selected) -> Unit,
    ) : SpaceFiltersState
}
