/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2021-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.app.Notification
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.notifications.factories.NotificationAccountParams
import io.element.android.libraries.push.impl.notifications.factories.NotificationCreator
import io.element.android.services.toolbox.api.strings.StringProvider

interface SummaryGroupMessageCreator {
    fun createSummaryNotification(
        notificationAccountParams: NotificationAccountParams,
        roomNotifications: List<RoomNotification>,
        invitationNotifications: List<OneShotNotification>,
        simpleNotifications: List<OneShotNotification>,
        fallbackNotifications: List<OneShotNotification>,
    ): Notification
}

/**
 * ======== Build summary notification =========
 * On Android 7.0 (API level 24) and higher, the system automatically builds a summary for
 * your group using snippets of text from each notification. The user can expand this
 * notification to see each separate notification.
 * The behavior of the group summary may vary on some device types such as wearables.
 * To ensure the best experience on all devices and versions, always include a group summary when you create a group
 * https://developer.android.com/training/notify-user/group
 */
@ContributesBinding(AppScope::class)
class DefaultSummaryGroupMessageCreator(
    private val stringProvider: StringProvider,
    private val notificationCreator: NotificationCreator,
) : SummaryGroupMessageCreator {
    override fun createSummaryNotification(
        notificationAccountParams: NotificationAccountParams,
        roomNotifications: List<RoomNotification>,
        invitationNotifications: List<OneShotNotification>,
        simpleNotifications: List<OneShotNotification>,
        fallbackNotifications: List<OneShotNotification>,
    ): Notification {
        val summaryIsNoisy = roomNotifications.any { it.shouldBing } ||
            invitationNotifications.any { it.isNoisy } ||
            simpleNotifications.any { it.isNoisy }
        val lastMessageTimestamp = roomNotifications.lastOrNull()?.latestTimestamp
            ?: invitationNotifications.lastOrNull()?.timestamp
            ?: simpleNotifications.last().timestamp
        val nbEvents = roomNotifications.size + invitationNotifications.size + simpleNotifications.size
        val sumTitle = stringProvider.getQuantityString(R.plurals.notification_compat_summary_title, nbEvents, nbEvents)
        return notificationCreator.createSummaryListNotification(
            notificationAccountParams = notificationAccountParams,
            sumTitle,
            noisy = summaryIsNoisy,
            lastMessageTimestamp = lastMessageTimestamp,
        )
    }
}
