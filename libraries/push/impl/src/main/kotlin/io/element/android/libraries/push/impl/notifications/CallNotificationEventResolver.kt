/*
 * Copyright (c) 2024 New Vector Ltd
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
    fun resolveEvent(sessionId: SessionId, notificationData: NotificationData, forceNotify: Boolean = false): NotifiableEvent?
}

@ContributesBinding(AppScope::class)
class DefaultCallNotificationEventResolver @Inject constructor(
    private val stringProvider: StringProvider,
) : CallNotificationEventResolver {
    override fun resolveEvent(sessionId: SessionId, notificationData: NotificationData, forceNotify: Boolean): NotifiableEvent? {
        val content = notificationData.content as? NotificationContent.MessageLike.CallNotify ?: return null
        return notificationData.run {
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
                // Create a simple message notification event
                buildNotifiableMessageEvent(
                    sessionId = sessionId,
                    senderId = content.senderId,
                    roomId = roomId,
                    eventId = eventId,
                    noisy = true,
                    timestamp = this.timestamp,
                    senderDisambiguatedDisplayName = getDisambiguatedDisplayName(content.senderId),
                    body = "☎️ ${stringProvider.getString(R.string.notification_incoming_call)}",
                    roomName = roomDisplayName,
                    roomIsDirect = isDirect,
                    roomAvatarPath = roomAvatarUrl,
                    senderAvatarPath = senderAvatarUrl,
                    type = EventType.CALL_NOTIFY,
                )
            }
        }
    }
}
