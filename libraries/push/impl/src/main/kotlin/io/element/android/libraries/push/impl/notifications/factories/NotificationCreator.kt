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

package io.element.android.libraries.push.impl.notifications.factories

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.notifications.RoomEventGroupInfo
import io.element.android.libraries.push.impl.notifications.channels.NotificationChannels
import io.element.android.libraries.push.impl.notifications.debug.annotateForDebug
import io.element.android.libraries.push.impl.notifications.factories.action.MarkAsReadActionFactory
import io.element.android.libraries.push.impl.notifications.factories.action.QuickReplyActionFactory
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import io.element.android.services.toolbox.api.strings.StringProvider
import javax.inject.Inject

class NotificationCreator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationChannels: NotificationChannels,
    private val stringProvider: StringProvider,
    private val buildMeta: BuildMeta,
    private val pendingIntentFactory: PendingIntentFactory,
    private val markAsReadActionFactory: MarkAsReadActionFactory,
    private val quickReplyActionFactory: QuickReplyActionFactory,
) {
    /**
     * Create a notification for a Room.
     */
    fun createMessagesListNotification(
        messageStyle: NotificationCompat.MessagingStyle,
        roomInfo: RoomEventGroupInfo,
        threadId: ThreadId?,
        largeIcon: Bitmap?,
        lastMessageTimestamp: Long,
        tickerText: String
    ): Notification {
        val accentColor = ContextCompat.getColor(context, R.color.notification_accent_color)
        // Build the pending intent for when the notification is clicked
        val openIntent = when {
            threadId != null -> pendingIntentFactory.createOpenThreadPendingIntent(roomInfo, threadId)
            else -> pendingIntentFactory.createOpenRoomPendingIntent(roomInfo.sessionId, roomInfo.roomId)
        }

        val smallIcon = CommonDrawables.ic_notification_small

        val channelId = notificationChannels.getChannelIdForMessage(roomInfo.shouldBing)
        return NotificationCompat.Builder(context, channelId)
            .setOnlyAlertOnce(roomInfo.isUpdated)
            .setWhen(lastMessageTimestamp)
            // MESSAGING_STYLE sets title and content for API 16 and above devices.
            .setStyle(messageStyle)
            // A category allows groups of notifications to be ranked and filtered – per user or system settings.
            // For example, alarm notifications should display before promo notifications, or message from known contact
            // that can be displayed in not disturb mode if white listed (the later will need compat28.x)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            // ID of the corresponding shortcut, for conversation features under API 30+
            .setShortcutId(roomInfo.roomId.value)
            // Title for API < 16 devices.
            .setContentTitle(roomInfo.roomDisplayName.annotateForDebug(1))
            // Content for API < 16 devices.
            .setContentText(stringProvider.getString(R.string.notification_new_messages).annotateForDebug(2))
            // Number of new notifications for API <24 (M and below) devices.
            .setSubText(
                stringProvider.getQuantityString(
                    R.plurals.notification_new_messages_for_room,
                    messageStyle.messages.size,
                    messageStyle.messages.size
                ).annotateForDebug(3)
            )
            // Auto-bundling is enabled for 4 or more notifications on API 24+ (N+)
            // devices and all Wear devices. But we want a custom grouping, so we specify the groupID
            .setGroup(roomInfo.sessionId.value)
            // In order to avoid notification making sound twice (due to the summary notification)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
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

                // Add actions and notification intents
                // Mark room as read
                addAction(markAsReadActionFactory.create(roomInfo))
                // Quick reply
                if (!roomInfo.hasSmartReplyError) {
                    addAction(quickReplyActionFactory.create(roomInfo, threadId))
                }
                if (openIntent != null) {
                    setContentIntent(openIntent)
                }
                if (largeIcon != null) {
                    setLargeIcon(largeIcon)
                }
                setDeleteIntent(pendingIntentFactory.createDismissRoomPendingIntent(roomInfo.sessionId, roomInfo.roomId))
            }
            .setTicker(tickerText.annotateForDebug(4))
            .build()
    }

    fun createRoomInvitationNotification(
        inviteNotifiableEvent: InviteNotifiableEvent
    ): Notification {
        val accentColor = ContextCompat.getColor(context, R.color.notification_accent_color)
        val smallIcon = CommonDrawables.ic_notification_small
        val channelId = notificationChannels.getChannelIdForMessage(inviteNotifiableEvent.noisy)
        return NotificationCompat.Builder(context, channelId)
            .setOnlyAlertOnce(true)
            .setContentTitle((inviteNotifiableEvent.roomName ?: buildMeta.applicationName).annotateForDebug(5))
            .setContentText(inviteNotifiableEvent.description.annotateForDebug(6))
            .setGroup(inviteNotifiableEvent.sessionId.value)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
            .setSmallIcon(smallIcon)
            .setColor(accentColor)
            // TODO removed for now, will be added back later
//            .addAction(rejectInvitationActionFactory.create(inviteNotifiableEvent))
//            .addAction(acceptInvitationActionFactory.create(inviteNotifiableEvent))
            .apply {
                // Build the pending intent for when the notification is clicked
                setContentIntent(pendingIntentFactory.createInviteListPendingIntent(inviteNotifiableEvent.sessionId))

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

    fun createSimpleEventNotification(
        simpleNotifiableEvent: SimpleNotifiableEvent,
    ): Notification {
        val accentColor = ContextCompat.getColor(context, R.color.notification_accent_color)
        val smallIcon = CommonDrawables.ic_notification_small

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
                setAutoCancel(true)
            }
            .build()
    }

    fun createFallbackNotification(
        fallbackNotifiableEvent: FallbackNotifiableEvent,
    ): Notification {
        val accentColor = ContextCompat.getColor(context, R.color.notification_accent_color)
        val smallIcon = CommonDrawables.ic_notification_small

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
            .apply {
                priority = NotificationCompat.PRIORITY_LOW
                setAutoCancel(true)
            }
            .build()
    }

    /**
     * Create the summary notification.
     */
    fun createSummaryListNotification(
        currentUser: MatrixUser,
        style: NotificationCompat.InboxStyle?,
        compatSummary: String,
        noisy: Boolean,
        lastMessageTimestamp: Long
    ): Notification {
        val accentColor = ContextCompat.getColor(context, R.color.notification_accent_color)
        val smallIcon = CommonDrawables.ic_notification_small
        val channelId = notificationChannels.getChannelIdForMessage(noisy)
        return NotificationCompat.Builder(context, channelId)
            .setOnlyAlertOnce(true)
            // used in compat < N, after summary is built based on child notifications
            .setWhen(lastMessageTimestamp)
            .setStyle(style)
            .setContentTitle(currentUser.userId.value.annotateForDebug(9))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setSmallIcon(smallIcon)
            // set content text to support devices running API level < 24
            .setContentText(compatSummary.annotateForDebug(10))
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

    fun createDiagnosticNotification(): Notification {
        val intent = pendingIntentFactory.createTestPendingIntent()
        return NotificationCompat.Builder(context, notificationChannels.getChannelIdForTest())
            .setContentTitle(buildMeta.applicationName)
            .setContentText(stringProvider.getString(R.string.notification_test_push_notification_content))
            .setSmallIcon(CommonDrawables.ic_notification_small)
            .setLargeIcon(getBitmap(R.drawable.element_logo_green))
            .setColor(ContextCompat.getColor(context, R.color.notification_accent_color))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(intent)
            .setDeleteIntent(intent)
            .build()
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
}
