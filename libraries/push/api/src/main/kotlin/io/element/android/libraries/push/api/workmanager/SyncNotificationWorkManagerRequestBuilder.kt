/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api.workmanager

import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.push.NotificationEventRequest
import io.element.android.libraries.workmanager.api.WorkManagerRequestBuilder

interface SyncNotificationWorkManagerRequestBuilder : WorkManagerRequestBuilder {
    fun interface Factory {
        fun create(sessionId: SessionId, notificationEventRequests: List<NotificationEventRequest>): SyncNotificationWorkManagerRequestBuilder
    }
}
