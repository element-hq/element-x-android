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

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.notification.NotificationContent
import io.element.android.libraries.matrix.api.notification.NotificationData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EventType
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.LocationMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.OtherMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.StickerMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VoiceMessageType
import io.element.android.libraries.matrix.ui.messages.toPlainText
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.toolbox.api.strings.StringProvider
import io.element.android.services.toolbox.api.systemclock.SystemClock
import timber.log.Timber
import javax.inject.Inject

private val loggerTag = LoggerTag("DefaultNotifiableEventResolver", LoggerTag.NotificationLoggerTag)

/**
 * The notifiable event resolver is able to create a NotifiableEvent (view model for notifications) from an sdk Event.
 * It is used as a bridge between the Event Thread and the NotificationDrawerManager.
 * The NotifiableEventResolver is the only aware of session/store, the NotificationDrawerManager has no knowledge of that,
 * this pattern allow decoupling between the object responsible of displaying notifications and the matrix sdk.
 */
interface NotifiableEventResolver {
    suspend fun resolveEvent(sessionId: SessionId, roomId: RoomId, eventId: EventId): NotifiableEvent?
}

@ContributesBinding(AppScope::class)
class DefaultNotifiableEventResolver @Inject constructor(
    private val stringProvider: StringProvider,
    private val clock: SystemClock,
    private val matrixClientProvider: MatrixClientProvider,
    private val notificationMediaRepoFactory: NotificationMediaRepo.Factory,
    @ApplicationContext private val context: Context,
    private val permalinkParser: PermalinkParser,
    private val callNotificationEventResolver: CallNotificationEventResolver,
) : NotifiableEventResolver {
    override suspend fun resolveEvent(sessionId: SessionId, roomId: RoomId, eventId: EventId): NotifiableEvent? {
        // Restore session
        val client = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return null
        val notificationService = client.notificationService()
        val notificationData = notificationService.getNotification(
            userId = sessionId,
            roomId = roomId,
            eventId = eventId,
        ).onFailure {
            Timber.tag(loggerTag.value).e(it, "Unable to resolve event: $eventId.")
        }.getOrNull()

        // TODO this notificationData is not always valid at the moment, sometimes the Rust SDK can't fetch the matching event
        return notificationData?.asNotifiableEvent(client, sessionId)
    }

    private suspend fun NotificationData.asNotifiableEvent(
        client: MatrixClient,
        userId: SessionId,
    ): NotifiableEvent? {
        return when (val content = this.content) {
            is NotificationContent.MessageLike.RoomMessage -> {
                val senderDisambiguatedDisplayName = getDisambiguatedDisplayName(content.senderId)
                val messageBody = descriptionFromMessageContent(content, senderDisambiguatedDisplayName)
                val notificationBody = if (hasMention) {
                    stringProvider.getString(R.string.notification_mentioned_you_body, messageBody)
                } else {
                    messageBody
                }
                buildNotifiableMessageEvent(
                    sessionId = userId,
                    senderId = content.senderId,
                    roomId = roomId,
                    eventId = eventId,
                    noisy = isNoisy,
                    timestamp = this.timestamp,
                    senderDisambiguatedDisplayName = senderDisambiguatedDisplayName,
                    body = notificationBody,
                    imageUriString = fetchImageIfPresent(client)?.toString(),
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
                        description = descriptionFromRoomMembershipInvite(isDirect),
                        // TODO check if type is needed anymore
                        type = null,
                        // TODO check if title is needed anymore
                        title = null,
                    )
                } else {
                    Timber.tag(loggerTag.value).d("Ignoring notification state event for membership ${content.membershipState}")
                    null
                }
            }
            NotificationContent.MessageLike.CallAnswer,
            NotificationContent.MessageLike.CallCandidates,
            NotificationContent.MessageLike.CallHangup -> {
                Timber.tag(loggerTag.value).d("Ignoring notification for call ${content.javaClass.simpleName}")
                null
            }
            is NotificationContent.MessageLike.CallInvite -> {
                buildNotifiableMessageEvent(
                    sessionId = userId,
                    senderId = content.senderId,
                    roomId = roomId,
                    eventId = eventId,
                    noisy = isNoisy,
                    timestamp = this.timestamp,
                    senderDisambiguatedDisplayName = getDisambiguatedDisplayName(content.senderId),
                    body = stringProvider.getString(CommonStrings.common_call_invite),
                    imageUriString = fetchImageIfPresent(client)?.toString(),
                    roomName = roomDisplayName,
                    roomIsDirect = isDirect,
                    roomAvatarPath = roomAvatarUrl,
                    senderAvatarPath = senderAvatarUrl,
                )
            }
            is NotificationContent.MessageLike.CallNotify -> {
                callNotificationEventResolver.resolveEvent(userId, this)
            }
            NotificationContent.MessageLike.KeyVerificationAccept,
            NotificationContent.MessageLike.KeyVerificationCancel,
            NotificationContent.MessageLike.KeyVerificationDone,
            NotificationContent.MessageLike.KeyVerificationKey,
            NotificationContent.MessageLike.KeyVerificationMac,
            NotificationContent.MessageLike.KeyVerificationReady,
            NotificationContent.MessageLike.KeyVerificationStart -> null.also {
                Timber.tag(loggerTag.value).d("Ignoring notification for verification ${content.javaClass.simpleName}")
            }
            is NotificationContent.MessageLike.Poll -> {
                buildNotifiableMessageEvent(
                    sessionId = userId,
                    senderId = content.senderId,
                    roomId = roomId,
                    eventId = eventId,
                    noisy = isNoisy,
                    timestamp = this.timestamp,
                    senderDisambiguatedDisplayName = getDisambiguatedDisplayName(content.senderId),
                    body = stringProvider.getString(CommonStrings.common_poll_summary, content.question),
                    imageUriString = null,
                    roomName = roomDisplayName,
                    roomIsDirect = isDirect,
                    roomAvatarPath = roomAvatarUrl,
                    senderAvatarPath = senderAvatarUrl,
                )
            }
            is NotificationContent.MessageLike.ReactionContent -> null.also {
                Timber.tag(loggerTag.value).d("Ignoring notification for reaction")
            }
            NotificationContent.MessageLike.RoomEncrypted -> fallbackNotifiableEvent(userId, roomId, eventId).also {
                Timber.tag(loggerTag.value).w("Notification with encrypted content -> fallback")
            }
            NotificationContent.MessageLike.RoomRedaction -> null.also {
                Timber.tag(loggerTag.value).d("Ignoring notification for redaction")
            }
            NotificationContent.MessageLike.Sticker -> null.also {
                Timber.tag(loggerTag.value).d("Ignoring notification for sticker")
            }
            NotificationContent.StateEvent.PolicyRuleRoom,
            NotificationContent.StateEvent.PolicyRuleServer,
            NotificationContent.StateEvent.PolicyRuleUser,
            NotificationContent.StateEvent.RoomAliases,
            NotificationContent.StateEvent.RoomAvatar,
            NotificationContent.StateEvent.RoomCanonicalAlias,
            NotificationContent.StateEvent.RoomCreate,
            NotificationContent.StateEvent.RoomEncryption,
            NotificationContent.StateEvent.RoomGuestAccess,
            NotificationContent.StateEvent.RoomHistoryVisibility,
            NotificationContent.StateEvent.RoomJoinRules,
            NotificationContent.StateEvent.RoomName,
            NotificationContent.StateEvent.RoomPinnedEvents,
            NotificationContent.StateEvent.RoomPowerLevels,
            NotificationContent.StateEvent.RoomServerAcl,
            NotificationContent.StateEvent.RoomThirdPartyInvite,
            NotificationContent.StateEvent.RoomTombstone,
            NotificationContent.StateEvent.RoomTopic,
            NotificationContent.StateEvent.SpaceChild,
            NotificationContent.StateEvent.SpaceParent -> null.also {
                Timber.tag(loggerTag.value).d("Ignoring notification for state event ${content.javaClass.simpleName}")
            }
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
        senderDisambiguatedDisplayName: String,
    ): String {
        return when (val messageType = content.messageType) {
            is AudioMessageType -> messageType.body
            is VoiceMessageType -> stringProvider.getString(CommonStrings.common_voice_message)
            is EmoteMessageType -> "* $senderDisambiguatedDisplayName ${messageType.body}"
            is FileMessageType -> messageType.body
            is ImageMessageType -> messageType.body
            is StickerMessageType -> messageType.body
            is NoticeMessageType -> messageType.body
            is TextMessageType -> messageType.toPlainText(permalinkParser = permalinkParser)
            is VideoMessageType -> messageType.body
            is LocationMessageType -> messageType.body
            is OtherMessageType -> messageType.body
        }
    }

    private fun descriptionFromRoomMembershipInvite(
        isDirectRoom: Boolean
    ): String {
        return if (isDirectRoom) {
            stringProvider.getString(R.string.notification_invite_body)
        } else {
            stringProvider.getString(R.string.notification_room_invite_body)
        }
    }

    private suspend fun NotificationData.fetchImageIfPresent(client: MatrixClient): Uri? {
        val fileResult = when (val content = this.content) {
            is NotificationContent.MessageLike.RoomMessage -> {
                when (val messageType = content.messageType) {
                    is ImageMessageType -> notificationMediaRepoFactory.create(client)
                        .getMediaFile(
                            mediaSource = messageType.source,
                            mimeType = messageType.info?.mimetype,
                            body = messageType.body,
                        )
                    is VideoMessageType -> null // Use the thumbnail here?
                    else -> null
                }
            }
            else -> null
        } ?: return null

        return fileResult
            .onFailure {
                Timber.tag(loggerTag.value).e(it, "Failed to download image for notification")
            }
            .map { mediaFile ->
                val authority = "${context.packageName}.notifications.fileprovider"
                FileProvider.getUriForFile(context, authority, mediaFile)
            }
            .getOrNull()
    }
}

@Suppress("LongParameterList")
internal fun buildNotifiableMessageEvent(
    sessionId: SessionId,
    senderId: UserId,
    roomId: RoomId,
    eventId: EventId,
    editedEventId: EventId? = null,
    canBeReplaced: Boolean = false,
    noisy: Boolean,
    timestamp: Long,
    senderDisambiguatedDisplayName: String?,
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
    isUpdated: Boolean = false,
    type: String = EventType.MESSAGE,
) = NotifiableMessageEvent(
    sessionId = sessionId,
    senderId = senderId,
    roomId = roomId,
    eventId = eventId,
    editedEventId = editedEventId,
    canBeReplaced = canBeReplaced,
    noisy = noisy,
    timestamp = timestamp,
    senderDisambiguatedDisplayName = senderDisambiguatedDisplayName,
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
    isUpdated = isUpdated,
    type = type,
)
