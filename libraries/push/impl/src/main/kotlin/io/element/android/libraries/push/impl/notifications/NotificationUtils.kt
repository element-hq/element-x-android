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

@file:Suppress("UNUSED_PARAMETER")

package io.element.android.libraries.push.impl.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import io.element.android.libraries.androidutils.system.startNotificationChannelSettingsIntent
import io.element.android.libraries.androidutils.uri.createIgnoredUri
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.intent.IntentProvider
import io.element.android.libraries.push.impl.notifications.actions.AcceptInvitationActionFactory
import io.element.android.libraries.push.impl.notifications.actions.MarkAsReadActionFactory
import io.element.android.libraries.push.impl.notifications.actions.QuickReplyActionFactory
import io.element.android.libraries.push.impl.notifications.actions.RejectInvitationActionFactory
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import io.element.android.services.toolbox.api.strings.StringProvider
import io.element.android.services.toolbox.api.systemclock.SystemClock
import timber.log.Timber
import javax.inject.Inject

// TODO EAx Split into factories
@SingleIn(AppScope::class)
class NotificationUtils @Inject constructor(
    @ApplicationContext private val context: Context,
    // private val vectorPreferences: VectorPreferences,
    private val stringProvider: StringProvider,
    private val clock: SystemClock,
    private val actionIds: NotificationActionIds,
    private val intentProvider: IntentProvider,
    private val buildMeta: BuildMeta,
    private val markAsReadActionFactory: MarkAsReadActionFactory,
    private val quickReplyActionFactory: QuickReplyActionFactory,
    private val rejectInvitationActionFactory: RejectInvitationActionFactory,
    private val acceptInvitationActionFactory: AcceptInvitationActionFactory,
) {

    companion object {
        /* ==========================================================================================
         * IDs for notifications
         * ========================================================================================== */

        /**
         * Identifier of the foreground notification used to keep the application alive
         * when it runs in background.
         * This notification, which is not removable by the end user, displays what
         * the application is doing while in background.
         */
        const val NOTIFICATION_ID_FOREGROUND_SERVICE = 61

        /* ==========================================================================================
         * IDs for channels
         * ========================================================================================== */

        // on devices >= android O, we need to define a channel for each notifications
        private const val LISTENING_FOR_EVENTS_NOTIFICATION_CHANNEL_ID = "LISTEN_FOR_EVENTS_NOTIFICATION_CHANNEL_ID"

        private const val NOISY_NOTIFICATION_CHANNEL_ID = "DEFAULT_NOISY_NOTIFICATION_CHANNEL_ID"

        const val SILENT_NOTIFICATION_CHANNEL_ID = "DEFAULT_SILENT_NOTIFICATION_CHANNEL_ID_V2"
        private const val CALL_NOTIFICATION_CHANNEL_ID = "CALL_NOTIFICATION_CHANNEL_ID_V2"

        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
        fun supportNotificationChannels() = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)

        fun openSystemSettingsForSilentCategory(activity: Activity) {
            startNotificationChannelSettingsIntent(activity, SILENT_NOTIFICATION_CHANNEL_ID)
        }

        fun openSystemSettingsForNoisyCategory(activity: Activity) {
            startNotificationChannelSettingsIntent(activity, NOISY_NOTIFICATION_CHANNEL_ID)
        }

        fun openSystemSettingsForCallCategory(activity: Activity) {
            startNotificationChannelSettingsIntent(activity, CALL_NOTIFICATION_CHANNEL_ID)
        }
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannels()
    }

    /* ==========================================================================================
     * Channel names
     * ========================================================================================== */

    /**
     * Create notification channels.
     */
    private fun createNotificationChannels() {
        if (!supportNotificationChannels()) {
            return
        }

        val accentColor = ContextCompat.getColor(context, R.color.notification_accent_color)

        // Migration - the noisy channel was deleted and recreated when sound preference was changed (id was DEFAULT_NOISY_NOTIFICATION_CHANNEL_ID_BASE
        // + currentTimeMillis).
        // Now the sound can only be change directly in system settings, so for app upgrading we are deleting this former channel
        // Starting from this version the channel will not be dynamic
        for (channel in notificationManager.notificationChannels) {
            val channelId = channel.id
            val legacyBaseName = "DEFAULT_NOISY_NOTIFICATION_CHANNEL_ID_BASE"
            if (channelId.startsWith(legacyBaseName)) {
                notificationManager.deleteNotificationChannel(channelId)
            }
        }
        // Migration - Remove deprecated channels
        for (channelId in listOf("DEFAULT_SILENT_NOTIFICATION_CHANNEL_ID", "CALL_NOTIFICATION_CHANNEL_ID")) {
            notificationManager.getNotificationChannel(channelId)?.let {
                notificationManager.deleteNotificationChannel(channelId)
            }
        }

        /**
         * Default notification importance: shows everywhere, makes noise, but does not visually
         * intrude.
         */
        notificationManager.createNotificationChannel(NotificationChannel(
            NOISY_NOTIFICATION_CHANNEL_ID,
            stringProvider.getString(R.string.notification_channel_noisy).ifEmpty { "Noisy notifications" },
            NotificationManager.IMPORTANCE_DEFAULT
        )
            .apply {
                description = stringProvider.getString(R.string.notification_channel_noisy)
                enableVibration(true)
                enableLights(true)
                lightColor = accentColor
            })

        /**
         * Low notification importance: shows everywhere, but is not intrusive.
         */
        notificationManager.createNotificationChannel(NotificationChannel(
            SILENT_NOTIFICATION_CHANNEL_ID,
            stringProvider.getString(R.string.notification_channel_silent).ifEmpty { "Silent notifications" },
            NotificationManager.IMPORTANCE_LOW
        )
            .apply {
                description = stringProvider.getString(R.string.notification_channel_silent)
                setSound(null, null)
                enableLights(true)
                lightColor = accentColor
            })

        notificationManager.createNotificationChannel(NotificationChannel(
            LISTENING_FOR_EVENTS_NOTIFICATION_CHANNEL_ID,
            stringProvider.getString(R.string.notification_channel_listening_for_events).ifEmpty { "Listening for events" },
            NotificationManager.IMPORTANCE_MIN
        )
            .apply {
                description = stringProvider.getString(R.string.notification_channel_listening_for_events)
                setSound(null, null)
                setShowBadge(false)
            })

        notificationManager.createNotificationChannel(NotificationChannel(
            CALL_NOTIFICATION_CHANNEL_ID,
            stringProvider.getString(R.string.notification_channel_call).ifEmpty { "Call" },
            NotificationManager.IMPORTANCE_HIGH
        )
            .apply {
                description = stringProvider.getString(R.string.notification_channel_call)
                setSound(null, null)
                enableLights(true)
                lightColor = accentColor
            })
    }

    fun getChannel(channelId: String): NotificationChannel? {
        return notificationManager.getNotificationChannel(channelId)
    }

    fun getChannelForIncomingCall(fromBg: Boolean): NotificationChannel? {
        val notificationChannel = if (fromBg) CALL_NOTIFICATION_CHANNEL_ID else SILENT_NOTIFICATION_CHANNEL_ID
        return getChannel(notificationChannel)
    }

    /**
     * Build a notification for a Room.
     */
    fun buildMessagesListNotification(
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
            threadId != null &&
                true
                /** TODO EAx vectorPreferences.areThreadMessagesEnabled() */
            -> buildOpenThreadIntent(roomInfo, threadId)

            else -> buildOpenRoomIntent(roomInfo.sessionId, roomInfo.roomId)
        }

        val smallIcon = R.drawable.ic_notification

        val channelID = if (roomInfo.shouldBing) NOISY_NOTIFICATION_CHANNEL_ID else SILENT_NOTIFICATION_CHANNEL_ID
        return NotificationCompat.Builder(context, channelID)
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
            .setContentTitle(roomInfo.roomDisplayName)
            // Content for API < 16 devices.
            .setContentText(stringProvider.getString(R.string.notification_new_messages))
            // Number of new notifications for API <24 (M and below) devices.
            .setSubText(
                stringProvider.getQuantityString(
                    R.plurals.notification_new_messages_for_room,
                    messageStyle.messages.size,
                    messageStyle.messages.size
                )
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
                setDeleteIntent(getDismissRoomPendingIntent(roomInfo))
            }
            .setTicker(tickerText)
            .build()
    }

    fun buildRoomInvitationNotification(
        inviteNotifiableEvent: InviteNotifiableEvent
    ): Notification {
        val accentColor = ContextCompat.getColor(context, R.color.notification_accent_color)
        val smallIcon = R.drawable.ic_notification
        val channelID = if (inviteNotifiableEvent.noisy) NOISY_NOTIFICATION_CHANNEL_ID else SILENT_NOTIFICATION_CHANNEL_ID

        return NotificationCompat.Builder(context, channelID)
            .setOnlyAlertOnce(true)
            .setContentTitle(inviteNotifiableEvent.roomName ?: buildMeta.applicationName)
            .setContentText(inviteNotifiableEvent.description)
            .setGroup(inviteNotifiableEvent.sessionId.value)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
            .setSmallIcon(smallIcon)
            .setColor(accentColor)
            .addAction(rejectInvitationActionFactory.create(inviteNotifiableEvent))
            .addAction(acceptInvitationActionFactory.create(inviteNotifiableEvent))
            .apply {
                /*
                // Build the pending intent for when the notification is clicked
                val contentIntent = HomeActivity.newIntent(
                    context,
                    firstStartMainActivity = true,
                    inviteNotificationRoomId = inviteNotifiableEvent.roomId
                )
                contentIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                // pending intent get reused by system, this will mess up the extra params, so put unique info to avoid that
                contentIntent.data = createIgnoredUri(inviteNotifiableEvent.eventId)
                setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, PendingIntentCompat.FLAG_IMMUTABLE))

                 */

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
                setAutoCancel(true)
            }
            .build()
    }

    fun buildSimpleEventNotification(
        simpleNotifiableEvent: SimpleNotifiableEvent,
    ): Notification {
        val accentColor = ContextCompat.getColor(context, R.color.notification_accent_color)
        val smallIcon = R.drawable.ic_notification

        val channelID = if (simpleNotifiableEvent.noisy) NOISY_NOTIFICATION_CHANNEL_ID else SILENT_NOTIFICATION_CHANNEL_ID

        return NotificationCompat.Builder(context, channelID)
            .setOnlyAlertOnce(true)
            .setContentTitle(buildMeta.applicationName)
            .setContentText(simpleNotifiableEvent.description)
            .setGroup(simpleNotifiableEvent.sessionId.value)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
            .setSmallIcon(smallIcon)
            .setColor(accentColor)
            .setAutoCancel(true)
            .setContentIntent(buildOpenRoomIntent(simpleNotifiableEvent.sessionId, simpleNotifiableEvent.roomId))
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

    private fun buildOpenSessionIntent(sessionId: SessionId): PendingIntent? {
        return getPendingIntent(sessionId = sessionId, roomId = null, threadId = null)
    }

    private fun buildOpenRoomIntent(sessionId: SessionId, roomId: RoomId): PendingIntent? {
        return getPendingIntent(sessionId = sessionId, roomId = roomId, threadId = null)
    }

    private fun buildOpenThreadIntent(roomInfo: RoomEventGroupInfo, threadId: ThreadId?): PendingIntent? {
        return getPendingIntent(sessionId = roomInfo.sessionId, roomId = roomInfo.roomId, threadId = threadId)
    }

    private fun getPendingIntent(sessionId: SessionId, roomId: RoomId?, threadId: ThreadId?): PendingIntent? {
        val intent = intentProvider.getViewIntent(sessionId = sessionId, roomId = roomId, threadId = threadId)
        return PendingIntent.getActivity(
            context,
            clock.epochMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Build the summary notification.
     */
    fun buildSummaryListNotification(
        sessionId: SessionId,
        style: NotificationCompat.InboxStyle?,
        compatSummary: String,
        noisy: Boolean,
        lastMessageTimestamp: Long
    ): Notification {
        val accentColor = ContextCompat.getColor(context, R.color.notification_accent_color)
        val smallIcon = R.drawable.ic_notification

        return NotificationCompat.Builder(context, if (noisy) NOISY_NOTIFICATION_CHANNEL_ID else SILENT_NOTIFICATION_CHANNEL_ID)
            .setOnlyAlertOnce(true)
            // used in compat < N, after summary is built based on child notifications
            .setWhen(lastMessageTimestamp)
            .setStyle(style)
            .setContentTitle(sessionId.value)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setSmallIcon(smallIcon)
            // set content text to support devices running API level < 24
            .setContentText(compatSummary)
            .setGroup(sessionId.value)
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
            .setContentIntent(buildOpenSessionIntent(sessionId))
            .setDeleteIntent(getDismissSummaryPendingIntent(sessionId))
            .build()
    }

    private fun getDismissSummaryPendingIntent(sessionId: SessionId): PendingIntent {
        val intent = Intent(context, NotificationBroadcastReceiver::class.java)
        intent.action = actionIds.dismissSummary
        intent.data = createIgnoredUri("deleteSummary?$sessionId")
        intent.putExtra(NotificationBroadcastReceiver.KEY_SESSION_ID, sessionId)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getDismissRoomPendingIntent(roomInfo: RoomEventGroupInfo): PendingIntent {
        val intent = Intent(context, NotificationBroadcastReceiver::class.java)
        intent.action = actionIds.dismissRoom
        intent.putExtra(NotificationBroadcastReceiver.KEY_SESSION_ID, roomInfo.sessionId)
        intent.putExtra(NotificationBroadcastReceiver.KEY_ROOM_ID, roomInfo.roomId)
        return PendingIntent.getBroadcast(
            context,
            clock.epochMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Cancel the foreground notification service.
     */
    fun cancelNotificationForegroundService() {
        notificationManager.cancel(NOTIFICATION_ID_FOREGROUND_SERVICE)
    }

    /**
     * Cancel all the notification.
     */
    fun cancelAllNotifications() {
        // Keep this try catch (reported by GA)
        try {
            notificationManager.cancelAll()
        } catch (e: Exception) {
            Timber.e(e, "## cancelAllNotifications() failed")
        }
    }

    @SuppressLint("LaunchActivityFromNotification")
    fun displayDiagnosticNotification() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Timber.w("Not allowed to notify.")
            return
        }
        val testActionIntent = Intent(context, TestNotificationReceiver::class.java)
        testActionIntent.action = actionIds.diagnostic
        val testPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            testActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        notificationManager.notify(
            "DIAGNOSTIC",
            888,
            NotificationCompat.Builder(context, NOISY_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(buildMeta.applicationName)
                .setContentText(stringProvider.getString(R.string.notification_test_push_notification_content))
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(getBitmap(context, R.drawable.element_logo_green))
                .setColor(ContextCompat.getColor(context, R.color.notification_accent_color))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setAutoCancel(true)
                .setContentIntent(testPendingIntent)
                .build()
        )
    }

    private fun getBitmap(context: Context, @DrawableRes drawableRes: Int): Bitmap? {
        val drawable = ResourcesCompat.getDrawable(context.resources, drawableRes, null) ?: return null
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * Return true it the user has enabled the do not disturb mode.
     */
    fun isDoNotDisturbModeOn(): Boolean {
        // We cannot use NotificationManagerCompat here.
        val setting = context.getSystemService<NotificationManager>()!!.currentInterruptionFilter

        return setting == NotificationManager.INTERRUPTION_FILTER_NONE ||
            setting == NotificationManager.INTERRUPTION_FILTER_ALARMS
    }

    /*
    private fun getActionText(@StringRes stringRes: Int, @AttrRes colorRes: Int): Spannable {
        return SpannableString(context.getText(stringRes)).apply {
            val foregroundColorSpan = ForegroundColorSpan(ThemeUtils.getColor(context, colorRes))
            setSpan(foregroundColorSpan, 0, length, 0)
        }
    }
     */

    private fun ensureTitleNotEmpty(title: String?): CharSequence {
        if (title.isNullOrBlank()) {
            return buildMeta.applicationName
        }

        return title
    }
}
