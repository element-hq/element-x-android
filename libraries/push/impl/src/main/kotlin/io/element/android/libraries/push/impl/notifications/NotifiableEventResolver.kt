/*
 * Copyright (c) 2023 New Vector Ltd
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

import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.notification.NotificationContent
import io.element.android.libraries.matrix.api.notification.NotificationData
import io.element.android.libraries.matrix.api.notification.NotificationEvent
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MessageType
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.log.pushLoggerTag
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.toolbox.api.strings.StringProvider
import io.element.android.services.toolbox.api.systemclock.SystemClock
import timber.log.Timber
import javax.inject.Inject

private val loggerTag = LoggerTag("NotifiableEventResolver", pushLoggerTag)

/**
 * The notifiable event resolver is able to create a NotifiableEvent (view model for notifications) from an sdk Event.
 * It is used as a bridge between the Event Thread and the NotificationDrawerManager.
 * The NotifiableEventResolver is the only aware of session/store, the NotificationDrawerManager has no knowledge of that,
 * this pattern allow decoupling between the object responsible of displaying notifications and the matrix sdk.
 */
class NotifiableEventResolver @Inject constructor(
    private val stringProvider: StringProvider,
    // private val noticeEventFormatter: NoticeEventFormatter,
    // private val displayableEventFormatter: DisplayableEventFormatter,
    private val matrixAuthenticationService: MatrixAuthenticationService,
    private val buildMeta: BuildMeta,
    private val clock: SystemClock,
) {

    suspend fun resolveEvent(sessionId: SessionId, roomId: RoomId, eventId: EventId): NotifiableEvent? {
        // Restore session
        val session = matrixAuthenticationService.restoreSession(sessionId).getOrNull() ?: return null
        // TODO EAx, no need for a session?
        val notificationService = session.notificationService()
        val notificationData = notificationService.getNotification(
                userId = sessionId,
                roomId = roomId,
                eventId = eventId,
                filterByPushRules = true,
            ).recover {
                Timber.tag(loggerTag.value).e(it, "Unable to resolve event.")
                null
            }.getOrNull()

        val notifiableEvent = notificationData?.asNotifiableEvent(sessionId)

        return if (notifiableEvent?.description != null) {
            notifiableEvent
        } else {
            fallbackNotifiableEvent(sessionId, roomId, eventId)
        }
    }

    private fun NotificationData.asNotifiableEvent(userId: SessionId): NotifiableEvent? {
        return when (val content = this.event.content) {
            is NotificationContent.MessageLike.RoomMessage -> {
                return NotifiableMessageEvent(
                    sessionId = userId,
                    roomId = roomId,
                    eventId = eventId,
                    editedEventId = null,
                    canBeReplaced = true,
                    noisy = isNoisy,
                    timestamp = event.timestamp,
                    senderName = senderDisplayName,
                    senderId = senderId.value,
                    body = messageFromNotificationContent(content, isDirect),
                    imageUriString = event.contentUrl,
                    threadId = null,
                    roomName = roomDisplayName,
                    roomIsDirect = isDirect,
                    roomAvatarPath = roomAvatarUrl,
                    senderAvatarPath = senderAvatarUrl,
                    soundName = null,
                    outGoingMessage = false,
                    outGoingMessageFailed = false,
                    isRedacted = false,
                    isUpdated = false
                )
            }
            is NotificationContent.StateEvent.RoomMemberContent -> {
                if (content.membershipState == RoomMembershipState.INVITE) {
                    return InviteNotifiableEvent(
                        sessionId = userId,
                        roomId = roomId,
                        eventId = eventId,
                        editedEventId = null,
                        canBeReplaced = true,
                        roomName = roomDisplayName,
                        noisy = isNoisy,
                        timestamp = event.timestamp,
                        soundName = null,
                        isRedacted = false,
                        isUpdated = false,
                        description = messageFromRoomMembershipContent(content, isDirect) ?: return null,
                        type = null, // TODO: find proper value
                        title = "Invitation", // TODO: find proper value
                    )
                } else {
                    null
                }
            }
            else -> null
        }
    }

    private fun fallbackNotifiableEvent(userId: SessionId, roomId: RoomId, eventId: EventId) = NotifiableMessageEvent(
        sessionId = userId,
        roomId = roomId,
        eventId = eventId,
        editedEventId = null,
        canBeReplaced = true,
        noisy = false,
        timestamp = clock.epochMillis(),
        soundName = null,
        isRedacted = false,
        isUpdated = false,
        body = "Notification", // TODO: find proper value
        imageUriString = null,
        threadId = null,
        roomName = null,
        roomIsDirect = false,
        roomAvatarPath = null,
        senderName = null,
        senderAvatarPath = null,
        senderId = null,
    )

    private fun messageFromNotificationContent(
        notificationContent: NotificationContent,
        isDirectRoom: Boolean
    ): String? {
        return when (notificationContent) {
            is NotificationContent.MessageLike.RoomMessage -> notificationContent.messageType.toNotificationMessage()
            is NotificationContent.StateEvent.RoomMemberContent -> messageFromRoomMembershipContent(notificationContent, isDirectRoom)
            else -> null
        }
    }

    private fun messageFromRoomMembershipContent(
        content: NotificationContent.StateEvent.RoomMemberContent,
        isDirectRoom: Boolean
    ): String? {
        return when (content.membershipState) {
            RoomMembershipState.INVITE -> {
                if (isDirectRoom) {
                    stringProvider.getString(R.string.notification_invite_body)
                } else {
                    stringProvider.getString(R.string.notification_room_invite_body)
                }
            }
            else -> null
        }
    }

    private fun MessageType.toNotificationMessage(): String {
        return when (this) {
            is AudioMessageType -> body
            is EmoteMessageType -> body
            is FileMessageType -> body
            is ImageMessageType -> body
            is NoticeMessageType -> body
            is TextMessageType -> body
            is VideoMessageType -> body
            is UnknownMessageType -> stringProvider.getString(CommonStrings.common_unsupported_event)
        }
    }
}
