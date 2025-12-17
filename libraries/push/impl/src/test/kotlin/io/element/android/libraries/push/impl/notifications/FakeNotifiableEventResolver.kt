/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.push.NotificationEventRequest
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import io.element.android.tests.testutils.lambda.lambdaError

class FakeNotifiableEventResolver(
    private val resolveEventsResult: (SessionId, List<NotificationEventRequest>) -> Result<Map<NotificationEventRequest, Result<ResolvedPushEvent>>> =
        { _, _ -> lambdaError() }
) : NotifiableEventResolver {
    override suspend fun resolveEvents(
        sessionId: SessionId,
        notificationEventRequests: List<NotificationEventRequest>
    ): Result<Map<NotificationEventRequest, Result<ResolvedPushEvent>>> {
        return resolveEventsResult(sessionId, notificationEventRequests)
    }
}
