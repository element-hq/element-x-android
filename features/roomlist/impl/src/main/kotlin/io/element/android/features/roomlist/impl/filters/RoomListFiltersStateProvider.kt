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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class RoomListFiltersStateProvider : PreviewParameterProvider<RoomListFiltersState> {
    override val values: Sequence<RoomListFiltersState>
        get() = sequenceOf(
            aRoomListFiltersState(),
            aRoomListFiltersState(
                selectedFilters = persistentListOf(RoomListFilter.Rooms, RoomListFilter.Favourites),
                unselectedFilters = persistentListOf(RoomListFilter.Unread),
            ),
        )
}

fun aRoomListFiltersState(
    unselectedFilters: ImmutableList<RoomListFilter> = RoomListFilter.entries.toImmutableList(),
    selectedFilters: ImmutableList<RoomListFilter> = persistentListOf(),
    isFeatureEnabled: Boolean = true,
    eventSink: (RoomListFiltersEvents) -> Unit = {},
) = RoomListFiltersState(
    unselectedFilters = unselectedFilters,
    selectedFilters = selectedFilters,
    isFeatureEnabled = isFeatureEnabled,
    eventSink = eventSink,
)
