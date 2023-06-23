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

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.notification.NotificationData
import io.element.android.libraries.matrix.api.notification.NotificationService
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.use

class RustNotificationService(
    private val client: Client,
) : NotificationService {
    private val notificationMapper: NotificationMapper = NotificationMapper()

    override fun getNotification(
        userId: SessionId,
        roomId: RoomId,
        eventId: EventId
    ): Result<NotificationData?> {
        return runCatching {
            client.getNotificationItem(roomId.value, eventId.value)?.use(notificationMapper::map)
        }
    }
}
