/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import io.element.android.tests.testutils.lambda.lambdaError

class FakeNotifiableEventResolver(
    private val notifiableEventResult: (SessionId, RoomId, EventId) -> ResolvedPushEvent? = { _, _, _ -> lambdaError() }
) : NotifiableEventResolver {
    override suspend fun resolveEvent(sessionId: SessionId, roomId: RoomId, eventId: EventId): ResolvedPushEvent? {
        return notifiableEventResult(sessionId, roomId, eventId)
    }
}
