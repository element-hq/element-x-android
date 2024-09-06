/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
            !roomSummary.isDm && !roomSummary.isInvited()
        }
        RoomListFilter.Category.People -> { roomSummary: RoomSummary ->
            roomSummary.isDm && !roomSummary.isInvited()
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
