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
import io.element.android.tests.testutils.lambda.lambdaError

class FakeNotificationDrawerManager(
    private val clearAllMessagesEventsLambda: (SessionId) -> Unit = { lambdaError() },
    private val clearMessagesForRoomLambda: (SessionId, RoomId) -> Unit = { _, _ -> lambdaError() },
    private val clearEventLambda: (SessionId, EventId) -> Unit = { _, _ -> lambdaError() },
    private val clearMembershipNotificationForSessionLambda: (SessionId) -> Unit = { lambdaError() },
    private val clearMembershipNotificationForRoomLambda: (SessionId, RoomId) -> Unit = { _, _ -> lambdaError() }
) : NotificationDrawerManager {
    override fun clearAllMessagesEvents(sessionId: SessionId) {
        clearAllMessagesEventsLambda(sessionId)
    }

    override fun clearMessagesForRoom(sessionId: SessionId, roomId: RoomId) {
        clearMessagesForRoomLambda(sessionId, roomId)
    }

    override fun clearEvent(sessionId: SessionId, eventId: EventId) {
        clearEventLambda(sessionId, eventId)
    }

    override fun clearMembershipNotificationForSession(sessionId: SessionId) {
        clearMembershipNotificationForSessionLambda(sessionId)
    }

    override fun clearMembershipNotificationForRoom(sessionId: SessionId, roomId: RoomId) {
        clearMembershipNotificationForRoomLambda(sessionId, roomId)
    }
}
