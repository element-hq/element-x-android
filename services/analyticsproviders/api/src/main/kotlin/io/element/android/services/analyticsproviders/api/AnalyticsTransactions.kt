/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.api

object AnalyticsTransactions {
    val coldStart = TransactionDefinition(
        name = "Cached room list",
        operation = "ux",
        description = "Cold start until the cached room list is displayed",
    )

    val catchUp = TransactionDefinition(
        name = "Up-to-date room list",
        operation = "ux",
        description = "The app syncs and the room list becomes up-to-date",
    )

    val notificationToMessage = TransactionDefinition(
        name = "Notification to message",
        operation = "ux",
        description = "A notification was tapped and it opened a timeline",
    )

    val openRoom = TransactionDefinition(
        name = "Open a room",
        operation = "ux",
        description = "Open a room and see loaded items in the timeline",
    )

    val sendMessage = TransactionDefinition(
        name = "Send a message",
        operation = "ux",
        description = "Send to sent state in timeline",
    )
}

data class TransactionDefinition(
    val name: String,
    val operation: String? = null,
    val description: String?,
)
