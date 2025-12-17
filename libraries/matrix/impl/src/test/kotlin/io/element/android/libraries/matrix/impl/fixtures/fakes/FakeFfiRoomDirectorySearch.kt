/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import io.element.android.tests.testutils.simulateLongTask
import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.RoomDirectorySearch
import org.matrix.rustcomponents.sdk.RoomDirectorySearchEntriesListener
import org.matrix.rustcomponents.sdk.RoomDirectorySearchEntryUpdate
import org.matrix.rustcomponents.sdk.TaskHandle

class FakeFfiRoomDirectorySearch(
    var isAtLastPage: Boolean = false,
) : RoomDirectorySearch(NoHandle) {
    override suspend fun isAtLastPage(): Boolean {
        return isAtLastPage
    }

    override suspend fun search(filter: String?, batchSize: UInt, viaServerName: String?) = simulateLongTask { }
    override suspend fun nextPage() = simulateLongTask { }

    private var listener: RoomDirectorySearchEntriesListener? = null

    override suspend fun results(listener: RoomDirectorySearchEntriesListener): TaskHandle {
        this.listener = listener
        return FakeFfiTaskHandle()
    }

    fun emitResult(roomEntriesUpdate: List<RoomDirectorySearchEntryUpdate>) {
        listener?.onUpdate(roomEntriesUpdate)
    }
}
