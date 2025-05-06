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
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.extensions.flatMap
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

private val loggerTag = LoggerTag("DefaultNotifiableEventResolver", LoggerTag.NotificationLoggerTag)

/**
 * The notifiable event resolver is able to create a NotifiableEvent (view model for notifications) from an sdk Event.
 * It is used as a bridge between the Event Thread and the NotificationDrawerManager.
 * The NotifiableEventResolver is the only aware of session/store, the NotificationDrawerManager has no knowledge of that,
 * this pattern allow decoupling between the object responsible of displaying notifications and the matrix sdk.
 */
interface NotifiableEventResolver {
    suspend fun resolveEvent(sessionId: SessionId, roomId: RoomId, eventId: EventId): Result<ResolvedPushEvent>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultNotifiableEventResolver @Inject constructor(
    private val stringProvider: StringProvider,
    private val clock: SystemClock,
    private val matrixClientProvider: MatrixClientProvider,
    private val notificationMediaRepoFactory: NotificationMediaRepo.Factory,
    @ApplicationContext private val context: Context,
    private val permalinkParser: PermalinkParser,
    private val callNotificationEventResolver: CallNotificationEventResolver,
    private val appPreferencesStore: AppPreferencesStore,
    private val appCoroutineScope: CoroutineScope,
) : NotifiableEventResolver {

    private val resolver = NotificationResolver(
        matrixClientProvider = matrixClientProvider,
        coroutineScope = appCoroutineScope,
    )

    override suspend fun resolveEvent(sessionId: SessionId, roomId: RoomId, eventId: EventId): Result<ResolvedPushEvent> {
        Timber.d("Queueing notification for $sessionId: $roomId, $eventId")
        resolver.queue(NotificationEventRequest(sessionId, eventId, roomId))
        val notificationData = runCatching {
            withTimeout(30.seconds) { resolver.results.map { it[eventId] }.first() }
        }

        // TODO this notificationData is not always valid at the moment, sometimes the Rust SDK can't fetch the matching event
        return notificationData.flatMap {
            if (it == null) {
                Timber.tag(loggerTag.value).d("No notification data found for event $eventId")
                return@flatMap Result.failure(ResolvingException("Unable to resolve event $eventId"))
            } else {
                Timber.tag(loggerTag.value).d("Found notification item for $eventId")
                val client = matrixClientProvider.getOrRestore(sessionId).getOrThrow()
                it.asNotifiableEvent(client, sessionId)
            }
        }
    }

    private suspend fun NotificationData.asNotifiableEvent(
        client: MatrixClient,
        userId: SessionId,
    ): Result<ResolvedPushEvent> = runCatching {
        when (val content = this.content) {
            is NotificationContent.MessageLike.RoomMessage -> {
                val senderDisambiguatedDisplayName = getDisambiguatedDisplayName(content.senderId)
                val messageBody = descriptionFromMessageContent(content, senderDisambiguatedDisplayName)
                val notifiableMessageEvent = buildNotifiableMessageEvent(
                    sessionId = userId,
                    senderId = content.senderId,
                    roomId = roomId,
                    eventId = eventId,
                    threadId = threadId,
                    noisy = isNoisy,
                    timestamp = this.timestamp,
                    senderDisambiguatedDisplayName = senderDisambiguatedDisplayName,
                    body = messageBody,
                    imageUriString = content.fetchImageIfPresent(client)?.toString(),
                    imageMimeType = content.getImageMimetype(),
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
                throw ResolvingException("Ignoring notification for call ${content.javaClass.simpleName}")
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
            is NotificationContent.MessageLike.CallNotify -> {
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
                throw ResolvingException("Ignoring notification for verification ${content.javaClass.simpleName}")
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
                throw ResolvingException("Ignoring notification for reaction")
            }
            NotificationContent.MessageLike.RoomEncrypted -> {
                Timber.tag(loggerTag.value).w("Notification with encrypted content -> fallback")
                val fallbackNotifiableEvent = fallbackNotifiableEvent(userId, roomId, eventId)
                ResolvedPushEvent.Event(fallbackNotifiableEvent)
            }
            is NotificationContent.MessageLike.RoomRedaction -> {
                // Note: this case will be handled below
                val redactedEventId = content.redactedEventId
                if (redactedEventId == null) {
                    Timber.tag(loggerTag.value).d("redactedEventId is null.")
                    throw ResolvingException("redactedEventId is null")
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
                throw ResolvingException("Ignoring notification for reaction")
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
                throw ResolvingException("Ignoring notification for state event ${content.javaClass.simpleName}")
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
        if (appPreferencesStore.getTimelineMediaPreviewValueFlow().first() != MediaPreviewValue.On) {
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

    private suspend fun NotificationContent.MessageLike.RoomMessage.getImageMimetype(): String? {
        if (appPreferencesStore.getTimelineMediaPreviewValueFlow().first() != MediaPreviewValue.On) {
            return null
        }
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

class NotificationResolver(
    private val matrixClientProvider: MatrixClientProvider,
    private val coroutineScope: CoroutineScope,
) {
    private val requests = Channel<NotificationEventRequest>(capacity = 100)

    val results = MutableSharedFlow<Map<EventId, NotificationData?>>()

    init {
        coroutineScope.launch {
            while (coroutineScope.isActive) {
                delay(250)
                val ids = buildList {
                    while (!requests.isEmpty) {
                        add(requests.receive())
                    }
                }.groupBy { it.sessionId }

                val sessionIds = ids.keys
                for (sessionId in sessionIds) {
                    val client = matrixClientProvider.getOrRestore(sessionId).getOrNull()
                    if (client != null) {
                        val notificationService = client.notificationService()
                        val requestsByRoom = ids[sessionId].orEmpty().groupBy { it.roomId }.mapValues { it.value.map { it.eventId } }
                        Timber.d("Fetching notifications for $sessionId: $requestsByRoom. Pending requests: ${!requests.isEmpty}")

                        coroutineScope.launch {
                            val results = notificationService.getNotifications(requestsByRoom).getOrNull().orEmpty()
                            if (results.isNotEmpty()) {
                                this@NotificationResolver.results.emit(results)
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun queue(request: NotificationEventRequest) {
        requests.send(request)
    }
}

data class NotificationEventRequest(
    val sessionId: SessionId,
    val eventId: EventId,
    val roomId: RoomId,
)
