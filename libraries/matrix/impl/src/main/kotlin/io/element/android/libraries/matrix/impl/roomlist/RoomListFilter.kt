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

package io.element.android.libraries.matrix.impl.roomlist

import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind
import org.matrix.rustcomponents.sdk.RoomListFilterCategory

fun RoomListFilter.toRustFilter(): RoomListEntriesDynamicFilterKind {
    return when (this) {
        is RoomListFilter.All -> RoomListEntriesDynamicFilterKind.All(filters.map { it.toRustFilter() })
        is RoomListFilter.Any -> RoomListEntriesDynamicFilterKind.Any(filters.map { it.toRustFilter() })
        RoomListFilter.Category.Group -> RoomListEntriesDynamicFilterKind.Category(RoomListFilterCategory.GROUP)
        RoomListFilter.Category.People -> RoomListEntriesDynamicFilterKind.Category(RoomListFilterCategory.PEOPLE)
        is RoomListFilter.FuzzyMatchRoomName -> RoomListEntriesDynamicFilterKind.FuzzyMatchRoomName(pattern)
        RoomListFilter.NonLeft -> RoomListEntriesDynamicFilterKind.NonLeft
        RoomListFilter.None -> RoomListEntriesDynamicFilterKind.None
        is RoomListFilter.NormalizedMatchRoomName -> RoomListEntriesDynamicFilterKind.NormalizedMatchRoomName(pattern)
        RoomListFilter.Unread -> RoomListEntriesDynamicFilterKind.Unread
        RoomListFilter.Favorite -> RoomListEntriesDynamicFilterKind.Favourite
    }
}
