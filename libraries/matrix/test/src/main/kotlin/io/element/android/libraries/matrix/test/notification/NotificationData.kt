/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        isEncrypted = false,
        isNoisy = false,
        timestamp = 0L,
        content = NotificationContent.MessageLike.RoomEncrypted,
        hasMention = false,
    )
}
