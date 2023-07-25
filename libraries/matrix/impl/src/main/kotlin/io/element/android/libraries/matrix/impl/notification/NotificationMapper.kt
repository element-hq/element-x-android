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
import io.element.android.libraries.matrix.api.notification.NotificationEvent
import org.matrix.rustcomponents.sdk.use
import org.matrix.rustcomponents.sdk.NotificationEvent as RustNotificationEvent
import org.matrix.rustcomponents.sdk.NotificationItem as RustNotificationItem

class NotificationMapper {

    private val notificationEventMapper = NotificationEventMapper()

    fun map(roomId: RoomId, notificationItem: RustNotificationItem): NotificationData {
        return notificationItem.use { item ->
            when (val event = item.event) {
                is RustNotificationEvent.Timeline -> NotificationData.Message(
                    senderId = UserId(item.event.senderId()),
                    eventId = item.event.eventId()?.let(::EventId),
                    roomId = roomId,
                    senderAvatarUrl = item.senderInfo.avatarUrl,
                    senderDisplayName = item.senderInfo.displayName,
                    roomAvatarUrl = item.roomInfo.avatarUrl ?: item.senderInfo.avatarUrl.takeIf { item.roomInfo.isDirect },
                    roomDisplayName = item.roomInfo.displayName,
                    isDirect = item.roomInfo.isDirect,
                    isEncrypted = item.roomInfo.isEncrypted.orFalse(),
                    isNoisy = item.isNoisy.orFalse(),
                    event = item.event.use { notificationEventMapper.map(it)!! }
                )
                is RustNotificationEvent.Invite -> NotificationData.Invite(
                    senderId = UserId(event.senderId()),
                    roomId = roomId,
                    senderAvatarUrl = item.senderInfo.avatarUrl,
                    senderDisplayName = item.senderInfo.displayName,
                    roomAvatarUrl = item.roomInfo.avatarUrl ?: item.senderInfo.avatarUrl.takeIf { item.roomInfo.isDirect },
                    roomDisplayName = item.roomInfo.displayName,
                    isDirect = item.roomInfo.isDirect,
                    isEncrypted = item.roomInfo.isEncrypted.orFalse(),
                    isNoisy = item.isNoisy.orFalse(),
                )
            }
        }
    }
}

class NotificationEventMapper {

    private val timelineEventMapper = TimelineEventMapper()

    fun map(notificationEvent: RustNotificationEvent): NotificationEvent? {
        return when (notificationEvent) {
            is RustNotificationEvent.Timeline -> timelineEventMapper.map(notificationEvent.event)
            is RustNotificationEvent.Invite -> null
        }
    }
}

private fun RustNotificationEvent.senderId(): String {
    return when (this) {
        is RustNotificationEvent.Invite -> senderId
        is RustNotificationEvent.Timeline -> event.senderId()
    }
}

private fun RustNotificationEvent.eventId(): String? {
    return when (this) {
        is RustNotificationEvent.Invite -> null
        is RustNotificationEvent.Timeline -> event.eventId()
    }
}
