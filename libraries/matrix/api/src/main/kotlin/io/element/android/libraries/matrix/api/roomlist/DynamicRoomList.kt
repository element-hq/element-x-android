/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.matrix.api.roomlist

/**
 * RoomList with dynamic filtering and loading.
 * This is useful for large lists of rooms.
 * It lets load rooms on demand and filter them.
 */
interface DynamicRoomList : RoomList {

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val DEFAULT_PAGES_TO_LOAD = 10
    }

    sealed interface Filter {
        /**
         * No filter applied.
         */
        data object All : Filter

        /**
         * Filter all rooms.
         */
        data object None : Filter

        /**
         * Filter rooms by normalized room name.
         */
        data class NormalizedMatchRoomName(val pattern: String) : Filter
    }

    /**
     * Load more rooms into the list if possible.
     */
    suspend fun loadMore()

    /**
     * Reset the list to its initial size.
     */
    suspend fun reset()

    /**
     * Update the filter to apply to the list.
     * @param filter the filter to apply.
     */
    suspend fun updateFilter(filter: Filter)
}
