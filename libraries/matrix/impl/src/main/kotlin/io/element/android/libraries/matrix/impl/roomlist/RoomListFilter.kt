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

import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind
import org.matrix.rustcomponents.sdk.RoomListFilterCategory

val RoomListFilter.predicate
    get() = when (this) {
        is RoomListFilter.All -> { _: RoomSummary -> true }
        is RoomListFilter.Any -> { _: RoomSummary -> true }
        RoomListFilter.None -> { _: RoomSummary -> false }
        RoomListFilter.Category.Group -> { roomSummary: RoomSummary ->
            !roomSummary.isDirect && !roomSummary.isInvited()
        }
        RoomListFilter.Category.People -> { roomSummary: RoomSummary ->
            roomSummary.isDirect && !roomSummary.isInvited()
        }
        RoomListFilter.Favorite -> { roomSummary: RoomSummary ->
            roomSummary.isFavorite && !roomSummary.isInvited()
        }
        RoomListFilter.Unread -> { roomSummary: RoomSummary ->
            !roomSummary.isInvited() && (roomSummary.numUnreadNotifications > 0 || roomSummary.isMarkedUnread)
        }
        is RoomListFilter.NormalizedMatchRoomName -> { roomSummary: RoomSummary ->
            roomSummary.name.orEmpty().contains(pattern, ignoreCase = true)
        }
        RoomListFilter.Invite -> { roomSummary: RoomSummary ->
            roomSummary.isInvited()
        }
        RoomListFilter.NonLeft -> { _: RoomSummary -> false }
    }

fun List<RoomSummary>.filter(filter: RoomListFilter): List<RoomSummary> {
    return when (filter) {
        is RoomListFilter.All -> {
            val predicates = filter.filters.map { it.predicate }
            filter { roomSummary -> predicates.all { it(roomSummary) } }
        }
        is RoomListFilter.Any -> {
            val predicates = filter.filters.map { it.predicate }
            filter { roomSummary -> predicates.any { it(roomSummary) } }
        }
        else -> filter(filter.predicate)
    }
}

private fun RoomSummary.isInvited() = currentUserMembership == CurrentUserMembership.INVITED

fun RoomListFilter.toRustFilter(): RoomListEntriesDynamicFilterKind {
    return when (this) {
        is RoomListFilter.All -> RoomListEntriesDynamicFilterKind.All(filters.map { it.toRustFilter() })
        is RoomListFilter.Any -> RoomListEntriesDynamicFilterKind.Any(filters.map { it.toRustFilter() })
        RoomListFilter.Category.Group -> RoomListEntriesDynamicFilterKind.Category(RoomListFilterCategory.GROUP)
        RoomListFilter.Category.People -> RoomListEntriesDynamicFilterKind.Category(RoomListFilterCategory.PEOPLE)
        RoomListFilter.None -> RoomListEntriesDynamicFilterKind.None
        is RoomListFilter.NormalizedMatchRoomName -> RoomListEntriesDynamicFilterKind.NormalizedMatchRoomName(pattern)
        RoomListFilter.Unread -> RoomListEntriesDynamicFilterKind.Unread
        RoomListFilter.Favorite -> RoomListEntriesDynamicFilterKind.Favourite
        RoomListFilter.Invite -> RoomListEntriesDynamicFilterKind.Invite
        RoomListFilter.NonLeft -> RoomListEntriesDynamicFilterKind.NonLeft
    }
}
