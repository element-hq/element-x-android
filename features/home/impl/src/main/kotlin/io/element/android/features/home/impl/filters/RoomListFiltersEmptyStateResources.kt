/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.filters

import androidx.annotation.StringRes
import io.element.android.features.home.impl.R

/**
 * Holds the resources for the empty state when filters are applied to the room list.
 * @param title the title of the empty state
 * @param subtitle the subtitle of the empty state
 */
data class RoomListFiltersEmptyStateResources(
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
) {
    companion object {
        /**
         * Create a [RoomListFiltersEmptyStateResources] from a list of selected filters.
         */
        fun fromSelectedFilters(selectedFilters: List<RoomListFilter>): RoomListFiltersEmptyStateResources? {
            return when {
                selectedFilters.isEmpty() -> null
                selectedFilters.size == 1 -> {
                    when (selectedFilters.first()) {
                        RoomListFilter.Unread -> RoomListFiltersEmptyStateResources(
                            title = R.string.screen_roomlist_filter_unreads_empty_state_title,
                            subtitle = R.string.screen_roomlist_filter_mixed_empty_state_subtitle
                        )
                        RoomListFilter.People -> RoomListFiltersEmptyStateResources(
                            title = R.string.screen_roomlist_filter_people_empty_state_title,
                            subtitle = R.string.screen_roomlist_filter_mixed_empty_state_subtitle
                        )
                        RoomListFilter.Rooms -> RoomListFiltersEmptyStateResources(
                            title = R.string.screen_roomlist_filter_rooms_empty_state_title,
                            subtitle = R.string.screen_roomlist_filter_mixed_empty_state_subtitle
                        )
                        RoomListFilter.Favourites -> RoomListFiltersEmptyStateResources(
                            title = R.string.screen_roomlist_filter_favourites_empty_state_title,
                            subtitle = R.string.screen_roomlist_filter_favourites_empty_state_subtitle
                        )
                        RoomListFilter.Invites -> RoomListFiltersEmptyStateResources(
                            title = R.string.screen_roomlist_filter_invites_empty_state_title,
                            subtitle = R.string.screen_roomlist_filter_mixed_empty_state_subtitle
                        )
                    }
                }
                else -> RoomListFiltersEmptyStateResources(
                    title = R.string.screen_roomlist_filter_mixed_empty_state_title,
                    subtitle = R.string.screen_roomlist_filter_mixed_empty_state_subtitle
                )
            }
        }
    }
}
