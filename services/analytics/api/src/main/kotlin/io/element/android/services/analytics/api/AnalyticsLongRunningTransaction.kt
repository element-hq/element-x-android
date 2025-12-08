/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.api

sealed class AnalyticsLongRunningTransaction(
    val name: String,
    val operation: String?,
) {
    data object ColdStartUntilCachedRoomList : AnalyticsLongRunningTransaction("Cold start until cached room list is displayed", null)
    data object FirstRoomsDisplayed : AnalyticsLongRunningTransaction("First rooms displayed after login or restoration", null)
    data object ResumeAppUntilNewRoomsReceived : AnalyticsLongRunningTransaction("App was resumed and new room list items arrived", null)
    data object NotificationTapOpensTimeline : AnalyticsLongRunningTransaction("A notification was tapped and it opened a timeline", null)
    data object OpenRoom : AnalyticsLongRunningTransaction("Open a room and see loaded items in the timeline", null)
    data object LoadJoinedRoomFlow : AnalyticsLongRunningTransaction("Load joined room UI", "ui.load")
    data object LoadMessagesUi : AnalyticsLongRunningTransaction("Load messages UI", "ui.load")
    data object DisplayFirstTimelineItems : AnalyticsLongRunningTransaction("Get and display first timeline items", null)
}
