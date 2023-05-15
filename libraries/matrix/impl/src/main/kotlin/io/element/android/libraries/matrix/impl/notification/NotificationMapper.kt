/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.matrix.impl.notification

import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.notification.NotificationData
import org.matrix.rustcomponents.sdk.NotificationItem
import org.matrix.rustcomponents.sdk.use
import javax.inject.Inject

class NotificationMapper @Inject constructor() {

    fun map(notificationItem: NotificationItem): NotificationData {
        return notificationItem.use {
            NotificationData(
                senderId = UserId(it.event.senderId()),
                eventId = EventId(it.event.eventId()),
                roomId = RoomId(it.roomId),
                senderAvatarUrl = it.senderAvatarUrl,
                senderDisplayName = it.senderDisplayName,
                roomAvatarUrl = it.roomAvatarUrl,
                isDirect = it.isDirect,
                isEncrypted = it.isEncrypted.orFalse(),
                isNoisy = it.isNoisy
            )
        }
    }
}
