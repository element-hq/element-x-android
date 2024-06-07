/*
 * Copyright (c) 2021 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.push.impl.notifications

import android.app.Notification
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.notifications.factories.NotificationCreator
import io.element.android.services.toolbox.api.strings.StringProvider
import javax.inject.Inject

interface SummaryGroupMessageCreator {
    fun createSummaryNotification(
        currentUser: MatrixUser,
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
class DefaultSummaryGroupMessageCreator @Inject constructor(
    private val stringProvider: StringProvider,
    private val notificationCreator: NotificationCreator,
) : SummaryGroupMessageCreator {
    override fun createSummaryNotification(
        currentUser: MatrixUser,
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

        // FIXME roomIdToEventMap.size is not correct, this is the number of rooms
        val nbEvents = roomNotifications.size + simpleNotifications.size
        val sumTitle = stringProvider.getQuantityString(R.plurals.notification_compat_summary_title, nbEvents, nbEvents)
        return notificationCreator.createSummaryListNotification(
            currentUser,
            sumTitle,
            noisy = summaryIsNoisy,
            lastMessageTimestamp = lastMessageTimestamp
        )
    }
}
