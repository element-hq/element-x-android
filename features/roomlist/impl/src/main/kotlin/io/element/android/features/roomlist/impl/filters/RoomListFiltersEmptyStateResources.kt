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

package io.element.android.features.roomlist.impl.filters

import androidx.annotation.StringRes
import io.element.android.features.roomlist.impl.R

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
