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

fun aNotificationData(
    senderDisplayName: String?,
    senderIsNameAmbiguous: Boolean,
): NotificationData {
    return NotificationData(
        eventId = AN_EVENT_ID,
        roomId = A_ROOM_ID,
        senderAvatarUrl = null,
        senderDisplayName = senderDisplayName,
        senderIsNameAmbiguous = senderIsNameAmbiguous,
        roomAvatarUrl = null,
        roomDisplayName = null,
        isDirect = false,
        isDm = false,
        isEncrypted = false,
        isNoisy = false,
        timestamp = 0L,
        content = NotificationContent.MessageLike.RoomEncrypted,
        hasMention = false,
    )
}
