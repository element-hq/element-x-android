/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.test.notifications

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.notifications.NotificationCleaner
import io.element.android.tests.testutils.lambda.lambdaError

class FakeNotificationCleaner(
    private val clearAllMessagesEventsLambda: (SessionId) -> Unit = { lambdaError() },
    private val clearMessagesForRoomLambda: (SessionId, RoomId) -> Unit = { _, _ -> lambdaError() },
    private val clearEventLambda: (SessionId, EventId) -> Unit = { _, _ -> lambdaError() },
    private val clearMembershipNotificationForSessionLambda: (SessionId) -> Unit = { lambdaError() },
    private val clearMembershipNotificationForRoomLambda: (SessionId, RoomId) -> Unit = { _, _ -> lambdaError() }
) : NotificationCleaner {
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
