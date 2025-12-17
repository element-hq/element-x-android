/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomlist

import io.element.android.libraries.core.extensions.withoutAccents
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.isDm
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomSummary

val RoomListFilter.predicate
    get() = when (this) {
        is RoomListFilter.All -> { roomSummary -> NonSpacePredicate(roomSummary) || IsInvitedPredicate(roomSummary) }
        is RoomListFilter.Any -> { roomSummary -> NonSpacePredicate(roomSummary) || IsInvitedPredicate(roomSummary) }
        RoomListFilter.None -> { _ -> false }
        RoomListFilter.Category.Group -> { roomSummary: RoomSummary ->
            !roomSummary.info.isDm && NonInvitedPredicate(roomSummary) && NonSpacePredicate(roomSummary)
        }
        RoomListFilter.Category.People -> { roomSummary: RoomSummary ->
            roomSummary.info.isDm && NonInvitedPredicate(roomSummary) && NonSpacePredicate(roomSummary)
        }
        RoomListFilter.Category.Space -> IsSpacePredicate
        RoomListFilter.Favorite -> { roomSummary: RoomSummary ->
            roomSummary.info.isFavorite && NonInvitedPredicate(roomSummary) && NonSpacePredicate(roomSummary)
        }
        RoomListFilter.Unread -> { roomSummary: RoomSummary ->
            NonInvitedPredicate(roomSummary) &&
                NonSpacePredicate(roomSummary) &&
                (roomSummary.info.numUnreadNotifications > 0 || roomSummary.info.isMarkedUnread)
        }
        is RoomListFilter.NormalizedMatchRoomName -> { roomSummary: RoomSummary ->
            roomSummary.info.name?.withoutAccents().orEmpty().contains(normalizedPattern, ignoreCase = true) &&
                (NonSpacePredicate(roomSummary) || IsInvitedPredicate(roomSummary))
        }
        RoomListFilter.Invite -> IsInvitedPredicate
    }

fun List<RoomSummary>.filter(filter: RoomListFilter): List<RoomSummary> {
    return when (filter) {
        is RoomListFilter.All -> {
            val predicates = if (filter.filters.isNotEmpty()) {
                filter.filters.map { it.predicate }
            } else {
                listOf(filter.predicate)
            }
            filter { roomSummary -> predicates.all { it(roomSummary) } }
        }
        is RoomListFilter.Any -> {
            val predicates = if (filter.filters.isNotEmpty()) {
                filter.filters.map { it.predicate }
            } else {
                listOf(filter.predicate)
            }
            filter { roomSummary -> predicates.any { it(roomSummary) } }
        }
        else -> filter(filter.predicate)
    }
}

private val IsSpacePredicate = { roomSummary: RoomSummary -> roomSummary.info.isSpace }

private val NonSpacePredicate = { roomSummary: RoomSummary -> !IsSpacePredicate(roomSummary) }

private val IsInvitedPredicate = { roomSummary: RoomSummary -> roomSummary.info.currentUserMembership == CurrentUserMembership.INVITED }

private val NonInvitedPredicate = { roomSummary: RoomSummary -> !IsInvitedPredicate(roomSummary) }
