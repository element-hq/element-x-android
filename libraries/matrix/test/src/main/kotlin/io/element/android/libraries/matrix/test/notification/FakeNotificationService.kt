/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.notification

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.notification.NotificationData
import io.element.android.libraries.matrix.api.notification.NotificationService

class FakeNotificationService : NotificationService {
    private var getNotificationsResult: Result<Map<EventId, Result<NotificationData>>> = Result.success(emptyMap())

    fun givenGetNotificationsResult(result: Result<Map<EventId, Result<NotificationData>>>) {
        getNotificationsResult = result
    }

    override suspend fun getNotifications(ids: Map<RoomId, List<EventId>>): Result<Map<EventId, Result<NotificationData>>> {
        return getNotificationsResult
    }
}
