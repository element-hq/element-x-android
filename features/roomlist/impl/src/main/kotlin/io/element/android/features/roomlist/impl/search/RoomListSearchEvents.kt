/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.search

sealed interface RoomListSearchEvents {
    data object ToggleSearchVisibility : RoomListSearchEvents
    data class QueryChanged(val query: String) : RoomListSearchEvents
    data object ClearQuery : RoomListSearchEvents
}
