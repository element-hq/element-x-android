/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.notification

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.notification.NotificationData
import io.element.android.libraries.matrix.api.notification.NotificationService

class FakeNotificationService : NotificationService {
    private var getNotificationResult: Result<NotificationData?> = Result.success(null)

    fun givenGetNotificationResult(result: Result<NotificationData?>) {
        getNotificationResult = result
    }

    override suspend fun getNotification(
        roomId: RoomId,
        eventId: EventId,
    ): Result<NotificationData?> {
        return getNotificationResult
    }
}
