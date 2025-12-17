/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import android.content.Context
import android.graphics.Typeface
import android.text.style.StyleSpan
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.MessagingStyle
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.push.impl.notifications.ActiveNotificationsProvider
import io.element.android.libraries.push.impl.notifications.NotificationDisplayer
import io.element.android.libraries.push.impl.notifications.factories.DefaultNotificationCreator
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.toolbox.api.strings.StringProvider
import timber.log.Timber

interface OnRedactedEventReceived {
    suspend fun onRedactedEventsReceived(redactions: List<ResolvedPushEvent.Redaction>)
}

@ContributesBinding(AppScope::class)
class DefaultOnRedactedEventReceived(
    private val activeNotificationsProvider: ActiveNotificationsProvider,
    private val notificationDisplayer: NotificationDisplayer,
    @ApplicationContext private val context: Context,
    private val stringProvider: StringProvider,
) : OnRedactedEventReceived {
    override suspend fun onRedactedEventsReceived(redactions: List<ResolvedPushEvent.Redaction>) {
        val redactionsBySessionIdAndRoom = redactions.groupBy { redaction ->
            redaction.sessionId to redaction.roomId
        }
        for ((keys, roomRedactions) in redactionsBySessionIdAndRoom) {
            val (sessionId, roomId) = keys
            // Get all notifications for the room, including those for threads
            val notifications = activeNotificationsProvider.getAllMessageNotificationsForRoom(sessionId, roomId)
            if (notifications.isEmpty()) {
                Timber.d("No notifications found for redacted event")
            }
            notifications.forEach { statusBarNotification ->
                val notification = statusBarNotification.notification
                val messagingStyle = MessagingStyle.extractMessagingStyleFromNotification(notification)
                if (messagingStyle == null) {
                    Timber.w("Unable to retrieve messaging style from notification")
                    return@forEach
                }
                val messageToRedactIndex = messagingStyle.messages.indexOfFirst { message ->
                    roomRedactions.any { it.redactedEventId.value == message.extras.getString(DefaultNotificationCreator.MESSAGE_EVENT_ID) }
                }
                if (messageToRedactIndex == -1) {
                    Timber.d("Unable to find the message to remove from notification")
                    return@forEach
                }
                val oldMessage = messagingStyle.messages[messageToRedactIndex]
                val content = buildSpannedString {
                    inSpans(StyleSpan(Typeface.ITALIC)) {
                        append(stringProvider.getString(CommonStrings.common_message_removed))
                    }
                }
                val newMessage = MessagingStyle.Message(
                    content,
                    oldMessage.timestamp,
                    oldMessage.person
                )
                messagingStyle.messages[messageToRedactIndex] = newMessage
                notificationDisplayer.showNotification(
                    statusBarNotification.tag,
                    statusBarNotification.id,
                    NotificationCompat.Builder(context, notification)
                        .setStyle(messagingStyle)
                        .build()
                )
            }
        }
    }
}
