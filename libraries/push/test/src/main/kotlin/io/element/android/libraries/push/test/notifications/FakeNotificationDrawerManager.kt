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

package io.element.android.libraries.push.test.notifications

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.notifications.NotificationDrawerManager

class FakeNotificationDrawerManager : NotificationDrawerManager {
    private val clearMemberShipNotificationForSessionCallsCount = mutableMapOf<String, Int>()
    private val clearMemberShipNotificationForRoomCallsCount = mutableMapOf<String, Int>()

    override fun clearAllMessagesEvents(sessionId: SessionId) {
    }

    override fun clearMessagesForRoom(sessionId: SessionId, roomId: RoomId) {
    }

    override fun clearEvent(sessionId: SessionId, eventId: EventId) {
    }

    override fun clearMembershipNotificationForSession(sessionId: SessionId) {
        clearMemberShipNotificationForSessionCallsCount.merge(sessionId.value, 1) { oldValue, value -> oldValue + value }
    }

    override fun clearMembershipNotificationForRoom(sessionId: SessionId, roomId: RoomId) {
        val key = getMembershipNotificationKey(sessionId, roomId)
        clearMemberShipNotificationForRoomCallsCount.merge(key, 1) { oldValue, value -> oldValue + value }
    }

    fun getClearMembershipNotificationForSessionCount(sessionId: SessionId): Int {
        return clearMemberShipNotificationForRoomCallsCount[sessionId.value] ?: 0
    }

    fun getClearMembershipNotificationForRoomCount(sessionId: SessionId, roomId: RoomId): Int {
        val key = getMembershipNotificationKey(sessionId, roomId)
        return clearMemberShipNotificationForRoomCallsCount[key] ?: 0
    }

    private fun getMembershipNotificationKey(sessionId: SessionId, roomId: RoomId): String {
        return "$sessionId-$roomId"
    }
}
