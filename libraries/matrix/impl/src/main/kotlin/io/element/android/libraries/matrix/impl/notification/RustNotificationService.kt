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

import io.element.android.libraries.matrix.api.notification.NotificationData
import io.element.android.libraries.matrix.api.notification.NotificationService
import java.io.File

class RustNotificationService(
    private val baseDirectory: File,
) : NotificationService {
    private val notificationMapper: NotificationMapper = NotificationMapper()

    override fun getNotification(userId: String, roomId: String, eventId: String): NotificationData? {
        return org.matrix.rustcomponents.sdk.NotificationService(
            basePath = File(baseDirectory, "sessions").absolutePath,
            userId = userId
        ).use {
            // TODO Not implemented yet, see https://github.com/matrix-org/matrix-rust-sdk/issues/1628
            it.getNotificationItem(roomId, eventId)?.let { notificationItem ->
                notificationMapper.map(notificationItem)
            }
        }
    }
}
