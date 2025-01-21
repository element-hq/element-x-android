/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import javax.inject.Inject

interface ActiveNotificationsProvider {
    fun getMessageNotificationsForRoom(sessionId: SessionId, roomId: RoomId): List<StatusBarNotification>
    fun getNotificationsForSession(sessionId: SessionId): List<StatusBarNotification>
    fun getMembershipNotificationForSession(sessionId: SessionId): List<StatusBarNotification>
    fun getMembershipNotificationForRoom(sessionId: SessionId, roomId: RoomId): List<StatusBarNotification>
    fun getSummaryNotification(sessionId: SessionId): StatusBarNotification?
    fun count(sessionId: SessionId): Int
}

@ContributesBinding(AppScope::class)
class DefaultActiveNotificationsProvider @Inject constructor(
    private val notificationManager: NotificationManagerCompat,
) : ActiveNotificationsProvider {
    override fun getNotificationsForSession(sessionId: SessionId): List<StatusBarNotification> {
        return notificationManager.activeNotifications.filter { it.notification.group == sessionId.value }
    }

    override fun getMembershipNotificationForSession(sessionId: SessionId): List<StatusBarNotification> {
        val notificationId = NotificationIdProvider.getRoomInvitationNotificationId(sessionId)
        return getNotificationsForSession(sessionId).filter { it.id == notificationId }
    }

    override fun getMessageNotificationsForRoom(sessionId: SessionId, roomId: RoomId): List<StatusBarNotification> {
        val notificationId = NotificationIdProvider.getRoomMessagesNotificationId(sessionId)
        return getNotificationsForSession(sessionId).filter { it.id == notificationId && it.tag == roomId.value }
    }

    override fun getMembershipNotificationForRoom(sessionId: SessionId, roomId: RoomId): List<StatusBarNotification> {
        val notificationId = NotificationIdProvider.getRoomInvitationNotificationId(sessionId)
        return getNotificationsForSession(sessionId).filter { it.id == notificationId && it.tag == roomId.value }
    }

    override fun getSummaryNotification(sessionId: SessionId): StatusBarNotification? {
        val summaryId = NotificationIdProvider.getSummaryNotificationId(sessionId)
        return getNotificationsForSession(sessionId).find { it.id == summaryId }
    }

    override fun count(sessionId: SessionId): Int {
        return getNotificationsForSession(sessionId).size
    }
}
