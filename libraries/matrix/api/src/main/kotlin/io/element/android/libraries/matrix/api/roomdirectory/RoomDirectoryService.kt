/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.roomdirectory

import kotlinx.coroutines.CoroutineScope

/**
 * Gives access to the public room directory, i.e. the rooms a server advertises to users who have not joined them.
 */
interface RoomDirectoryService {
    /**
     * Creates an independent directory search, which is stateful and holds one query at a time.
     *
     * @param scope the lifetime of the search; the underlying SDK resources are released when it completes, so there is nothing to close by hand.
     */
    fun createRoomDirectoryList(scope: CoroutineScope): RoomDirectoryList
}
