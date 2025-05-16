/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.factories

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.MessagingStyle
import androidx.core.app.Person
import androidx.core.content.res.ResourcesCompat
import coil3.ImageLoader
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appconfig.NotificationConfig
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.timeline.item.event.EventType
import io.element.android.libraries.matrix.api.user.MatrixUser
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
import io.element.android.services.toolbox.api.strings.StringProvider
import javax.inject.Inject

interface NotificationCreator {
    /**
     * Create a notification for a Room.
     */
    suspend fun createMessagesListNotification(
        roomInfo: RoomEventGroupInfo,
        threadId: ThreadId?,
        largeIcon: Bitmap?,
        lastMessageTimestamp: Long,
        tickerText: String,
        currentUser: MatrixUser,
        existingNotification: Notification?,
        imageLoader: ImageLoader,
        events: List<NotifiableMessageEvent>,
    ): Notification

    fun createRoomInvitationNotification(
        inviteNotifiableEvent: InviteNotifiableEvent
    ): Notification

    fun createSimpleEventNotification(
        simpleNotifiableEvent: SimpleNotifiableEvent,
    ): Notification

    fun createFallbackNotification(
        fallbackNotifiableEvent: FallbackNotifiableEvent,
    ): Notification

    /**
     * Create the summary notification.
     */
    fun createSummaryListNotification(
        currentUser: MatrixUser,
        compatSummary: String,
        noisy: Boolean,
        lastMessageTimestamp: Long
    ): Notification

    fun createDiagnosticNotification(): Notification
}

@ContributesBinding(AppScope::class)
class DefaultNotificationCreator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationChannels: NotificationChannels,
    private val stringProvider: StringProvider,
    private val buildMeta: BuildMeta,
    private val pendingIntentFactory: PendingIntentFactory,
    private val markAsReadActionFactory: MarkAsReadActionFactory,
    private val quickReplyActionFactory: QuickReplyActionFactory,
    private val bitmapLoader: NotificationBitmapLoader,
    private val acceptInvitationActionFactory: AcceptInvitationActionFactory,
    private val rejectInvitationActionFactory: RejectInvitationActionFactory
) : NotificationCreator {
    private val accentColor = NotificationConfig.NOTIFICATION_ACCENT_COLOR

    /**
     * Create a notification for a Room.
     */
    override suspend fun createMessagesListNotification(
        roomInfo: RoomEventGroupInfo,
        threadId: ThreadId?,
        largeIcon: Bitmap?,
        lastMessageTimestamp: Long,
        tickerText: String,
        currentUser: MatrixUser,
        existingNotification: Notification?,
        imageLoader: ImageLoader,
        events: List<NotifiableMessageEvent>,
    ): Notification {
        // Build the pending intent for when the notification is clicked
        val openIntent = when {
            threadId != null -> pendingIntentFactory.createOpenThreadPendingIntent(roomInfo, threadId)
            else -> pendingIntentFactory.createOpenRoomPendingIntent(roomInfo.sessionId, roomInfo.roomId)
        }

        val smallIcon = CommonDrawables.ic_notification

        val containsMissedCall = events.any { it.type == EventType.CALL_NOTIFY }
        val channelId = if (containsMissedCall) {
            notificationChannels.getChannelForIncomingCall(false)
        } else {
            notificationChannels.getChannelIdForMessage(noisy = roomInfo.shouldBing)
        }
        val builder = if (existingNotification != null) {
            NotificationCompat.Builder(context, existingNotification)
        } else {
            NotificationCompat.Builder(context, channelId)
                // A category allows groups of notifications to be ranked and filtered – per user or system settings.
                // For example, alarm notifications should display before promo notifications, or message from known contact
                // that can be displayed in not disturb mode if white listed (the later will need compat28.x)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                // ID of the corresponding shortcut, for conversation features under API 30+
                .setShortcutId(roomInfo.roomId.value)
                // Auto-bundling is enabled for 4 or more notifications on API 24+ (N+)
                // devices and all Wear devices. But we want a custom grouping, so we specify the groupID
                .setGroup(roomInfo.sessionId.value)
                // In order to avoid notification making sound twice (due to the summary notification)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
                // Remove notification after opening it or using an action
                .setAutoCancel(true)
        }

        val messagingStyle = existingNotification?.let {
            MessagingStyle.extractMessagingStyleFromNotification(it)
        } ?: messagingStyleFromCurrentUser(roomInfo.sessionId, currentUser, imageLoader, roomInfo.roomDisplayName, !roomInfo.isDm)

        messagingStyle.addMessagesFromEvents(events, imageLoader)

        return builder
            .setNumber(events.size)
            .setOnlyAlertOnce(roomInfo.isUpdated)
            .setWhen(lastMessageTimestamp)
            // MESSAGING_STYLE sets title and content for API 16 and above devices.
            .setStyle(messagingStyle)
            // Not needed anymore?
            // Title for API < 16 devices.
            .setContentTitle(roomInfo.roomDisplayName.annotateForDebug(1))
            // Content for API < 16 devices.
            .setContentText(stringProvider.getString(R.string.notification_new_messages).annotateForDebug(2))
            // Number of new notifications for API <24 (M and below) devices.
            .setSubText(
                stringProvider.getQuantityString(
                    R.plurals.notification_new_messages_for_room,
                    messagingStyle.messages.size,
                    messagingStyle.messages.size
                ).annotateForDebug(3)
            )
            .setSmallIcon(smallIcon)
            // Set primary color (important for Wear 2.0 Notifications).
            .setColor(accentColor)
            // Sets priority for 25 and below. For 26 and above, 'priority' is deprecated for
            // 'importance' which is set in the NotificationChannel. The integers representing
            // 'priority' are different from 'importance', so make sure you don't mix them.
            .apply {
                if (roomInfo.shouldBing) {
                    // Compat
                    priority = NotificationCompat.PRIORITY_DEFAULT
                    /*
                    vectorPreferences.getNotificationRingTone()?.let {
                        setSound(it)
                    }
                     */
                    setLights(accentColor, 500, 500)
                } else {
                    priority = NotificationCompat.PRIORITY_LOW
                }
                // Clear existing actions since we might be updating an existing notification
                clearActions()
                // Add actions and notification intents
                // Mark room as read
                addAction(markAsReadActionFactory.create(roomInfo))
                // Quick reply
                if (!roomInfo.hasSmartReplyError) {
                    val latestEventId = events.lastOrNull()?.eventId
                    addAction(quickReplyActionFactory.create(roomInfo, latestEventId, threadId))
                }
                if (openIntent != null) {
                    setContentIntent(openIntent)
                }
                if (largeIcon != null) {
                    setLargeIcon(largeIcon)
                }
                setDeleteIntent(pendingIntentFactory.createDismissRoomPendingIntent(roomInfo.sessionId, roomInfo.roomId))

                // If any of the events are of call notify type it means a missed call, set the category to the right value
                if (events.any { it.type == EventType.CALL_NOTIFY }) {
                    setCategory(NotificationCompat.CATEGORY_MISSED_CALL)
                }
            }
            .setTicker(tickerText)
            .build()
    }

    override fun createRoomInvitationNotification(
        inviteNotifiableEvent: InviteNotifiableEvent
    ): Notification {
        val smallIcon = CommonDrawables.ic_notification
        val channelId = notificationChannels.getChannelIdForMessage(inviteNotifiableEvent.noisy)
        return NotificationCompat.Builder(context, channelId)
            .setOnlyAlertOnce(true)
            .setContentTitle((inviteNotifiableEvent.roomName ?: buildMeta.applicationName).annotateForDebug(5))
            .setContentText(inviteNotifiableEvent.description.annotateForDebug(6))
            .setGroup(inviteNotifiableEvent.sessionId.value)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
            .setSmallIcon(smallIcon)
            .setColor(accentColor)
            .apply {
                addAction(rejectInvitationActionFactory.create(inviteNotifiableEvent))
                addAction(acceptInvitationActionFactory.create(inviteNotifiableEvent))
                // Build the pending intent for when the notification is clicked
                setContentIntent(pendingIntentFactory.createOpenRoomPendingIntent(inviteNotifiableEvent.sessionId, inviteNotifiableEvent.roomId))

                if (inviteNotifiableEvent.noisy) {
                    // Compat
                    priority = NotificationCompat.PRIORITY_DEFAULT
                    /*
                    vectorPreferences.getNotificationRingTone()?.let {
                        setSound(it)
                    }
                     */
                    setLights(accentColor, 500, 500)
                } else {
                    priority = NotificationCompat.PRIORITY_LOW
                }
                setDeleteIntent(
                    pendingIntentFactory.createDismissInvitePendingIntent(
                        inviteNotifiableEvent.sessionId,
                        inviteNotifiableEvent.roomId,
                    )
                )
                setAutoCancel(true)
            }
            .build()
    }

    override fun createSimpleEventNotification(
        simpleNotifiableEvent: SimpleNotifiableEvent,
    ): Notification {
        val smallIcon = CommonDrawables.ic_notification

        val channelId = notificationChannels.getChannelIdForMessage(simpleNotifiableEvent.noisy)
        return NotificationCompat.Builder(context, channelId)
            .setOnlyAlertOnce(true)
            .setContentTitle(buildMeta.applicationName.annotateForDebug(7))
            .setContentText(simpleNotifiableEvent.description.annotateForDebug(8))
            .setGroup(simpleNotifiableEvent.sessionId.value)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
            .setSmallIcon(smallIcon)
            .setColor(accentColor)
            .setAutoCancel(true)
            .setContentIntent(pendingIntentFactory.createOpenRoomPendingIntent(simpleNotifiableEvent.sessionId, simpleNotifiableEvent.roomId))
            .apply {
                if (simpleNotifiableEvent.noisy) {
                    // Compat
                    priority = NotificationCompat.PRIORITY_DEFAULT
                    /*
                    vectorPreferences.getNotificationRingTone()?.let {
                        setSound(it)
                    }
                     */
                    setLights(accentColor, 500, 500)
                } else {
                    priority = NotificationCompat.PRIORITY_LOW
                }
            }
            .build()
    }

    override fun createFallbackNotification(
        fallbackNotifiableEvent: FallbackNotifiableEvent,
    ): Notification {
        val smallIcon = CommonDrawables.ic_notification

        val channelId = notificationChannels.getChannelIdForMessage(false)
        return NotificationCompat.Builder(context, channelId)
            .setOnlyAlertOnce(true)
            .setContentTitle(buildMeta.applicationName.annotateForDebug(7))
            .setContentText(fallbackNotifiableEvent.description.orEmpty().annotateForDebug(8))
            .setGroup(fallbackNotifiableEvent.sessionId.value)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
            .setSmallIcon(smallIcon)
            .setColor(accentColor)
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
        currentUser: MatrixUser,
        compatSummary: String,
        noisy: Boolean,
        lastMessageTimestamp: Long
    ): Notification {
        val smallIcon = CommonDrawables.ic_notification
        val channelId = notificationChannels.getChannelIdForMessage(noisy)
        return NotificationCompat.Builder(context, channelId)
            .setOnlyAlertOnce(true)
            // used in compat < N, after summary is built based on child notifications
            .setWhen(lastMessageTimestamp)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setSmallIcon(smallIcon)
            .setGroup(currentUser.userId.value)
            // set this notification as the summary for the group
            .setGroupSummary(true)
            .setColor(accentColor)
            .apply {
                if (noisy) {
                    // Compat
                    priority = NotificationCompat.PRIORITY_DEFAULT
                    /*
                    vectorPreferences.getNotificationRingTone()?.let {
                        setSound(it)
                    }
                     */
                    setLights(accentColor, 500, 500)
                } else {
                    // compat
                    priority = NotificationCompat.PRIORITY_LOW
                }
            }
            .setContentIntent(pendingIntentFactory.createOpenSessionPendingIntent(currentUser.userId))
            .setDeleteIntent(pendingIntentFactory.createDismissSummaryPendingIntent(currentUser.userId))
            .build()
    }

    override fun createDiagnosticNotification(): Notification {
        val intent = pendingIntentFactory.createTestPendingIntent()
        return NotificationCompat.Builder(context, notificationChannels.getChannelIdForTest())
            .setContentTitle(buildMeta.applicationName)
            .setContentText(stringProvider.getString(R.string.notification_test_push_notification_content))
            .setSmallIcon(CommonDrawables.ic_notification)
            .setLargeIcon(getBitmap(R.drawable.element_logo_green))
            .setColor(accentColor)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(intent)
            .setDeleteIntent(intent)
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
                    .setIcon(bitmapLoader.getUserIcon(event.senderAvatarPath, imageLoader))
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
                    val message = MessagingStyle.Message(
                        event.body?.annotateForDebug(71),
                        event.timestamp,
                        senderPerson
                    ).also { message ->
                        event.imageUri?.let {
                            message.setData(event.imageMimeType ?: "image/", it)
                        }
                        message.extras.putString(MESSAGE_EVENT_ID, event.eventId.value)
                    }
                    addMessage(message)

                    // Add additional message for captions
                    if (event.imageUri != null && event.body != null) {
                        addMessage(
                            MessagingStyle.Message(
                                event.body,
                                event.timestamp,
                                senderPerson,
                            )
                        )
                    }
                }
            }
        }
    }

    private suspend fun messagingStyleFromCurrentUser(
        sessionId: SessionId,
        user: MatrixUser,
        imageLoader: ImageLoader,
        roomName: String,
        roomIsGroup: Boolean
    ): MessagingStyle {
        return MessagingStyle(
            Person.Builder()
                .setName(user.displayName?.annotateForDebug(50))
                .setIcon(bitmapLoader.getUserIcon(user.avatarUrl, imageLoader))
                .setKey(sessionId.value)
                .build()
        ).also {
            it.conversationTitle = roomName.takeIf { roomIsGroup }
            it.isGroupConversation = roomIsGroup
        }
    }

    private fun getBitmap(@DrawableRes drawableRes: Int): Bitmap? {
        val drawable = ResourcesCompat.getDrawable(context.resources, drawableRes, null) ?: return null
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    companion object {
        const val MESSAGE_EVENT_ID = "message_event_id"
    }
}

fun NotifiableMessageEvent.isSmartReplyError() = outGoingMessage && outGoingMessageFailed
