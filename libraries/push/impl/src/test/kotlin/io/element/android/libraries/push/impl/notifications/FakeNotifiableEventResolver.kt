/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import io.element.android.tests.testutils.lambda.lambdaError

class FakeNotifiableEventResolver(
    private val notifiableEventResult: (SessionId, RoomId, EventId) -> Result<ResolvedPushEvent> = { _, _, _ -> lambdaError() }
) : NotifiableEventResolver {
    override suspend fun resolveEvents(sessionId: SessionId, notificationEventRequests: List<NotificationEventRequest>): Result<Map<EventId, ResolvedPushEvent?>> {
        return notificationEventRequests.associate {
            val eventId = it.eventId
            val roomId = it.roomId
            eventId to notifiableEventResult(sessionId, roomId, eventId).getOrNull()
        }.let { resolvedEvents ->
            Result.success(resolvedEvents)
        }
    }
}
