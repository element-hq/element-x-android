/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.notification

import io.element.android.libraries.matrix.api.notification.NotificationContent
import io.element.android.libraries.matrix.api.notification.NotificationData
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_TIMESTAMP
import io.element.android.libraries.matrix.test.A_USER_NAME_2

fun aNotificationData(
    content: NotificationContent = NotificationContent.MessageLike.RoomEncrypted,
    isDirect: Boolean = false,
    hasMention: Boolean = false,
    timestamp: Long = A_TIMESTAMP,
    senderDisplayName: String? = A_USER_NAME_2,
    senderIsNameAmbiguous: Boolean = false,
    roomDisplayName: String? = A_ROOM_NAME
): NotificationData {
    return NotificationData(
        eventId = AN_EVENT_ID,
        roomId = A_ROOM_ID,
        senderAvatarUrl = null,
        senderDisplayName = senderDisplayName,
        senderIsNameAmbiguous = senderIsNameAmbiguous,
        roomAvatarUrl = null,
        roomDisplayName = roomDisplayName,
        isDirect = isDirect,
        isDm = false,
        isEncrypted = false,
        isNoisy = false,
        timestamp = timestamp,
        content = content,
        hasMention = hasMention,
    )
}
