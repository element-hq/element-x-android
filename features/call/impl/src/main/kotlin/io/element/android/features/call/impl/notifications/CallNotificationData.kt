/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
) : Parcelable
