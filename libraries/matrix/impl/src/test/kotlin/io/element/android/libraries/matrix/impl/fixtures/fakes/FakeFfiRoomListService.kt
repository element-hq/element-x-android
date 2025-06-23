/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.RoomList
import org.matrix.rustcomponents.sdk.RoomListService
import org.matrix.rustcomponents.sdk.RoomListServiceStateListener
import org.matrix.rustcomponents.sdk.RoomListServiceSyncIndicator
import org.matrix.rustcomponents.sdk.RoomListServiceSyncIndicatorListener
import org.matrix.rustcomponents.sdk.TaskHandle

class FakeFfiRoomListService : RoomListService(NoPointer) {
    override suspend fun allRooms(): RoomList {
        return FakeFfiRoomList()
    }

    private var listener: RoomListServiceSyncIndicatorListener? = null
    override fun syncIndicator(
        delayBeforeShowingInMs: UInt,
        delayBeforeHidingInMs: UInt,
        listener: RoomListServiceSyncIndicatorListener,
    ): TaskHandle {
        this.listener = listener
        return FakeFfiTaskHandle()
    }

    fun emitRoomListServiceSyncIndicator(syncIndicator: RoomListServiceSyncIndicator) {
        listener?.onUpdate(syncIndicator)
    }

    override fun state(listener: RoomListServiceStateListener): TaskHandle {
        return FakeFfiTaskHandle()
    }
}
