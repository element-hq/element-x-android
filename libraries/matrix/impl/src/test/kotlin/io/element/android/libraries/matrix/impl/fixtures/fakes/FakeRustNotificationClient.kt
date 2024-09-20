/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import io.element.android.tests.testutils.simulateLongTask
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.NotificationClient
import org.matrix.rustcomponents.sdk.NotificationItem

class FakeRustNotificationClient(
    var notificationItemResult: NotificationItem? = null
) : NotificationClient(NoPointer) {
    override suspend fun getNotification(roomId: String, eventId: String): NotificationItem? = simulateLongTask {
        notificationItemResult
    }
}
