/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.datasource

import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.test.FakeDateFormatter
import io.element.android.libraries.eventformatter.api.RoomLastMessageFormatter

fun aRoomListRoomSummaryFactory(
    dateFormatter: DateFormatter = FakeDateFormatter { _, _, _ -> "Today" },
    roomLastMessageFormatter: RoomLastMessageFormatter = RoomLastMessageFormatter { _, _ -> "Hey" }
) = RoomListRoomSummaryFactory(
    dateFormatter = dateFormatter,
    roomLastMessageFormatter = roomLastMessageFormatter
)
