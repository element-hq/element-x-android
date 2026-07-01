/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.recentcalls.api

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.notification.CallIntent

data class RecentCallEntry(
    val id: String,
    val eventId: EventId?,
    val roomId: RoomId,
    val roomDisplayName: String,
    val avatarUrl: String?,
    val isDirect: Boolean,
    val counterpartUserId: UserId?,
    val direction: RecentCallDirection,
    val status: RecentCallStatus,
    val callIntent: CallIntent,
    val timestamp: Long,
    val durationMs: Long?,
)

enum class RecentCallDirection {
    INCOMING,
    OUTGOING,
}

enum class RecentCallStatus {
    COMPLETED,
    MISSED,
    DECLINED,
    ONGOING,
}
