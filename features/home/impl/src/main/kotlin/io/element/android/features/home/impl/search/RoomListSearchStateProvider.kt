/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.features.home.impl.roomlist.aRoomListRoomSummaryList
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

class RoomListSearchStateProvider : PreviewParameterProvider<RoomListSearchState> {
    override val values: Sequence<RoomListSearchState>
        get() = sequenceOf(
            aRoomListSearchState(),
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
    eventSink: (RoomListSearchEvents) -> Unit = { },
) = RoomListSearchState(
    isSearchActive = isSearchActive,
    query = query,
    results = results,
    eventSink = eventSink,
)
