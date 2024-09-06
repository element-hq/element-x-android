/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.roomdirectory

import kotlinx.coroutines.CoroutineScope

interface RoomDirectoryService {
    fun createRoomDirectoryList(scope: CoroutineScope): RoomDirectoryList
}
