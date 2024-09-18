/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.sdk

import io.element.android.libraries.matrix.api.core.RoomId
import org.matrix.rustcomponents.sdk.EventTimelineItem
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.RoomInfo
import org.matrix.rustcomponents.sdk.RoomListItem

class FakeRustRoomListItem(
    private val roomId: RoomId,
    private val roomInfo: RoomInfo = aRustRoomInfo(id = roomId.value),
    private val latestEvent: EventTimelineItem? = null,
) : RoomListItem(NoPointer) {
    override fun id(): String {
        return roomId.value
    }

    override suspend fun roomInfo(): RoomInfo {
        return roomInfo
    }

    override suspend fun latestEvent(): EventTimelineItem? {
        return latestEvent
    }
}
