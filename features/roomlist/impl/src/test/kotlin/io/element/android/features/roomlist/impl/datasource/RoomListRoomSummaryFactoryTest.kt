/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.datasource

import io.element.android.libraries.dateformatter.api.LastMessageTimestampFormatter
import io.element.android.libraries.eventformatter.api.RoomLastMessageFormatter

fun aRoomListRoomSummaryFactory(
    lastMessageTimestampFormatter: LastMessageTimestampFormatter = LastMessageTimestampFormatter { _ -> "Today" },
    roomLastMessageFormatter: RoomLastMessageFormatter = RoomLastMessageFormatter { _, _ -> "Hey" }
) = RoomListRoomSummaryFactory(
    lastMessageTimestampFormatter = lastMessageTimestampFormatter,
    roomLastMessageFormatter = roomLastMessageFormatter
)
