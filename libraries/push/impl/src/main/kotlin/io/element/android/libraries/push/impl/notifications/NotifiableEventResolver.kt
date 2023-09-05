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
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.notification.NotificationContent
import io.element.android.libraries.matrix.api.notification.NotificationData
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.sync.StartSyncReason
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.LocationMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.log.pushLoggerTag
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.toolbox.api.strings.StringProvider
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.delay
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
    private val clock: SystemClock,
    private val matrixClientProvider: MatrixClientProvider,
) {

    suspend fun resolveEvent(sessionId: SessionId, roomId: RoomId, eventId: EventId): NotifiableEvent? {
        // Restore session
        val client = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return null
        val notificationService = client.notificationService()

        // Restart the sync service to ensure that the crypto sync handle the toDevice Events.
        client.syncService().startSync(StartSyncReason.Notification(roomId, eventId))
        // Wait for toDevice Event to be processed
        // FIXME This delay can be removed when the Rust SDK will handle internal retry to get
        //  clear notification content.
        delay(300)

        val notificationData = notificationService.getNotification(
            userId = sessionId,
            roomId = roomId,
            eventId = eventId,
        ).onFailure {
            Timber.tag(loggerTag.value).e(it, "Unable to resolve event: $eventId.")
        }.getOrNull()

        client.syncService().stopSync(StartSyncReason.Notification(roomId, eventId))

        // TODO this notificationData is not always valid at the moment, sometimes the Rust SDK can't fetch the matching event
        return notificationData?.asNotifiableEvent(sessionId)
    }

    private fun NotificationData.asNotifiableEvent(userId: SessionId): NotifiableEvent? {
        return when (val content = this.content) {
            is NotificationContent.MessageLike.RoomMessage -> {
                buildNotifiableMessageEvent(
                    sessionId = userId,
                    senderId = content.senderId,
                    roomId = roomId,
                    eventId = eventId,
                    noisy = isNoisy,
                    timestamp = this.timestamp,
                    senderName = senderDisplayName,
                    body = descriptionFromMessageContent(content),
                    imageUriString = this.contentUrl,
                    roomName = roomDisplayName,
                    roomIsDirect = isDirect,
                    roomAvatarPath = roomAvatarUrl,
                    senderAvatarPath = senderAvatarUrl,
                )
            }
            is NotificationContent.StateEvent.RoomMemberContent -> {
                if (content.membershipState == RoomMembershipState.INVITE) {
                    InviteNotifiableEvent(
                        sessionId = userId,
                        roomId = roomId,
                        eventId = eventId,
                        editedEventId = null,
                        canBeReplaced = true,
                        roomName = roomDisplayName,
                        noisy = isNoisy,
                        timestamp = this.timestamp,
                        soundName = null,
                        isRedacted = false,
                        isUpdated = false,
                        description = descriptionFromRoomMembershipContent(content, isDirect) ?: return null,
                        type = null, // TODO check if type is needed anymore
                        title = null, // TODO check if title is needed anymore
                    )
                } else {
                    fallbackNotifiableEvent(userId, roomId, eventId)
                }
            }
            else -> fallbackNotifiableEvent(userId, roomId, eventId)
        }
    }

    private fun fallbackNotifiableEvent(
        userId: SessionId,
        roomId: RoomId,
        eventId: EventId
    ) = FallbackNotifiableEvent(
        sessionId = userId,
        roomId = roomId,
        eventId = eventId,
        editedEventId = null,
        canBeReplaced = true,
        isRedacted = false,
        isUpdated = false,
        timestamp = clock.epochMillis(),
        description = stringProvider.getString(R.string.notification_fallback_content),
    )

    private fun descriptionFromMessageContent(
        content: NotificationContent.MessageLike.RoomMessage,
    ): String {
        return when (val messageType = content.messageType) {
            is AudioMessageType -> messageType.body
            is EmoteMessageType -> messageType.body
            is FileMessageType -> messageType.body
            is ImageMessageType -> messageType.body
            is NoticeMessageType -> messageType.body
            is TextMessageType -> messageType.body
            is VideoMessageType -> messageType.body
            is LocationMessageType -> messageType.body
            is UnknownMessageType -> stringProvider.getString(CommonStrings.common_unsupported_event)
        }
    }

    private fun descriptionFromRoomMembershipContent(
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
}

@Suppress("LongParameterList")
private fun buildNotifiableMessageEvent(
    sessionId: SessionId,
    senderId: UserId,
    roomId: RoomId,
    eventId: EventId,
    editedEventId: EventId? = null,
    canBeReplaced: Boolean = false,
    noisy: Boolean,
    timestamp: Long,
    senderName: String?,
    body: String?,
    // We cannot use Uri? type here, as that could trigger a
    // NotSerializableException when persisting this to storage
    imageUriString: String? = null,
    threadId: ThreadId? = null,
    roomName: String? = null,
    roomIsDirect: Boolean = false,
    roomAvatarPath: String? = null,
    senderAvatarPath: String? = null,
    soundName: String? = null,
    // This is used for >N notification, as the result of a smart reply
    outGoingMessage: Boolean = false,
    outGoingMessageFailed: Boolean = false,
    isRedacted: Boolean = false,
    isUpdated: Boolean = false
) = NotifiableMessageEvent(
    sessionId = sessionId,
    senderId = senderId,
    roomId = roomId,
    eventId = eventId,
    editedEventId = editedEventId,
    canBeReplaced = canBeReplaced,
    noisy = noisy,
    timestamp = timestamp,
    senderName = senderName,
    body = body,
    imageUriString = imageUriString,
    threadId = threadId,
    roomName = roomName,
    roomIsDirect = roomIsDirect,
    roomAvatarPath = roomAvatarPath,
    senderAvatarPath = senderAvatarPath,
    soundName = soundName,
    outGoingMessage = outGoingMessage,
    outGoingMessageFailed = outGoingMessageFailed,
    isRedacted = isRedacted,
    isUpdated = isUpdated
)
