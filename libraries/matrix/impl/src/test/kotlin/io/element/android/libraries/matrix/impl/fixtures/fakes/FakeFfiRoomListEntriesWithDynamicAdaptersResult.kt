/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.RoomListDynamicEntriesController
import org.matrix.rustcomponents.sdk.RoomListEntriesWithDynamicAdaptersResult
import org.matrix.rustcomponents.sdk.TaskHandle

class FakeFfiRoomListEntriesWithDynamicAdaptersResult(
    private val controller: RoomListDynamicEntriesController = FakeFfiRoomListDynamicEntriesController(),
    private val entriesStream: TaskHandle = FakeFfiTaskHandle(),
) : RoomListEntriesWithDynamicAdaptersResult(NoHandle) {
    override fun controller(): RoomListDynamicEntriesController = controller
    override fun entriesStream(): TaskHandle = entriesStream
    override fun destroy() = Unit
}
