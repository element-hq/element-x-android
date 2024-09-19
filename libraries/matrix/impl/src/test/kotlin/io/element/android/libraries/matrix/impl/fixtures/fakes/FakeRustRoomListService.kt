/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.RoomList
import org.matrix.rustcomponents.sdk.RoomListService
import org.matrix.rustcomponents.sdk.RoomListServiceSyncIndicator
import org.matrix.rustcomponents.sdk.RoomListServiceSyncIndicatorListener
import org.matrix.rustcomponents.sdk.TaskHandle

class FakeRustRoomListService : RoomListService(NoPointer) {

    override suspend fun allRooms(): RoomList {
        return FakeRustRoomList()
    }

    private var listener: RoomListServiceSyncIndicatorListener? = null
    override fun syncIndicator(
        delayBeforeShowingInMs: UInt,
        delayBeforeHidingInMs: UInt,
        listener: RoomListServiceSyncIndicatorListener,
    ): TaskHandle {
        this.listener = listener
        return FakeRustTaskHandle()
    }

    fun emitRoomListServiceSyncIndicator(syncIndicator: RoomListServiceSyncIndicator) {
        listener?.onUpdate(syncIndicator)
    }
}
