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

import com.google.common.truth.Truth.assertThat
import io.element.android.features.roomlist.impl.R
import org.junit.Test

class RoomListFiltersEmptyStateResourcesTest {
    @Test
    fun `fromSelectedFilters should return null when selectedFilters is empty`() {
        val selectedFilters = emptyList<RoomListFilter>()
        val result = RoomListFiltersEmptyStateResources.fromSelectedFilters(selectedFilters)
        assertThat(result).isNull()
    }

    @Test
    fun `fromSelectedFilters should return exact RoomListFiltersEmptyStateResources when selectedFilters has only unread filter`() {
        val selectedFilters = listOf(RoomListFilter.Unread)
        val result = RoomListFiltersEmptyStateResources.fromSelectedFilters(selectedFilters)
        assertThat(result).isNotNull()
        assertThat(result?.title).isEqualTo(R.string.screen_roomlist_filter_unreads_empty_state_title)
        assertThat(result?.subtitle).isEqualTo(R.string.screen_roomlist_filter_mixed_empty_state_subtitle)
    }

    @Test
    fun `fromSelectedFilters should return exact RoomListFiltersEmptyStateResources when selectedFilters has only people filter`() {
        val selectedFilters = listOf(RoomListFilter.People)
        val result = RoomListFiltersEmptyStateResources.fromSelectedFilters(selectedFilters)
        assertThat(result).isNotNull()
        assertThat(result?.title).isEqualTo(R.string.screen_roomlist_filter_people_empty_state_title)
        assertThat(result?.subtitle).isEqualTo(R.string.screen_roomlist_filter_mixed_empty_state_subtitle)
    }

    @Test
    fun `fromSelectedFilters should return exact RoomListFiltersEmptyStateResources when selectedFilters has only rooms filter`() {
        val selectedFilters = listOf(RoomListFilter.Rooms)
        val result = RoomListFiltersEmptyStateResources.fromSelectedFilters(selectedFilters)
        assertThat(result).isNotNull()
        assertThat(result?.title).isEqualTo(R.string.screen_roomlist_filter_rooms_empty_state_title)
        assertThat(result?.subtitle).isEqualTo(R.string.screen_roomlist_filter_mixed_empty_state_subtitle)
    }

    @Test
    fun `fromSelectedFilters should return exact RoomListFiltersEmptyStateResources when selectedFilters has only favourites filter`() {
        val selectedFilters = listOf(RoomListFilter.Favourites)
        val result = RoomListFiltersEmptyStateResources.fromSelectedFilters(selectedFilters)
        assertThat(result).isNotNull()
        assertThat(result?.title).isEqualTo(R.string.screen_roomlist_filter_favourites_empty_state_title)
        assertThat(result?.subtitle).isEqualTo(R.string.screen_roomlist_filter_favourites_empty_state_subtitle)
    }

    @Test
    fun `fromSelectedFilters should return exact RoomListFiltersEmptyStateResources when selectedFilters has only invites filter`() {
        val selectedFilters = listOf(RoomListFilter.Invites)
        val result = RoomListFiltersEmptyStateResources.fromSelectedFilters(selectedFilters)
        assertThat(result).isNotNull()
        assertThat(result?.title).isEqualTo(R.string.screen_roomlist_filter_invites_empty_state_title)
        assertThat(result?.subtitle).isEqualTo(R.string.screen_roomlist_filter_mixed_empty_state_subtitle)
    }

    @Test
    fun `fromSelectedFilters should return exact RoomListFiltersEmptyStateResources when selectedFilters has multiple filters`() {
        val selectedFilters = listOf(RoomListFilter.Unread, RoomListFilter.People, RoomListFilter.Rooms, RoomListFilter.Favourites)
        val result = RoomListFiltersEmptyStateResources.fromSelectedFilters(selectedFilters)
        assertThat(result).isNotNull()
        assertThat(result?.title).isEqualTo(R.string.screen_roomlist_filter_mixed_empty_state_title)
        assertThat(result?.subtitle).isEqualTo(R.string.screen_roomlist_filter_mixed_empty_state_subtitle)
    }
}
