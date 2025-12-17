/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search

import io.element.android.features.home.impl.model.RoomListRoomSummary
import kotlinx.collections.immutable.ImmutableList

data class RoomListSearchState(
    val isSearchActive: Boolean,
    val query: String,
    val results: ImmutableList<RoomListRoomSummary>,
    val eventSink: (RoomListSearchEvents) -> Unit
)
