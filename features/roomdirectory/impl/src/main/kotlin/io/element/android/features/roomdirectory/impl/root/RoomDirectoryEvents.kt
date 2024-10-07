/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdirectory.impl.root

sealed interface RoomDirectoryEvents {
    data class Search(val query: String) : RoomDirectoryEvents
    data object LoadMore : RoomDirectoryEvents
}
