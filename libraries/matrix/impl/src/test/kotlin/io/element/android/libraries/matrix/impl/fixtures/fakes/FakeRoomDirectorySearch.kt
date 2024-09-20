/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import io.element.android.tests.testutils.simulateLongTask
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.RoomDirectorySearch
import org.matrix.rustcomponents.sdk.RoomDirectorySearchEntriesListener
import org.matrix.rustcomponents.sdk.RoomDirectorySearchEntryUpdate
import org.matrix.rustcomponents.sdk.TaskHandle

class FakeRoomDirectorySearch(
    var isAtLastPage: Boolean = false,
) : RoomDirectorySearch(NoPointer) {
    override suspend fun isAtLastPage(): Boolean {
        return isAtLastPage
    }

    override suspend fun search(filter: String?, batchSize: UInt) = simulateLongTask { }
    override suspend fun nextPage() = simulateLongTask { }

    private var listener: RoomDirectorySearchEntriesListener? = null

    override suspend fun results(listener: RoomDirectorySearchEntriesListener): TaskHandle {
        this.listener = listener
        return FakeRustTaskHandle()
    }

    fun emitResult(roomEntriesUpdate: List<RoomDirectorySearchEntryUpdate>) {
        listener?.onUpdate(roomEntriesUpdate)
    }
}
