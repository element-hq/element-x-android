/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.filters

sealed interface RoomListFiltersEvents {
    data class ToggleFilter(val filter: RoomListFilter) : RoomListFiltersEvents
    data object ClearSelectedFilters : RoomListFiltersEvents
}
