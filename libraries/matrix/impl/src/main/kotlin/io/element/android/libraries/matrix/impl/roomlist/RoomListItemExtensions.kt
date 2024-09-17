/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomlist

import org.matrix.rustcomponents.sdk.RoomInterface
import org.matrix.rustcomponents.sdk.RoomListItem
import org.matrix.rustcomponents.sdk.TimelineEventTypeFilter

/** Returns a `Room` with an initialized timeline using the given [filter]. */
suspend fun RoomListItem.fullRoomWithTimeline(filter: TimelineEventTypeFilter? = null): RoomInterface {
    if (!isTimelineInitialized()) {
        initTimeline(filter, "live")
    }
    return fullRoom()
}
