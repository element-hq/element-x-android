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
import io.element.android.libraries.matrix.api.notification.NotificationContent
import io.element.android.libraries.matrix.api.notification.NotificationData
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.LocationMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
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
        val notificationData = notificationService.getNotification(
            userId = sessionId,
            roomId = roomId,
            eventId = eventId,
            // FIXME should be true in the future, but right now it's broken
            //  (https://github.com/vector-im/element-x-android/issues/640#issuecomment-1612913658)
            filterByPushRules = false,
        ).onFailure {
            Timber.tag(loggerTag.value).e(it, "Unable to resolve event: $eventId.")
        }.getOrNull()

        // TODO this notificationData is not always valid at the moment, sometimes the Rust SDK can't fetch the matching event
        return notificationData?.asNotifiableEvent(sessionId)
            ?: fallbackNotifiableEvent(sessionId, roomId, eventId)
    }

    private fun NotificationData.asNotifiableEvent(userId: SessionId): NotifiableEvent? {
        return when (this) {
            is NotificationData.Message -> {
                buildNotifiableMessageEvent(
                    sessionId = userId,
                    roomId = roomId,
                    eventId = eventId,
                    noisy = isNoisy,
                    timestamp = event.timestamp,
                    senderName = senderDisplayName,
                    senderId = senderId.value,
                    body = descriptionFromMessageContent(event.content as NotificationContent.MessageLike.RoomMessage),
                    imageUriString = event.contentUrl,
                    roomName = roomDisplayName,
                    roomIsDirect = isDirect,
                    roomAvatarPath = roomAvatarUrl,
                    senderAvatarPath = senderAvatarUrl,
                )
            }
            is NotificationData.Invite -> {
                InviteNotifiableEvent(
                    sessionId = userId,
                    roomId = roomId,
                    eventId = null,
                    editedEventId = null,
                    canBeReplaced = true,
                    roomName = roomDisplayName,
                    noisy = isNoisy,
                    timestamp = System.currentTimeMillis(), // TODO: use actual timestamp
                    soundName = null,
                    isRedacted = false,
                    isUpdated = false,
                    description = descriptionFromRoomMembershipContent(
                        NotificationContent.StateEvent.RoomMemberContent(userId.value, RoomMembershipState.INVITE), isDirect
                    ) ?: return null,
                    type = null, // TODO check if type is needed anymore
                    title = null, // TODO check if title is needed anymore
                )
            }
            else -> null
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
    roomId: RoomId,
    eventId: EventId?,
    editedEventId: EventId? = null,
    canBeReplaced: Boolean = false,
    noisy: Boolean,
    timestamp: Long,
    senderName: String?,
    senderId: String?,
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
    roomId = roomId,
    eventId = eventId,
    editedEventId = editedEventId,
    canBeReplaced = canBeReplaced,
    noisy = noisy,
    timestamp = timestamp,
    senderName = senderName,
    senderId = senderId,
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
