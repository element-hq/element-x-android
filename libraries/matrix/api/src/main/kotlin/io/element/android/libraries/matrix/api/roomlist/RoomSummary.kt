/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.roomlist

import io.element.android.libraries.matrix.api.room.RoomInfo

data class RoomSummary(
    val info: RoomInfo,
    val latestEvent: LatestEventValue,
) {
    val roomId = info.id
    val latestEventTimestamp = when (latestEvent) {
        is LatestEventValue.None -> null
        is LatestEventValue.Local -> latestEvent.timestamp
        is LatestEventValue.Remote -> latestEvent.timestamp
    }
    val isOneToOne get() = info.activeMembersCount == 2L
}
