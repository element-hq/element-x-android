/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.recentcalls.api

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.notification.CallIntent
import kotlinx.coroutines.flow.StateFlow

interface CallSessionRecorder {
    val ongoingEntries: StateFlow<List<RecentCallEntry>>

    fun onIncomingRing(
        roomId: RoomId,
        roomDisplayName: String,
        avatarUrl: String?,
        isDirect: Boolean,
        counterpartUserId: UserId?,
        callIntent: CallIntent,
        timestamp: Long,
    )

    fun onJoined(roomId: RoomId, timestamp: Long)

    fun onMissed(
        roomId: RoomId,
        callIntent: CallIntent,
        timestamp: Long,
    )

    fun onDeclined(
        roomId: RoomId,
        callIntent: CallIntent,
        timestamp: Long,
    )

    fun onCompleted(roomId: RoomId, durationMs: Long)
}
