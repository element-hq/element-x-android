/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdirectory.impl.root.model

import io.element.android.features.roomdirectory.api.RoomDescription
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class RoomDirectoryListState(
    val hasMoreToLoad: Boolean,
    val items: ImmutableList<RoomDescription>,
) {
    companion object {
        val Default = RoomDirectoryListState(
            hasMoreToLoad = true,
            items = persistentListOf()
        )
    }
}
