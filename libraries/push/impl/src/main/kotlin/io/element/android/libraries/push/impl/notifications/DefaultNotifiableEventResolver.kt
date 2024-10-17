/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.toolbox.api.strings.StringProvider
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.flow.first
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
    suspend fun resolveEvent(sessionId: SessionId, roomId: RoomId, eventId: EventId): ResolvedPushEvent?
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
    private val appPreferencesStore: AppPreferencesStore,
) : NotifiableEventResolver {
    override suspend fun resolveEvent(sessionId: SessionId, roomId: RoomId, eventId: EventId): ResolvedPushEvent? {
        // Restore session
        val client = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return null
        val notificationService = client.notificationService()
        val notificationData = notificationService.getNotification(
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
    ): ResolvedPushEvent? {
        val content = this.content
        val notifiableEvent = when (content) {
            is NotificationContent.MessageLike.RoomMessage -> {
                val senderDisambiguatedDisplayName = getDisambiguatedDisplayName(content.senderId)
                val messageBody = descriptionFromMessageContent(content, senderDisambiguatedDisplayName)
                buildNotifiableMessageEvent(
                    sessionId = userId,
                    senderId = content.senderId,
                    roomId = roomId,
                    eventId = eventId,
                    noisy = isNoisy,
                    timestamp = this.timestamp,
                    senderDisambiguatedDisplayName = senderDisambiguatedDisplayName,
                    body = messageBody,
                    imageUriString = content.fetchImageIfPresent(client)?.toString(),
                    roomName = roomDisplayName,
                    roomIsDm = isDm,
                    roomAvatarPath = roomAvatarUrl,
                    senderAvatarPath = senderAvatarUrl,
                    hasMentionOrReply = hasMention,
                )
            }
            is NotificationContent.Invite -> {
                val senderDisambiguatedDisplayName = getDisambiguatedDisplayName(content.senderId)
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
                    description = descriptionFromRoomMembershipInvite(senderDisambiguatedDisplayName, isDirect),
                    // TODO check if type is needed anymore
                    type = null,
                    // TODO check if title is needed anymore
                    title = null,
                )
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
                    roomName = roomDisplayName,
                    roomIsDm = isDm,
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
                    roomIsDm = isDm,
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
            is NotificationContent.MessageLike.RoomRedaction -> {
                // Note: this case will be handled below
                null
            }
            NotificationContent.MessageLike.Sticker -> null.also {
                Timber.tag(loggerTag.value).d("Ignoring notification for sticker")
            }
            is NotificationContent.StateEvent.RoomMemberContent,
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

        return if (notifiableEvent != null) {
            ResolvedPushEvent.Event(notifiableEvent)
        } else if (content is NotificationContent.MessageLike.RoomRedaction) {
            val redactedEventId = content.redactedEventId
            if (redactedEventId == null) {
                Timber.tag(loggerTag.value).d("redactedEventId is null.")
                null
            } else {
                ResolvedPushEvent.Redaction(
                    sessionId = userId,
                    roomId = roomId,
                    redactedEventId = redactedEventId,
                    reason = content.reason,
                )
            }
        } else {
            null
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
            is AudioMessageType -> messageType.bestDescription
            is VoiceMessageType -> stringProvider.getString(CommonStrings.common_voice_message)
            is EmoteMessageType -> "* $senderDisambiguatedDisplayName ${messageType.body}"
            is FileMessageType -> messageType.bestDescription
            is ImageMessageType -> messageType.bestDescription
            is StickerMessageType -> messageType.bestDescription
            is NoticeMessageType -> messageType.body
            is TextMessageType -> messageType.toPlainText(permalinkParser = permalinkParser)
            is VideoMessageType -> messageType.bestDescription
            is LocationMessageType -> messageType.body
            is OtherMessageType -> messageType.body
        }
    }

    private fun descriptionFromRoomMembershipInvite(
        senderDisambiguatedDisplayName: String,
        isDirectRoom: Boolean
    ): String {
        return if (isDirectRoom) {
            stringProvider.getString(R.string.notification_invite_body_with_sender, senderDisambiguatedDisplayName)
        } else {
            stringProvider.getString(R.string.notification_room_invite_body_with_sender, senderDisambiguatedDisplayName)
        }
    }

    private suspend fun NotificationContent.MessageLike.RoomMessage.fetchImageIfPresent(client: MatrixClient): Uri? {
        if (appPreferencesStore.doesHideImagesAndVideosFlow().first()) {
            return null
        }
        val fileResult = when (val messageType = messageType) {
            is ImageMessageType -> notificationMediaRepoFactory.create(client)
                .getMediaFile(
                    mediaSource = messageType.source,
                    mimeType = messageType.info?.mimetype,
                    filename = messageType.filename,
                )
            is VideoMessageType -> null // Use the thumbnail here?
            else -> null
        }
            ?: return null

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
    roomIsDm: Boolean = false,
    roomAvatarPath: String? = null,
    senderAvatarPath: String? = null,
    soundName: String? = null,
    // This is used for >N notification, as the result of a smart reply
    outGoingMessage: Boolean = false,
    outGoingMessageFailed: Boolean = false,
    isRedacted: Boolean = false,
    isUpdated: Boolean = false,
    type: String = EventType.MESSAGE,
    hasMentionOrReply: Boolean = false,
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
    roomIsDm = roomIsDm,
    roomAvatarPath = roomAvatarPath,
    senderAvatarPath = senderAvatarPath,
    soundName = soundName,
    outGoingMessage = outGoingMessage,
    outGoingMessageFailed = outGoingMessageFailed,
    isRedacted = isRedacted,
    isUpdated = isUpdated,
    type = type,
    hasMentionOrReply = hasMentionOrReply,
)
