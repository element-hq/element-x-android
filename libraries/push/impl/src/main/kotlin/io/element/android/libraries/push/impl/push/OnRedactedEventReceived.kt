/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import android.content.Context
import android.graphics.Typeface
import android.text.style.StyleSpan
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.MessagingStyle
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.push.impl.notifications.ActiveNotificationsProvider
import io.element.android.libraries.push.impl.notifications.NotificationDisplayer
import io.element.android.libraries.push.impl.notifications.factories.DefaultNotificationCreator
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.toolbox.api.strings.StringProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

interface OnRedactedEventReceived {
    fun onRedactedEventReceived(redaction: ResolvedPushEvent.Redaction)
}

@ContributesBinding(AppScope::class)
class DefaultOnRedactedEventReceived @Inject constructor(
    private val activeNotificationsProvider: ActiveNotificationsProvider,
    private val notificationDisplayer: NotificationDisplayer,
    private val coroutineScope: CoroutineScope,
    @ApplicationContext private val context: Context,
    private val stringProvider: StringProvider,
) : OnRedactedEventReceived {
    override fun onRedactedEventReceived(redaction: ResolvedPushEvent.Redaction) {
        coroutineScope.launch {
            val notifications = activeNotificationsProvider.getMessageNotificationsForRoom(
                redaction.sessionId,
                redaction.roomId,
            )
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
                    message.extras.getString(DefaultNotificationCreator.MESSAGE_EVENT_ID) == redaction.redactedEventId.value
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
                notificationDisplayer.showNotificationMessage(
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
