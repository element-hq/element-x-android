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
        fun all(vararg filters: RoomListFilter): RoomListFilter {
            return All(filters.toList())
        }

        fun any(vararg filters: RoomListFilter): RoomListFilter {
            return Any(filters.toList())
        }
    }

    data class All(
        val filters: List<RoomListFilter>
    ) : RoomListFilter

    data class Any(
        val filters: List<RoomListFilter>
    ) : RoomListFilter

    data object NonLeft : RoomListFilter

    data object Unread : RoomListFilter

    sealed interface Category : RoomListFilter {
        data object Group : Category
        data object People : Category
    }

    data object None : RoomListFilter

    data class NormalizedMatchRoomName(
        val pattern: String
    ) : RoomListFilter

    data class FuzzyMatchRoomName(
        val pattern: String
    ) : RoomListFilter
}
