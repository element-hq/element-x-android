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
