/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.notification.NotificationContent
import io.element.android.libraries.matrix.api.notification.NotificationData
import io.element.android.libraries.matrix.api.timeline.item.event.EventType
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableRingingCallEvent
import io.element.android.services.toolbox.api.strings.StringProvider
import timber.log.Timber
import javax.inject.Inject

/**
 * Helper to resolve a valid [NotifiableEvent] from a [NotificationData].
 */
interface CallNotificationEventResolver {
    /**
     * Resolve a call notification event from a notification data depending on whether it should be a ringing one or not.
     * @param sessionId the current session id
     * @param notificationData the notification data
     * @param forceNotify `true` to force the notification to be non-ringing, `false` to use the default behaviour. Default is `false`.
     * @return a [NotifiableEvent] if the notification data is a call notification, null otherwise
     */
    fun resolveEvent(
        sessionId: SessionId,
        notificationData: NotificationData,
        forceNotify: Boolean = false,
    ): Result<NotifiableEvent>
}

@ContributesBinding(AppScope::class)
class DefaultCallNotificationEventResolver @Inject constructor(
    private val stringProvider: StringProvider,
) : CallNotificationEventResolver {
    override fun resolveEvent(
        sessionId: SessionId,
        notificationData: NotificationData,
        forceNotify: Boolean
    ): Result<NotifiableEvent> = runCatching {
        val content = notificationData.content as? NotificationContent.MessageLike.CallNotify
            ?: throw ResolvingException("content is not a call notify")

        notificationData.run {
            if (NotifiableRingingCallEvent.shouldRing(content.type, timestamp) && !forceNotify) {
                NotifiableRingingCallEvent(
                    sessionId = sessionId,
                    roomId = roomId,
                    eventId = eventId,
                    roomName = roomDisplayName,
                    editedEventId = null,
                    canBeReplaced = true,
                    timestamp = this.timestamp,
                    isRedacted = false,
                    isUpdated = false,
                    description = stringProvider.getString(R.string.notification_incoming_call),
                    senderDisambiguatedDisplayName = getDisambiguatedDisplayName(content.senderId),
                    roomAvatarUrl = roomAvatarUrl,
                    callNotifyType = content.type,
                    senderId = content.senderId,
                    senderAvatarUrl = senderAvatarUrl,
                )
            } else {
                val now = System.currentTimeMillis()
                val elapsed = now - timestamp
                Timber.d("Event $eventId is call notify but should not ring: $timestamp vs $now ($elapsed ms elapsed), notify: ${content.type}")
                // Create a simple message notification event
                buildNotifiableMessageEvent(
                    sessionId = sessionId,
                    senderId = content.senderId,
                    roomId = roomId,
                    eventId = eventId,
                    noisy = true,
                    timestamp = this.timestamp,
                    senderDisambiguatedDisplayName = getDisambiguatedDisplayName(content.senderId),
                    body = stringProvider.getString(R.string.notification_incoming_call),
                    roomName = roomDisplayName,
                    roomIsDm = isDm,
                    roomAvatarPath = roomAvatarUrl,
                    senderAvatarPath = senderAvatarUrl,
                    type = EventType.CALL_NOTIFY,
                )
            }
        }
    }
}
