/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.RoomListService
import org.matrix.rustcomponents.sdk.SyncService
import org.matrix.rustcomponents.sdk.SyncServiceStateObserver
import org.matrix.rustcomponents.sdk.TaskHandle

class FakeFfiSyncService(
    private val roomListService: RoomListService = FakeFfiRoomListService(),
) : SyncService(NoHandle) {
    override fun roomListService(): RoomListService = roomListService
    override fun state(listener: SyncServiceStateObserver): TaskHandle {
        return FakeFfiTaskHandle()
    }
    override suspend fun stop() {}
}
