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
