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

val RoomListFilter.predicate
    get() = when (this) {
        is RoomListFilter.All -> { _: RoomSummary -> true }
        is RoomListFilter.Any -> { _: RoomSummary -> true }
        RoomListFilter.None -> { _: RoomSummary -> false }
        RoomListFilter.Category.Group -> { roomSummary: RoomSummary ->
            roomSummary is RoomSummary.Filled && !roomSummary.details.isDirect && !roomSummary.isInvited()
        }
        RoomListFilter.Category.People -> { roomSummary: RoomSummary ->
            roomSummary is RoomSummary.Filled && roomSummary.details.isDirect && !roomSummary.isInvited()
        }
        RoomListFilter.Favorite -> { roomSummary: RoomSummary ->
            roomSummary is RoomSummary.Filled && roomSummary.details.isFavorite && !roomSummary.isInvited()
        }
        RoomListFilter.Unread -> { roomSummary: RoomSummary ->
            roomSummary is RoomSummary.Filled &&
                !roomSummary.isInvited() &&
                (roomSummary.details.numUnreadNotifications > 0 || roomSummary.details.isMarkedUnread)
        }
        is RoomListFilter.NormalizedMatchRoomName -> { roomSummary: RoomSummary ->
            roomSummary is RoomSummary.Filled && roomSummary.details.name.orEmpty().contains(pattern, ignoreCase = true)
        }
        RoomListFilter.Invite -> { roomSummary: RoomSummary ->
            roomSummary.isInvited()
        }
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

private fun RoomSummary.isInvited() = this is RoomSummary.Filled && this.details.currentUserMembership == CurrentUserMembership.INVITED
