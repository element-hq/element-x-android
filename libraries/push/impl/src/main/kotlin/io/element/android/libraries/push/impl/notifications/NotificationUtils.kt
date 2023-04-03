/*
 * Copyright 2018 New Vector Ltd
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

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import io.element.android.libraries.androidutils.intent.PendingIntentCompat
import io.element.android.libraries.androidutils.system.startNotificationChannelSettingsIntent
import io.element.android.libraries.androidutils.uri.createIgnoredUri
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.intent.IntentProvider
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import io.element.android.services.toolbox.api.strings.StringProvider
import io.element.android.services.toolbox.api.systemclock.SystemClock
import timber.log.Timber
import javax.inject.Inject
import io.element.android.libraries.ui.strings.R as StringR

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
            stringProvider.getString(R.string.notification_noisy_notifications).ifEmpty { "Noisy notifications" },
            NotificationManager.IMPORTANCE_DEFAULT
        )
            .apply {
                description = stringProvider.getString(R.string.notification_noisy_notifications)
                enableVibration(true)
                enableLights(true)
                lightColor = accentColor
            })

        /**
         * Low notification importance: shows everywhere, but is not intrusive.
         */
        notificationManager.createNotificationChannel(NotificationChannel(
            SILENT_NOTIFICATION_CHANNEL_ID,
            stringProvider.getString(R.string.notification_silent_notifications).ifEmpty { "Silent notifications" },
            NotificationManager.IMPORTANCE_LOW
        )
            .apply {
                description = stringProvider.getString(R.string.notification_silent_notifications)
                setSound(null, null)
                enableLights(true)
                lightColor = accentColor
            })

        notificationManager.createNotificationChannel(NotificationChannel(
            LISTENING_FOR_EVENTS_NOTIFICATION_CHANNEL_ID,
            stringProvider.getString(R.string.notification_listening_for_events).ifEmpty { "Listening for events" },
            NotificationManager.IMPORTANCE_MIN
        )
            .apply {
                description = stringProvider.getString(R.string.notification_listening_for_events)
                setSound(null, null)
                setShowBadge(false)
            })

        notificationManager.createNotificationChannel(NotificationChannel(
            CALL_NOTIFICATION_CHANNEL_ID,
            stringProvider.getString(R.string.call).ifEmpty { "Call" },
            NotificationManager.IMPORTANCE_HIGH
        )
            .apply {
                description = stringProvider.getString(R.string.call)
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
        threadId: String?,
        largeIcon: Bitmap?,
        lastMessageTimestamp: Long,
        senderDisplayNameForReplyCompat: String?,
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
            .setShortcutId(roomInfo.roomId)
            // Title for API < 16 devices.
            .setContentTitle(roomInfo.roomDisplayName)
            // Content for API < 16 devices.
            .setContentText(stringProvider.getString(R.string.notification_new_messages))
            // Number of new notifications for API <24 (M and below) devices.
            .setSubText(
                stringProvider.getQuantityString(
                    R.plurals.room_new_messages_notification,
                    messageStyle.messages.size,
                    messageStyle.messages.size
                )
            )
            // Auto-bundling is enabled for 4 or more notifications on API 24+ (N+)
            // devices and all Wear devices. But we want a custom grouping, so we specify the groupID
            .setGroup(roomInfo.sessionId)
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
                val markRoomReadIntent = Intent(context, NotificationBroadcastReceiver::class.java)
                markRoomReadIntent.action = actionIds.markRoomRead
                markRoomReadIntent.data = createIgnoredUri("markRead?${roomInfo.sessionId}&$${roomInfo.roomId}")
                markRoomReadIntent.putExtra(NotificationBroadcastReceiver.KEY_SESSION_ID, roomInfo.sessionId)
                markRoomReadIntent.putExtra(NotificationBroadcastReceiver.KEY_ROOM_ID, roomInfo.roomId)
                val markRoomReadPendingIntent = PendingIntent.getBroadcast(
                    context,
                    clock.epochMillis().toInt(),
                    markRoomReadIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntentCompat.FLAG_IMMUTABLE
                )

                NotificationCompat.Action.Builder(
                    R.drawable.ic_material_done_all_white,
                    stringProvider.getString(R.string.action_mark_room_read), markRoomReadPendingIntent
                )
                    .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
                    .setShowsUserInterface(false)
                    .build()
                    .let { addAction(it) }

                // Quick reply
                if (!roomInfo.hasSmartReplyError) {
                    buildQuickReplyIntent(roomInfo.sessionId, roomInfo.roomId, threadId, senderDisplayNameForReplyCompat)?.let { replyPendingIntent ->
                        val remoteInput = RemoteInput.Builder(NotificationBroadcastReceiver.KEY_TEXT_REPLY)
                            .setLabel(stringProvider.getString(StringR.string.action_quick_reply))
                            .build()
                        NotificationCompat.Action.Builder(
                            R.drawable.vector_notification_quick_reply,
                            stringProvider.getString(StringR.string.action_quick_reply), replyPendingIntent
                        )
                            .addRemoteInput(remoteInput)
                            .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                            .setShowsUserInterface(false)
                            .build()
                            .let { addAction(it) }
                    }
                }

                if (openIntent != null) {
                    setContentIntent(openIntent)
                }

                if (largeIcon != null) {
                    setLargeIcon(largeIcon)
                }

                val intent = Intent(context, NotificationBroadcastReceiver::class.java)
                intent.action = actionIds.dismissRoom
                intent.putExtra(NotificationBroadcastReceiver.KEY_SESSION_ID, roomInfo.sessionId)
                intent.putExtra(NotificationBroadcastReceiver.KEY_ROOM_ID, roomInfo.roomId)
                val pendingIntent = PendingIntent.getBroadcast(
                    context.applicationContext,
                    clock.epochMillis().toInt(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntentCompat.FLAG_IMMUTABLE
                )
                setDeleteIntent(pendingIntent)
            }
            .setTicker(tickerText)
            .build()
    }

    fun buildRoomInvitationNotification(
        inviteNotifiableEvent: InviteNotifiableEvent
    ): Notification {
        val accentColor = ContextCompat.getColor(context, R.color.notification_accent_color)
        // Build the pending intent for when the notification is clicked
        val smallIcon = R.drawable.ic_notification
        val channelID = if (inviteNotifiableEvent.noisy) NOISY_NOTIFICATION_CHANNEL_ID else SILENT_NOTIFICATION_CHANNEL_ID

        return NotificationCompat.Builder(context, channelID)
            .setOnlyAlertOnce(true)
            .setContentTitle(inviteNotifiableEvent.roomName ?: buildMeta.applicationName)
            .setContentText(inviteNotifiableEvent.description)
            .setGroup(inviteNotifiableEvent.sessionId)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
            .setSmallIcon(smallIcon)
            .setColor(accentColor)
            .apply {
                val roomId = inviteNotifiableEvent.roomId
                // offer to type a quick reject button
                val rejectIntent = Intent(context, NotificationBroadcastReceiver::class.java)
                rejectIntent.action = actionIds.reject
                rejectIntent.data = createIgnoredUri("rejectInvite?${inviteNotifiableEvent.sessionId}&$roomId")
                rejectIntent.putExtra(NotificationBroadcastReceiver.KEY_SESSION_ID, inviteNotifiableEvent.sessionId)
                rejectIntent.putExtra(NotificationBroadcastReceiver.KEY_ROOM_ID, roomId)
                val rejectIntentPendingIntent = PendingIntent.getBroadcast(
                    context,
                    clock.epochMillis().toInt(),
                    rejectIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntentCompat.FLAG_IMMUTABLE
                )

                addAction(
                    R.drawable.vector_notification_reject_invitation,
                    stringProvider.getString(R.string.action_reject),
                    rejectIntentPendingIntent
                )

                // offer to type a quick accept button
                val joinIntent = Intent(context, NotificationBroadcastReceiver::class.java)
                joinIntent.action = actionIds.join
                joinIntent.data = createIgnoredUri("acceptInvite?${inviteNotifiableEvent.sessionId}&$roomId")
                joinIntent.putExtra(NotificationBroadcastReceiver.KEY_SESSION_ID, inviteNotifiableEvent.sessionId)
                joinIntent.putExtra(NotificationBroadcastReceiver.KEY_ROOM_ID, roomId)
                val joinIntentPendingIntent = PendingIntent.getBroadcast(
                    context,
                    clock.epochMillis().toInt(),
                    joinIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntentCompat.FLAG_IMMUTABLE
                )
                addAction(
                    R.drawable.vector_notification_accept_invitation,
                    stringProvider.getString(R.string.action_join),
                    joinIntentPendingIntent
                )

                /*
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
        // Build the pending intent for when the notification is clicked
        val smallIcon = R.drawable.ic_notification

        val channelID = if (simpleNotifiableEvent.noisy) NOISY_NOTIFICATION_CHANNEL_ID else SILENT_NOTIFICATION_CHANNEL_ID

        return NotificationCompat.Builder(context, channelID)
            .setOnlyAlertOnce(true)
            .setContentTitle(buildMeta.applicationName)
            .setContentText(simpleNotifiableEvent.description)
            .setGroup(simpleNotifiableEvent.sessionId)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL)
            .setSmallIcon(smallIcon)
            .setColor(accentColor)
            .setAutoCancel(true)
            .apply {
                /* TODO EAx
                val contentIntent = HomeActivity.newIntent(context, firstStartMainActivity = true)
                contentIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                // pending intent get reused by system, this will mess up the extra params, so put unique info to avoid that
                contentIntent.data = createIgnoredUri(simpleNotifiableEvent.eventId)
                setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, PendingIntentCompat.FLAG_IMMUTABLE))
                 */
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

    private fun buildOpenRoomIntent(sessionId: String, roomId: String): PendingIntent? {
        val roomIntent = intentProvider.getIntent(sessionId = sessionId, roomId = roomId, threadId = null)
        roomIntent.action = actionIds.tapToView
        // pending intent get reused by system, this will mess up the extra params, so put unique info to avoid that
        roomIntent.data = createIgnoredUri("openRoom?$sessionId&$roomId")

        return PendingIntent.getActivity(
            context,
            clock.epochMillis().toInt(),
            roomIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntentCompat.FLAG_IMMUTABLE
        )
    }

    private fun buildOpenThreadIntent(roomInfo: RoomEventGroupInfo, threadId: String?): PendingIntent? {
        val sessionId = roomInfo.sessionId
        val roomId = roomInfo.roomId
        val threadIntentTap = intentProvider.getIntent(sessionId = sessionId, roomId = roomId, threadId = threadId)
        threadIntentTap.action = actionIds.tapToView
        // pending intent get reused by system, this will mess up the extra params, so put unique info to avoid that
        threadIntentTap.data = createIgnoredUri("openThread?$sessionId&$roomId&$threadId")

        return PendingIntent.getActivity(
            context,
            clock.epochMillis().toInt(),
            threadIntentTap,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntentCompat.FLAG_IMMUTABLE
        )
    }

    private fun buildOpenHomePendingIntentForSummary(sessionId: String): PendingIntent {
        val intent = intentProvider.getIntent(sessionId = sessionId, roomId = null, threadId = null)
        intent.data = createIgnoredUri("tapSummary?$sessionId")
        return PendingIntent.getActivity(
            context,
            clock.epochMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntentCompat.FLAG_IMMUTABLE
        )
    }

    /*
        Direct reply is new in Android N, and Android already handles the UI, so the right pending intent
        here will ideally be a Service/IntentService (for a long running background task) or a BroadcastReceiver,
         which runs on the UI thread. It also works without unlocking, making the process really fluid for the user.
        However, for Android devices running Marshmallow and below (API level 23 and below),
        it will be more appropriate to use an activity. Since you have to provide your own UI.
     */
    private fun buildQuickReplyIntent(
        sessionId: String,
        roomId: String,
        threadId: String?,
        senderName: String?
    ): PendingIntent? {
        val intent: Intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = Intent(context, NotificationBroadcastReceiver::class.java)
            intent.action = actionIds.smartReply
            intent.data = createIgnoredUri("quickReply?$sessionId&$roomId")
            intent.putExtra(NotificationBroadcastReceiver.KEY_SESSION_ID, sessionId)
            intent.putExtra(NotificationBroadcastReceiver.KEY_ROOM_ID, roomId)
            threadId?.let {
                intent.putExtra(NotificationBroadcastReceiver.KEY_THREAD_ID, it)
            }

            return PendingIntent.getBroadcast(
                context,
                clock.epochMillis().toInt(),
                intent,
                // PendingIntents attached to actions with remote inputs must be mutable
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntentCompat.FLAG_MUTABLE
            )
        } else {
            /*
            TODO
            if (!LockScreenActivity.isDisplayingALockScreenActivity()) {
                // start your activity for Android M and below
                val quickReplyIntent = Intent(context, LockScreenActivity::class.java)
                quickReplyIntent.putExtra(LockScreenActivity.EXTRA_ROOM_ID, roomId)
                quickReplyIntent.putExtra(LockScreenActivity.EXTRA_SENDER_NAME, senderName ?: "")

                // the action must be unique else the parameters are ignored
                quickReplyIntent.action = QUICK_LAUNCH_ACTION
                quickReplyIntent.data = createIgnoredUri($roomId")
                return PendingIntent.getActivity(context, 0, quickReplyIntent, PendingIntentCompat.FLAG_IMMUTABLE)
            }
             */
        }
        return null
    }

    // // Number of new notifications for API <24 (M and below) devices.
    /**
     * Build the summary notification.
     */
    fun buildSummaryListNotification(
        sessionId: String,
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
            .setContentTitle(sessionId)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setSmallIcon(smallIcon)
            // set content text to support devices running API level < 24
            .setContentText(compatSummary)
            .setGroup(sessionId)
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
            .setContentIntent(buildOpenHomePendingIntentForSummary(sessionId))
            .setDeleteIntent(getDismissSummaryPendingIntent(sessionId))
            .build()
    }

    private fun getDismissSummaryPendingIntent(sessionId: String): PendingIntent {
        val intent = Intent(context, NotificationBroadcastReceiver::class.java)
        intent.action = actionIds.dismissSummary
        intent.data = createIgnoredUri("deleteSummary?$sessionId")
        intent.putExtra(NotificationBroadcastReceiver.KEY_SESSION_ID, sessionId)
        return PendingIntent.getBroadcast(
            context.applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntentCompat.FLAG_IMMUTABLE
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
        val testActionIntent = Intent(context, TestNotificationReceiver::class.java)
        testActionIntent.action = actionIds.diagnostic
        val testPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            testActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntentCompat.FLAG_IMMUTABLE
        )

        notificationManager.notify(
            "DIAGNOSTIC",
            888,
            NotificationCompat.Builder(context, NOISY_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(buildMeta.applicationName)
                .setContentText(stringProvider.getString(R.string.settings_troubleshoot_test_push_notification_content))
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
