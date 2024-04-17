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

package io.element.android.libraries.matrix.api.roomlist

sealed interface RoomListFilter {
    companion object {
        /**
         * Create a filter that matches all the given filters.
         */
        fun all(vararg filters: RoomListFilter): RoomListFilter {
            return All(filters.toList())
        }

        /**
         * Create a filter that matches any of the given filters.
         */
        fun any(vararg filters: RoomListFilter): RoomListFilter {
            return Any(filters.toList())
        }
    }

    /**
     * A filter that matches all the given filters.
     */
    data class All(
        val filters: List<RoomListFilter>
    ) : RoomListFilter

    /**
     * A filter that matches any of the given filters.
     */
    data class Any(
        val filters: List<RoomListFilter>
    ) : RoomListFilter

    /**
     * A filter that matches rooms that are unread.
     */
    data object Unread : RoomListFilter

    /**
     * A filter that matches rooms that are marked as favorite.
     */
    data object Favorite : RoomListFilter

    /**
     * A filter that matches rooms with Invited membership.
     */
    data object Invite : RoomListFilter

    /**
     * A filter that matches either Group or People rooms.
     */
    sealed interface Category : RoomListFilter {
        data object Group : Category
        data object People : Category
    }

    /**
     * A filter that matches no room.
     */
    data object None : RoomListFilter

    /**
     * A filter that matches rooms with a name using a normalized match.
     */
    data class NormalizedMatchRoomName(
        val pattern: String
    ) : RoomListFilter
}
