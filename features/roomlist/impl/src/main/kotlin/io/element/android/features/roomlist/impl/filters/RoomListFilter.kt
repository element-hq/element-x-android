/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.filters

import io.element.android.features.roomlist.impl.R

/**
 * Enum class representing the different filters that can be applied to the room list.
 * Order is important, it'll be used as initial order in the UI.
 */
enum class RoomListFilter(val stringResource: Int) {
    Unread(R.string.screen_roomlist_filter_unreads),
    People(R.string.screen_roomlist_filter_people),
    Rooms(R.string.screen_roomlist_filter_rooms),
    Favourites(R.string.screen_roomlist_filter_favourites),
    Invites(R.string.screen_roomlist_filter_invites);

    val incompatibleFilters: Set<RoomListFilter>
        get() = when (this) {
            Rooms -> setOf(People, Invites)
            People -> setOf(Rooms, Invites)
            Unread -> setOf(Invites)
            Favourites -> setOf(Invites)
            Invites -> setOf(Rooms, People, Unread, Favourites)
        }
}
