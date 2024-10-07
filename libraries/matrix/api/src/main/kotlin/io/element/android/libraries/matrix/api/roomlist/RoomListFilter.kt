/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.roomlist

sealed interface RoomListFilter {
    companion object {
        /**
         * Create a filter that matches all the given filters.
         * If no filters are provided, all the rooms will match.
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
     * If [filters] is empty, all the room will match.
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
