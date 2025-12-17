/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.notifications

import android.os.Parcelable
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.parcelize.Parcelize

@Parcelize
data class CallNotificationData(
    val sessionId: SessionId,
    val roomId: RoomId,
    val eventId: EventId,
    val senderId: UserId,
    val roomName: String?,
    val senderName: String?,
    val avatarUrl: String?,
    val notificationChannelId: String,
    val timestamp: Long,
    val textContent: String?,
    // Expiration timestamp in millis since epoch
    val expirationTimestamp: Long,
) : Parcelable
