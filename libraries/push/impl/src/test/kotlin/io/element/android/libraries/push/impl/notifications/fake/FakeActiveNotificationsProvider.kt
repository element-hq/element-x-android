/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.fake

import android.service.notification.StatusBarNotification
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.impl.notifications.ActiveNotificationsProvider

class FakeActiveNotificationsProvider(
    private val getMessageNotificationsForRoomResult: (SessionId, RoomId) -> List<StatusBarNotification> = { _, _ -> emptyList() },
    private val getNotificationsForSessionResult: (SessionId) -> List<StatusBarNotification> = { emptyList() },
    private val getMembershipNotificationForSessionResult: (SessionId) -> List<StatusBarNotification> = { emptyList() },
    private val getMembershipNotificationForRoomResult: (SessionId, RoomId) -> List<StatusBarNotification> = { _, _ -> emptyList() },
    private val getSummaryNotificationResult: (SessionId) -> StatusBarNotification? = { null },
    private val countResult: (SessionId) -> Int = { 0 },
) : ActiveNotificationsProvider {
    override fun getMessageNotificationsForRoom(sessionId: SessionId, roomId: RoomId): List<StatusBarNotification> {
        return getMessageNotificationsForRoomResult(sessionId, roomId)
    }

    override fun getNotificationsForSession(sessionId: SessionId): List<StatusBarNotification> {
        return getNotificationsForSessionResult(sessionId)
    }

    override fun getMembershipNotificationForSession(sessionId: SessionId): List<StatusBarNotification> {
        return getMembershipNotificationForSessionResult(sessionId)
    }

    override fun getMembershipNotificationForRoom(sessionId: SessionId, roomId: RoomId): List<StatusBarNotification> {
        return getMembershipNotificationForRoomResult(sessionId, roomId)
    }

    override fun getSummaryNotification(sessionId: SessionId): StatusBarNotification? {
        return getSummaryNotificationResult(sessionId)
    }

    override fun count(sessionId: SessionId): Int {
        return countResult(sessionId)
    }
}
