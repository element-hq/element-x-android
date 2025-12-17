/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.BatchNotificationResult
import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.NotificationClient
import org.matrix.rustcomponents.sdk.NotificationItemsRequest

class FakeFfiNotificationClient(
    var notificationItemResult: Map<String, BatchNotificationResult> = emptyMap(),
    val closeResult: () -> Unit = { }
) : NotificationClient(NoHandle) {
    override suspend fun getNotifications(requests: List<NotificationItemsRequest>): Map<String, BatchNotificationResult> {
        return notificationItemResult
    }

    override fun close() = closeResult()
}
