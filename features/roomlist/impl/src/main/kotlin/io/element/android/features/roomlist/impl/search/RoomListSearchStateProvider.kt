/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.search

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.roomlist.impl.aRoomListRoomSummaryList
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

class RoomListSearchStateProvider : PreviewParameterProvider<RoomListSearchState> {
    override val values: Sequence<RoomListSearchState>
        get() = sequenceOf(
            aRoomListSearchState(),
            aRoomListSearchState(isRoomDirectorySearchEnabled = true),
            aRoomListSearchState(
                isSearchActive = true,
                query = "Test",
                results = aRoomListRoomSummaryList()
            ),
        )
}

fun aRoomListSearchState(
    isSearchActive: Boolean = false,
    query: String = "",
    results: ImmutableList<RoomListRoomSummary> = persistentListOf(),
    isRoomDirectorySearchEnabled: Boolean = false,
    eventSink: (RoomListSearchEvents) -> Unit = { },
) = RoomListSearchState(
    isDebugBuild = false,
    isSearchActive = isSearchActive,
    query = query,
    results = results,
    isRoomDirectorySearchEnabled = isRoomDirectorySearchEnabled,
    eventSink = eventSink,
)
