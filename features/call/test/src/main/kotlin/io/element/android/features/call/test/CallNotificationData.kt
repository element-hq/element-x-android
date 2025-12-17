/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.test

import io.element.android.features.call.impl.notifications.CallNotificationData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.test.AN_AVATAR_URL
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.A_USER_NAME

fun aCallNotificationData(
    sessionId: SessionId = A_SESSION_ID,
    roomId: RoomId = A_ROOM_ID,
    eventId: EventId = AN_EVENT_ID,
    senderId: UserId = A_USER_ID_2,
    roomName: String = A_ROOM_NAME,
    senderName: String? = A_USER_NAME,
    avatarUrl: String? = AN_AVATAR_URL,
    notificationChannelId: String = "channel_id",
    timestamp: Long = 0L,
    expirationTimestamp: Long = 30_000L,
    textContent: String? = null,
): CallNotificationData = CallNotificationData(
    sessionId = sessionId,
    roomId = roomId,
    eventId = eventId,
    senderId = senderId,
    roomName = roomName,
    senderName = senderName,
    avatarUrl = avatarUrl,
    notificationChannelId = notificationChannelId,
    timestamp = timestamp,
    expirationTimestamp = expirationTimestamp,
    textContent = textContent,
)
