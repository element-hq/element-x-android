/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.core.extensions.flatMap
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.exception.NotificationResolverException
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.matrix.api.media.getMediaPreviewValue
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
import io.element.android.libraries.push.api.push.NotificationEventRequest
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.toolbox.api.strings.StringProvider
import timber.log.Timber

private val loggerTag = LoggerTag("DefaultNotifiableEventResolver", LoggerTag.NotificationLoggerTag)

/**
 * Result of resolving a batch of push events.
 * The outermost [Result] indicates whether the setup to resolve the events was successful.
 * The results for each push notification will be a map of [NotificationEventRequest] to [Result] of [ResolvedPushEvent].
 * If the resolution of a specific event fails, the innermost [Result] will contain an exception.
 */
typealias ResolvePushEventsResult = Result<Map<NotificationEventRequest, Result<ResolvedPushEvent>>>

/**
 * The notifiable event resolver is able to create a NotifiableEvent (view model for notifications) from an sdk Event.
 * It is used as a bridge between the Event Thread and the NotificationDrawerManager.
 * The NotifiableEventResolver is the only aware of session/store, the NotificationDrawerManager has no knowledge of that,
 * this pattern allow decoupling between the object responsible of displaying notifications and the matrix sdk.
 */
interface NotifiableEventResolver {
    suspend fun resolveEvents(
        sessionId: SessionId,
        notificationEventRequests: List<NotificationEventRequest>
    ): ResolvePushEventsResult
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultNotifiableEventResolver(
    private val stringProvider: StringProvider,
    private val matrixClientProvider: MatrixClientProvider,
    private val notificationMediaRepoFactory: NotificationMediaRepo.Factory,
    @ApplicationContext private val context: Context,
    private val permalinkParser: PermalinkParser,
    private val callNotificationEventResolver: CallNotificationEventResolver,
    private val fallbackNotificationFactory: FallbackNotificationFactory,
    private val featureFlagService: FeatureFlagService,
) : NotifiableEventResolver {
    override suspend fun resolveEvents(
        sessionId: SessionId,
        notificationEventRequests: List<NotificationEventRequest>
    ): ResolvePushEventsResult {
        Timber.d("Queueing notifications: $notificationEventRequests")
        val client = matrixClientProvider.getOrRestore(sessionId).getOrElse {
            return Result.failure(IllegalStateException("Couldn't get or restore client for session $sessionId"))
        }
        val ids = notificationEventRequests.groupBy { it.roomId }
            .mapValues { (_, requests) ->
                requests.map { it.eventId }
            }

        // TODO this notificationData is not always valid at the moment, sometimes the Rust SDK can't fetch the matching event
        val notificationsResult = client.notificationService.getNotifications(ids)

        if (notificationsResult.isFailure) {
            val exception = notificationsResult.exceptionOrNull()
            Timber.tag(loggerTag.value).e(exception, "Failed to get notifications for $ids")
            return Result.failure(exception ?: NotificationResolverException.UnknownError("Unknown error while fetching notifications"))
        }

        // The null check is done above
        val notificationDataMap = notificationsResult.getOrNull()!!.mapValues { (_, notificationData) ->
            notificationData.flatMap { data ->
                data.asNotifiableEvent(client, sessionId)
            }
        }

        return Result.success(
            notificationEventRequests.associate { request ->
                val notificationDataResult = notificationDataMap[request.eventId]
                if (notificationDataResult == null) {
                    request to Result.failure(NotificationResolverException.UnknownError("No notification data for ${request.roomId} - ${request.eventId}"))
                } else {
                    request to notificationDataResult
                }
            }
        )
    }

    private suspend fun NotificationData.asNotifiableEvent(
        client: MatrixClient,
        userId: SessionId,
    ): Result<ResolvedPushEvent> = runCatchingExceptions {
        when (val content = this.content) {
            is NotificationContent.MessageLike.RoomMessage -> {
                val showMediaPreview = client.mediaPreviewService.getMediaPreviewValue() == MediaPreviewValue.On
                val senderDisambiguatedDisplayName = getDisambiguatedDisplayName(content.senderId)
                val messageBody = descriptionFromMessageContent(content, senderDisambiguatedDisplayName)
                val notifiableMessageEvent = buildNotifiableMessageEvent(
                    sessionId = userId,
                    senderId = content.senderId,
                    roomId = roomId,
                    eventId = eventId,
                    threadId = threadId.takeIf { featureFlagService.isFeatureEnabled(FeatureFlags.Threads) },
                    noisy = isNoisy,
                    timestamp = this.timestamp,
                    senderDisambiguatedDisplayName = senderDisambiguatedDisplayName,
                    body = messageBody,
                    imageUriString = if (showMediaPreview) content.fetchImageIfPresent(client)?.toString() else null,
                    imageMimeType = if (showMediaPreview) content.getImageMimetype() else null,
                    roomName = roomDisplayName,
                    roomIsDm = isDm,
                    roomAvatarPath = roomAvatarUrl,
                    senderAvatarPath = senderAvatarUrl,
                    hasMentionOrReply = hasMention,
                )
                ResolvedPushEvent.Event(notifiableMessageEvent)
            }
            is NotificationContent.Invite -> {
                val senderDisambiguatedDisplayName = getDisambiguatedDisplayName(content.senderId)
                val inviteNotifiableEvent = InviteNotifiableEvent(
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
                ResolvedPushEvent.Event(inviteNotifiableEvent)
            }
            NotificationContent.MessageLike.CallAnswer,
            NotificationContent.MessageLike.CallCandidates,
            NotificationContent.MessageLike.CallHangup -> {
                Timber.tag(loggerTag.value).d("Ignoring notification for call ${content.javaClass.simpleName}")
                throw NotificationResolverException.EventFilteredOut
            }
            is NotificationContent.MessageLike.CallInvite -> {
                val notifiableMessageEvent = buildNotifiableMessageEvent(
                    sessionId = userId,
                    senderId = content.senderId,
                    roomId = roomId,
                    eventId = eventId,
                    noisy = isNoisy,
                    timestamp = this.timestamp,
                    senderDisambiguatedDisplayName = getDisambiguatedDisplayName(content.senderId),
                    body = stringProvider.getString(CommonStrings.common_unsupported_call),
                    roomName = roomDisplayName,
                    roomIsDm = isDm,
                    roomAvatarPath = roomAvatarUrl,
                    senderAvatarPath = senderAvatarUrl,
                )
                ResolvedPushEvent.Event(notifiableMessageEvent)
            }
            is NotificationContent.MessageLike.RtcNotification -> {
                val notifiableEvent = callNotificationEventResolver.resolveEvent(userId, this).getOrThrow()
                ResolvedPushEvent.Event(notifiableEvent)
            }
            NotificationContent.MessageLike.KeyVerificationAccept,
            NotificationContent.MessageLike.KeyVerificationCancel,
            NotificationContent.MessageLike.KeyVerificationDone,
            NotificationContent.MessageLike.KeyVerificationKey,
            NotificationContent.MessageLike.KeyVerificationMac,
            NotificationContent.MessageLike.KeyVerificationReady,
            NotificationContent.MessageLike.KeyVerificationStart -> {
                Timber.tag(loggerTag.value).d("Ignoring notification for verification ${content.javaClass.simpleName}")
                throw NotificationResolverException.EventFilteredOut
            }
            is NotificationContent.MessageLike.Poll -> {
                val notifiableEventMessage = buildNotifiableMessageEvent(
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
                ResolvedPushEvent.Event(notifiableEventMessage)
            }
            is NotificationContent.MessageLike.ReactionContent -> {
                Timber.tag(loggerTag.value).d("Ignoring notification for reaction")
                throw NotificationResolverException.EventFilteredOut
            }
            NotificationContent.MessageLike.RoomEncrypted -> {
                Timber.tag(loggerTag.value).w("Notification with encrypted content -> fallback")
                val fallbackNotifiableEvent = fallbackNotificationFactory.create(
                    sessionId = userId,
                    roomId = roomId,
                    eventId = eventId,
                    cause = "Unable to decrypt event content",
                )
                ResolvedPushEvent.Event(fallbackNotifiableEvent)
            }
            is NotificationContent.MessageLike.RoomRedaction -> {
                // Note: this case will be handled below
                val redactedEventId = content.redactedEventId
                if (redactedEventId == null) {
                    Timber.tag(loggerTag.value).d("redactedEventId is null.")
                    throw NotificationResolverException.UnknownError("redactedEventId is null")
                } else {
                    ResolvedPushEvent.Redaction(
                        sessionId = userId,
                        roomId = roomId,
                        redactedEventId = redactedEventId,
                        reason = content.reason,
                    )
                }
            }
            NotificationContent.MessageLike.Sticker -> {
                Timber.tag(loggerTag.value).d("Ignoring notification for sticker")
                throw NotificationResolverException.EventFilteredOut
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
            is NotificationContent.StateEvent.RoomTopic,
            NotificationContent.StateEvent.SpaceChild,
            NotificationContent.StateEvent.SpaceParent -> {
                Timber.tag(loggerTag.value).d("Ignoring notification for state event ${content.javaClass.simpleName}")
                throw NotificationResolverException.EventFilteredOut
            }
        }
    }

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

    private fun NotificationContent.MessageLike.RoomMessage.getImageMimetype(): String? {
        return when (val messageType = messageType) {
            is ImageMessageType -> messageType.info?.mimetype
            is VideoMessageType -> null // Use the thumbnail here?
            else -> null
        }
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
    imageMimeType: String? = null,
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
    imageMimeType = imageMimeType,
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
