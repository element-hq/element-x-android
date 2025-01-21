/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.roomdirectory

import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryList
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryService
import kotlinx.coroutines.CoroutineScope

class FakeRoomDirectoryService(
    private val createRoomDirectoryListFactory: (CoroutineScope) -> RoomDirectoryList = { throw AssertionError("Configure a proper factory.") }
) : RoomDirectoryService {
    override fun createRoomDirectoryList(scope: CoroutineScope) = createRoomDirectoryListFactory(scope)
}
