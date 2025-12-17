/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.test.notifications

import io.element.android.libraries.push.api.push.NotificationEventRequest
import io.element.android.libraries.push.impl.notifications.NotificationResolverQueue
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeNotificationResolverQueue(
    private val processingLambda: suspend (NotificationEventRequest) -> Result<ResolvedPushEvent>,
) : NotificationResolverQueue {
    override val results = MutableSharedFlow<Pair<List<NotificationEventRequest>, Map<NotificationEventRequest, Result<ResolvedPushEvent>>>>(replay = 1)

    override suspend fun enqueue(request: NotificationEventRequest) {
        results.emit(listOf(request) to mapOf(request to processingLambda(request)))
    }
}
