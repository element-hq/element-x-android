/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import io.element.android.libraries.push.impl.notifications.factories.NotificationCreator
import timber.log.Timber

interface ActiveNotificationsProvider {
    /**
     * Gets the displayed notifications for the combination of [sessionId], [roomId] and [threadId].
     */
    fun getMessageNotificationsForRoom(sessionId: SessionId, roomId: RoomId, threadId: ThreadId?): List<StatusBarNotification>

    /**
     * Gets all displayed notifications associated to [sessionId] and [roomId]. These will include all thread notifications as well.
     */
    fun getAllMessageNotificationsForRoom(sessionId: SessionId, roomId: RoomId): List<StatusBarNotification>
    fun getNotificationsForSession(sessionId: SessionId): List<StatusBarNotification>
    fun getMembershipNotificationForSession(sessionId: SessionId): List<StatusBarNotification>
    fun getMembershipNotificationForRoom(sessionId: SessionId, roomId: RoomId): List<StatusBarNotification>
    fun getSummaryNotification(sessionId: SessionId): StatusBarNotification?
    fun count(sessionId: SessionId): Int
}

@ContributesBinding(AppScope::class)
class DefaultActiveNotificationsProvider(
    private val notificationManager: NotificationManagerCompat,
) : ActiveNotificationsProvider {
    override fun getNotificationsForSession(sessionId: SessionId): List<StatusBarNotification> {
        return runCatchingExceptions { notificationManager.activeNotifications }
            .onFailure {
                Timber.e(it, "Failed to get active notifications")
            }
            .getOrElse { emptyList() }
            .filter { it.notification.group == sessionId.value }
    }

    override fun getMembershipNotificationForSession(sessionId: SessionId): List<StatusBarNotification> {
        val notificationId = NotificationIdProvider.getRoomInvitationNotificationId(sessionId)
        return getNotificationsForSession(sessionId).filter { it.id == notificationId }
    }

    override fun getMessageNotificationsForRoom(sessionId: SessionId, roomId: RoomId, threadId: ThreadId?): List<StatusBarNotification> {
        val notificationId = NotificationIdProvider.getRoomMessagesNotificationId(sessionId)
        val expectedTag = NotificationCreator.messageTag(roomId, threadId)
        return getNotificationsForSession(sessionId).filter { it.id == notificationId && it.tag == expectedTag }
    }

    override fun getAllMessageNotificationsForRoom(sessionId: SessionId, roomId: RoomId): List<StatusBarNotification> {
        val notificationId = NotificationIdProvider.getRoomMessagesNotificationId(sessionId)
        return getNotificationsForSession(sessionId).filter { it.id == notificationId && it.tag.startsWith(roomId.value) }
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
