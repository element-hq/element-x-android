/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.api

import io.element.android.services.analyticsproviders.api.AnalyticsTransactions
import io.element.android.services.analyticsproviders.api.TransactionDefinition

sealed class AnalyticsLongRunningTransaction(
    val name: String,
    val operation: String? = null,
    val description: String? = null,
) {
    constructor(definition: TransactionDefinition) : this(definition.name, definition.operation, definition.description)

    // UX flows
    data object ColdStart : AnalyticsLongRunningTransaction(AnalyticsTransactions.coldStart)
    data object CatchUp : AnalyticsLongRunningTransaction(AnalyticsTransactions.catchUp)
    data object NotificationToMessage : AnalyticsLongRunningTransaction(AnalyticsTransactions.notificationToMessage)
    data object OpenRoom : AnalyticsLongRunningTransaction(AnalyticsTransactions.openRoom)

    // Technical flows
    data object FirstRoomsDisplayed : AnalyticsLongRunningTransaction("First rooms displayed after login or restoration", null, null)
    data object LoadJoinedRoomFlow : AnalyticsLongRunningTransaction("Load joined room UI", "ui.load")
    data object LoadMessagesUi : AnalyticsLongRunningTransaction("Load messages UI", "ui.load")
    data object DisplayFirstTimelineItems : AnalyticsLongRunningTransaction("Get and display first timeline items", null)
    data class PushToNotification(val eventId: String) : AnalyticsLongRunningTransaction(AnalyticsTransactions.pushToNotification)
    data class PushToWorkManager(val eventId: String) : AnalyticsLongRunningTransaction("Push to WorkManager")
}
