/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.search

import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import kotlinx.collections.immutable.ImmutableList

data class RoomListSearchState(
    val isDebugBuild: Boolean,
    val isSearchActive: Boolean,
    val query: String,
    val results: ImmutableList<RoomListRoomSummary>,
    val isRoomDirectorySearchEnabled: Boolean,
    val eventSink: (RoomListSearchEvents) -> Unit
) {
    val displayRoomDirectorySearch = query.isEmpty() && isRoomDirectorySearchEnabled
}
