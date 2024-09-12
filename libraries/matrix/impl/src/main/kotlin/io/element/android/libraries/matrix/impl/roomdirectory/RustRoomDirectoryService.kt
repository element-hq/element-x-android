/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomdirectory

import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryList
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.matrix.rustcomponents.sdk.Client

class RustRoomDirectoryService(
    private val client: Client,
    private val sessionDispatcher: CoroutineDispatcher,
) : RoomDirectoryService {
    override fun createRoomDirectoryList(scope: CoroutineScope): RoomDirectoryList {
        return RustRoomDirectoryList(client.roomDirectorySearch(), scope, sessionDispatcher)
    }
}
