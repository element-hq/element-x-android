/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.NotificationClient
import org.matrix.rustcomponents.sdk.NotificationItem
import org.matrix.rustcomponents.sdk.NotificationItemsRequest

class FakeFfiNotificationClient(
    var notificationItemResult: Map<String, NotificationItem> = emptyMap(),
    val closeResult: () -> Unit = { }
) : NotificationClient(NoPointer) {
    override suspend fun getNotifications(requests: List<NotificationItemsRequest>): Map<String, NotificationItem> {
        return notificationItemResult
    }

    override fun close() = closeResult()
}
