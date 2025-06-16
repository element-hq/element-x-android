/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.notification

import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.notification.NotificationContent
import io.element.android.libraries.matrix.api.notification.NotificationData
import io.element.android.libraries.matrix.api.room.isDm
import io.element.android.services.toolbox.api.systemclock.SystemClock
import org.matrix.rustcomponents.sdk.NotificationEvent
import org.matrix.rustcomponents.sdk.NotificationItem
import org.matrix.rustcomponents.sdk.use

class NotificationMapper(
    private val clock: SystemClock,
) {
    private val notificationContentMapper = NotificationContentMapper()

    fun map(
        sessionId: SessionId,
        eventId: EventId,
        roomId: RoomId,
        notificationItem: NotificationItem
    ): NotificationData {
        return notificationItem.use { item ->
            val isDm = isDm(
                isDirect = item.roomInfo.isDirect,
                activeMembersCount = item.roomInfo.joinedMembersCount.toInt(),
            )
            NotificationData(
                sessionId = sessionId,
                eventId = eventId,
                // FIXME once the `NotificationItem` in the SDK returns the thread id
                threadId = null,
                roomId = roomId,
                senderAvatarUrl = item.senderInfo.avatarUrl,
                senderDisplayName = item.senderInfo.displayName,
                senderIsNameAmbiguous = item.senderInfo.isNameAmbiguous,
                roomAvatarUrl = item.roomInfo.avatarUrl ?: item.senderInfo.avatarUrl.takeIf { isDm },
                roomDisplayName = item.roomInfo.displayName,
                isDirect = item.roomInfo.isDirect,
                isDm = isDm,
                isEncrypted = item.roomInfo.isEncrypted.orFalse(),
                isNoisy = item.isNoisy.orFalse(),
                timestamp = item.timestamp() ?: clock.epochMillis(),
                content = item.event.use { notificationContentMapper.map(it) },
                hasMention = item.hasMention.orFalse(),
            )
        }
    }
}

class NotificationContentMapper {
    private val timelineEventToNotificationContentMapper = TimelineEventToNotificationContentMapper()

    fun map(notificationEvent: NotificationEvent): NotificationContent =
        when (notificationEvent) {
            is NotificationEvent.Timeline -> timelineEventToNotificationContentMapper.map(notificationEvent.event)
            is NotificationEvent.Invite -> NotificationContent.Invite(
                senderId = UserId(notificationEvent.sender),
            )
        }
}

private fun NotificationItem.timestamp(): Long? {
    return (this.event as? NotificationEvent.Timeline)?.event?.timestamp()?.toLong()
}
