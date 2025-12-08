/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.factories

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.ColorInt
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.MessagingStyle
import androidx.core.app.Person
import androidx.core.os.bundleOf
import coil3.ImageLoader
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.timeline.item.event.EventType
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.matrix.ui.model.getBestName
import io.element.android.libraries.push.api.notifications.NotificationBitmapLoader
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.notifications.RoomEventGroupInfo
import io.element.android.libraries.push.impl.notifications.channels.NotificationChannels
import io.element.android.libraries.push.impl.notifications.debug.annotateForDebug
import io.element.android.libraries.push.impl.notifications.factories.action.AcceptInvitationActionFactory
import io.element.android.libraries.push.impl.notifications.factories.action.MarkAsReadActionFactory
import io.element.android.libraries.push.impl.notifications.factories.action.QuickReplyActionFactory
import io.element.android.libraries.push.impl.notifications.factories.action.RejectInvitationActionFactory
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import io.element.android.libraries.push.impl.notifications.shortcut.createShortcutId
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.appnavstate.api.ROOM_OPENED_FROM_NOTIFICATION
import io.element.android.services.toolbox.api.strings.StringProvider

interface NotificationCreator {
    /**
     * Create a notification for a Room.
     */
    suspend fun createMessagesListNotification(
        notificationAccountParams: NotificationAccountParams,
        roomInfo: RoomEventGroupInfo,
        threadId: ThreadId?,
        largeIcon: Bitmap?,
        lastMessageTimestamp: Long,
        tickerText: String,
        existingNotification: Notification?,
        imageLoader: ImageLoader,
        events: List<NotifiableMessageEvent>,
    ): Notification

    fun createRoomInvitationNotification(
        notificationAccountParams: NotificationAccountParams,
        inviteNotifiableEvent: InviteNotifiableEvent,
    ): Notification

    fun createSimpleEventNotification(
        notificationAccountParams: NotificationAccountParams,
        simpleNotifiableEvent: SimpleNotifiableEvent,
    ): Notification

    fun createFallbackNotification(
        notificationAccountParams: NotificationAccountParams,
        fallbackNotifiableEvent: FallbackNotifiableEvent,
    ): Notification

    /**
     * Create the summary notification.
     */
    fun createSummaryListNotification(
        notificationAccountParams: NotificationAccountParams,
        compatSummary: String,
        noisy: Boolean,
        lastMessageTimestamp: Long,
    ): Notification

    fun createDiagnosticNotification(
        @ColorInt color: Int,
    ): Notification

    fun createUnregistrationNotification(
        notificationAccountParams: NotificationAccountParams,
    ): Notification

    companion object {
        /**
         * Creates a tag for a message notification given its [roomId] and optional [threadId].
         */
        fun messageTag(roomId: RoomId, threadId: ThreadId?): String = if (threadId != null) {
            "$roomId|$threadId"
        } else {
            roomId.value
        }
    }
}

@ContributesBinding(AppScope::class)
class DefaultNotificationCreator(
    @ApplicationContext private val context: Context,
    private val notificationChannels: NotificationChannels,
    private val stringProvider: StringProvider,
    private val buildMeta: BuildMeta,
    private val pendingIntentFactory: PendingIntentFactory,
    private val markAsReadActionFactory: MarkAsReadActionFactory,
    private val quickReplyActionFactory: QuickReplyActionFactory,
    private val bitmapLoader: NotificationBitmapLoader,
    private val acceptInvitationActionFactory: AcceptInvitationActionFactory,
    private val rejectInvitationActionFactory: RejectInvitationActionFactory,
) : NotificationCreator {
    /**
     * Create a notification for a Room.
     */
    override suspend fun createMessagesListNotification(
        notificationAccountParams: NotificationAccountParams,
        roomInfo: RoomEventGroupInfo,
        threadId: ThreadId?,
        largeIcon: Bitmap?,
        lastMessageTimestamp: Long,
        tickerText: String,
        existingNotification: Notification?,
        imageLoader: ImageLoader,
        events: List<NotifiableMessageEvent>,
    ): Notification {
        // Build the pending intent for when the notification is clicked
        val eventId = events.firstOrNull()?.eventId
        val openIntent = when {
            threadId != null -> pendingIntentFactory.createOpenThreadPendingIntent(roomInfo.sessionId, roomInfo.roomId, eventId, threadId)
            else -> pendingIntentFactory.createOpenRoomPendingIntent(
                sessionId = roomInfo.sessionId,
                roomId = roomInfo.roomId,
                eventId = eventId,
                extras = bundleOf(ROOM_OPENED_FROM_NOTIFICATION to true),
            )
        }
        val containsMissedCall = events.any { it.type == EventType.RTC_NOTIFICATION }
        val channelId = if (containsMissedCall) {
            notificationChannels.getChannelForIncomingCall(false)
        } else {
            notificationChannels.getChannelIdForMessage(noisy = roomInfo.shouldBing)
        }
        // A category allows groups of notifications to be ranked and filtered – per user or system settings.
        // For example, alarm notifications should display before promo notifications, or message from known contact
        // that can be displayed in not disturb mode if white listed (the later will need compat28.x)
        // If any of the events are of rtc notification type it means a missed call, set the category to the right value
        val category = if (containsMissedCall) {
            NotificationCompat.CATEGORY_MISSED_CALL
        } else {
            NotificationCompat.CATEGORY_MESSAGE
        }
        val builder = if (existingNotification != null) {
            NotificationCompat.Builder(context, existingNotification)
                // Clear existing actions
                .clearActions()
        } else {
            NotificationCompat.Builder(context, channelId)
                // ID of the corresponding shortcut, for conversation features under API 30+
                // Must match those created in the ShortcutInfoCompat.Builder()
                // for the notification to appear as a "Conversation":
                // https://developer.android.com/develop/ui/views/notifications/conversations
                .apply {
                    if (threadId == null) {
                        setShortcutId(createShortcutId(roomInfo.sessionId, roomInfo.roomId))
                    }
                }
                .setGroupSummary(false)
                // In order to avoid notification making sound twice (due to the summary notification)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                // Remove notification after opening it or using an action
                .setAutoCancel(true)
        }
        val messagingStyle = existingNotification?.let {
            MessagingStyle.extractMessagingStyleFromNotification(it)
        } ?: createMessagingStyleFromCurrentUser(
            user = notificationAccountParams.user,
            imageLoader = imageLoader,
            roomName = roomInfo.roomDisplayName,
            isThread = threadId != null,
            roomIsGroup = !roomInfo.isDm,
        )
        messagingStyle.addMessagesFromEvents(events, imageLoader)
        return builder
            .setCategory(category)
            .setNumber(events.size)
            .setOnlyAlertOnce(roomInfo.isUpdated)
            .setWhen(lastMessageTimestamp)
            // MESSAGING_STYLE sets title and content for API 16 and above devices.
            .setStyle(messagingStyle)
            .configureWith(notificationAccountParams)
            // Mark room/thread as read
            .addAction(markAsReadActionFactory.create(roomInfo, threadId))
            .setContentIntent(openIntent)
            .setLargeIcon(largeIcon)
            .setDeleteIntent(pendingIntentFactory.createDismissRoomPendingIntent(roomInfo.sessionId, roomInfo.roomId))
            .apply {
                // Sets priority for 25 and below. For 26 and above, 'priority' is deprecated for
                // 'importance' which is set in the NotificationChannel. The integers representing
                // 'priority' are different from 'importance', so make sure you don't mix them.
                if (roomInfo.shouldBing) {
                    priority = NotificationCompat.PRIORITY_DEFAULT
                    setLights(notificationAccountParams.color, 500, 500)
                } else {
                    priority = NotificationCompat.PRIORITY_LOW
                }
                // Quick reply
                if (!roomInfo.hasSmartReplyError) {
                    val latestEventId = events.lastOrNull()?.eventId
                    addAction(quickReplyActionFactory.create(roomInfo, latestEventId, threadId))
                }
            }
            .setTicker(tickerText)
            .build()
    }

    override fun createRoomInvitationNotification(
        notificationAccountParams: NotificationAccountParams,
        inviteNotifiableEvent: InviteNotifiableEvent,
    ): Notification {
        val channelId = notificationChannels.getChannelIdForMessage(inviteNotifiableEvent.noisy)
        return NotificationCompat.Builder(context, channelId)
            .setOnlyAlertOnce(true)
            .setContentTitle((inviteNotifiableEvent.roomName ?: buildMeta.applicationName).annotateForDebug(5))
            .setContentText(inviteNotifiableEvent.description.annotateForDebug(6))
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
            .configureWith(notificationAccountParams)
            .addAction(rejectInvitationActionFactory.create(inviteNotifiableEvent))
            .addAction(acceptInvitationActionFactory.create(inviteNotifiableEvent))
            // Build the pending intent for when the notification is clicked
            .setContentIntent(pendingIntentFactory.createOpenRoomPendingIntent(
                sessionId = inviteNotifiableEvent.sessionId,
                roomId = inviteNotifiableEvent.roomId,
                eventId = null,
            ))
            .apply {
                if (inviteNotifiableEvent.noisy) {
                    // Compat
                    priority = NotificationCompat.PRIORITY_DEFAULT
                    setLights(notificationAccountParams.color, 500, 500)
                } else {
                    priority = NotificationCompat.PRIORITY_LOW
                }
            }
            .setDeleteIntent(
                pendingIntentFactory.createDismissInvitePendingIntent(
                    inviteNotifiableEvent.sessionId,
                    inviteNotifiableEvent.roomId,
                )
            )
            .setAutoCancel(true)
            .build()
    }

    override fun createSimpleEventNotification(
        notificationAccountParams: NotificationAccountParams,
        simpleNotifiableEvent: SimpleNotifiableEvent,
    ): Notification {
        val channelId = notificationChannels.getChannelIdForMessage(simpleNotifiableEvent.noisy)
        return NotificationCompat.Builder(context, channelId)
            .setOnlyAlertOnce(true)
            .setContentTitle(buildMeta.applicationName.annotateForDebug(7))
            .setContentText(simpleNotifiableEvent.description.annotateForDebug(8))
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
            .configureWith(notificationAccountParams)
            .setAutoCancel(true)
            .setContentIntent(pendingIntentFactory.createOpenRoomPendingIntent(
                sessionId = simpleNotifiableEvent.sessionId,
                roomId = simpleNotifiableEvent.roomId,
                eventId = null,
                extras = bundleOf(ROOM_OPENED_FROM_NOTIFICATION to true),
            ))
            .apply {
                if (simpleNotifiableEvent.noisy) {
                    // Compat
                    priority = NotificationCompat.PRIORITY_DEFAULT
                    setLights(notificationAccountParams.color, 500, 500)
                } else {
                    priority = NotificationCompat.PRIORITY_LOW
                }
            }
            .build()
    }

    override fun createFallbackNotification(
        notificationAccountParams: NotificationAccountParams,
        fallbackNotifiableEvent: FallbackNotifiableEvent,
    ): Notification {
        val channelId = notificationChannels.getChannelIdForMessage(false)
        return NotificationCompat.Builder(context, channelId)
            .setOnlyAlertOnce(true)
            .setContentTitle(buildMeta.applicationName.annotateForDebug(7))
            .setContentText(fallbackNotifiableEvent.description.orEmpty().annotateForDebug(8))
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
            .configureWith(notificationAccountParams)
            .setAutoCancel(true)
            .setWhen(fallbackNotifiableEvent.timestamp)
            // Ideally we'd use `createOpenRoomPendingIntent` here, but the broken notification might apply to an invite
            // and the user won't have access to the room yet, resulting in an error screen.
            .setContentIntent(pendingIntentFactory.createOpenSessionPendingIntent(fallbackNotifiableEvent.sessionId))
            .setDeleteIntent(
                pendingIntentFactory.createDismissEventPendingIntent(
                    fallbackNotifiableEvent.sessionId,
                    fallbackNotifiableEvent.roomId,
                    fallbackNotifiableEvent.eventId
                )
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * Create the summary notification.
     */
    override fun createSummaryListNotification(
        notificationAccountParams: NotificationAccountParams,
        compatSummary: String,
        noisy: Boolean,
        lastMessageTimestamp: Long,
    ): Notification {
        val channelId = notificationChannels.getChannelIdForMessage(noisy)
        val userId = notificationAccountParams.user.userId
        return NotificationCompat.Builder(context, channelId)
            .setOnlyAlertOnce(true)
            // used in compat < N, after summary is built based on child notifications
            .setWhen(lastMessageTimestamp)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            // set this notification as the summary for the group
            .setGroupSummary(true)
            .configureWith(notificationAccountParams)
            .apply {
                if (noisy) {
                    // Compat
                    priority = NotificationCompat.PRIORITY_DEFAULT
                    setLights(notificationAccountParams.color, 500, 500)
                } else {
                    // compat
                    priority = NotificationCompat.PRIORITY_LOW
                }
            }
            .setContentIntent(pendingIntentFactory.createOpenSessionPendingIntent(userId))
            .setDeleteIntent(pendingIntentFactory.createDismissSummaryPendingIntent(userId))
            .build()
    }

    override fun createDiagnosticNotification(
        @ColorInt color: Int,
    ): Notification {
        val intent = pendingIntentFactory.createTestPendingIntent()
        return NotificationCompat.Builder(context, notificationChannels.getChannelIdForTest())
            .setContentTitle(buildMeta.applicationName)
            .setContentText(stringProvider.getString(R.string.notification_test_push_notification_content))
            .setSmallIcon(CommonDrawables.ic_notification)
            .setColor(color)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(intent)
            .setDeleteIntent(intent)
            .build()
    }

    override fun createUnregistrationNotification(
        notificationAccountParams: NotificationAccountParams,
    ): Notification {
        val userId = notificationAccountParams.user.userId
        val text = stringProvider.getString(R.string.notification_error_unified_push_unregistered_android)
        return NotificationCompat.Builder(context, notificationChannels.getChannelIdForTest())
            .setSubText(userId.value)
            // The text is long and can be truncated so use BigTextStyle.
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentTitle(stringProvider.getString(CommonStrings.dialog_title_warning))
            .setContentText(text)
            .configureWith(notificationAccountParams)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setAutoCancel(true)
            .setContentIntent(pendingIntentFactory.createOpenSessionPendingIntent(userId))
            .build()
    }

    private suspend fun MessagingStyle.addMessagesFromEvents(
        events: List<NotifiableMessageEvent>,
        imageLoader: ImageLoader,
    ) {
        events.forEach { event ->
            val senderPerson = if (event.outGoingMessage) {
                null
            } else {
                val senderName = event.senderDisambiguatedDisplayName.orEmpty()
                // If the notification is for a mention or reply, we create a fake `Person` with a custom name and key
                val displayName = if (event.hasMentionOrReply) {
                    stringProvider.getString(R.string.notification_sender_mention_reply, senderName)
                } else {
                    senderName
                }
                val key = if (event.hasMentionOrReply) {
                    "mention-or-reply:${event.eventId.value}"
                } else {
                    event.senderId.value
                }
                Person.Builder()
                    .setName(displayName.annotateForDebug(70))
                    .setIcon(
                        bitmapLoader.getUserIcon(
                            avatarData = AvatarData(
                                id = event.senderId.value,
                                name = senderName,
                                url = event.senderAvatarPath,
                                size = AvatarSize.UserHeader,
                            ),
                            imageLoader = imageLoader,
                        )
                    )
                    .setKey(key)
                    .build()
            }
            when {
                event.isSmartReplyError() -> addMessage(
                    stringProvider.getString(R.string.notification_inline_reply_failed),
                    event.timestamp,
                    senderPerson
                )
                else -> {
                    if (event.imageMimeType != null && event.imageUri != null) {
                        // Image case
                        val message = MessagingStyle.Message(
                            // This text will not be rendered, but some systems does not render the image
                            // if the text is null
                            stringProvider.getString(CommonStrings.common_image),
                            event.timestamp,
                            senderPerson,
                        )
                            .setData(event.imageMimeType, event.imageUri)
                        message.extras.putString(MESSAGE_EVENT_ID, event.eventId.value)
                        addMessage(message)
                        // Add additional message for captions
                        if (event.body != null) {
                            addMessage(
                                MessagingStyle.Message(
                                    event.body.annotateForDebug(72),
                                    event.timestamp,
                                    senderPerson,
                                )
                            )
                        }
                    } else {
                        // Text case
                        val message = MessagingStyle.Message(
                            event.body?.annotateForDebug(71),
                            event.timestamp,
                            senderPerson
                        )
                        message.extras.putString(MESSAGE_EVENT_ID, event.eventId.value)
                        addMessage(message)
                    }
                }
            }
        }
    }

    private suspend fun createMessagingStyleFromCurrentUser(
        user: MatrixUser,
        imageLoader: ImageLoader,
        roomName: String,
        isThread: Boolean,
        roomIsGroup: Boolean
    ): MessagingStyle {
        return MessagingStyle(
            Person.Builder()
                // Note: name cannot be empty else NotificationCompat.MessagingStyle() will crash
                .setName(user.getBestName().annotateForDebug(50))
                .setIcon(
                    bitmapLoader.getUserIcon(
                        avatarData = user.getAvatarData(AvatarSize.UserHeader),
                        imageLoader = imageLoader,
                    )
                )
                .setKey(user.userId.value)
                .build()
        ).also {
            it.conversationTitle = if (isThread) {
                stringProvider.getString(R.string.notification_thread_in_room, roomName)
            } else {
                roomName
            }
            // So the avatar is displayed even if they're part of a conversation
            it.isGroupConversation = roomIsGroup || isThread
        }
    }

    companion object {
        const val MESSAGE_EVENT_ID = "message_event_id"
    }
}

private fun NotificationCompat.Builder.configureWith(notificationAccountParams: NotificationAccountParams) = apply {
    setSmallIcon(CommonDrawables.ic_notification)
    setColor(notificationAccountParams.color)
    setGroup(notificationAccountParams.user.userId.value)
    if (notificationAccountParams.showSessionId) {
        setSubText(notificationAccountParams.user.userId.value)
    }
}

fun NotifiableMessageEvent.isSmartReplyError() = outGoingMessage && outGoingMessageFailed
