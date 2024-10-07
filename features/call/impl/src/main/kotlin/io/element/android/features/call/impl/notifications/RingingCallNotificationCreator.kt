/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */
package io.element.android.features.call.impl.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.app.Person
import io.element.android.appconfig.ElementCallConfig
import io.element.android.features.call.api.CallType
import io.element.android.features.call.impl.receivers.DeclineCallBroadcastReceiver
import io.element.android.features.call.impl.ui.IncomingCallActivity
import io.element.android.features.call.impl.utils.IntentProvider
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.ui.media.ImageLoaderHolder
import io.element.android.libraries.push.api.notifications.NotificationBitmapLoader
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

/**
 * Creates a notification for a ringing call.
 */
class RingingCallNotificationCreator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val matrixClientProvider: MatrixClientProvider,
    private val imageLoaderHolder: ImageLoaderHolder,
    private val notificationBitmapLoader: NotificationBitmapLoader,
) {
    companion object {
        /**
         * Request code for the decline action.
         */
        const val DECLINE_REQUEST_CODE = 1

        /**
         * Request code for the full screen intent.
         */
        const val FULL_SCREEN_INTENT_REQUEST_CODE = 2
    }

    suspend fun createNotification(
        sessionId: SessionId,
        roomId: RoomId,
        eventId: EventId,
        senderId: UserId,
        roomName: String?,
        senderDisplayName: String,
        roomAvatarUrl: String?,
        notificationChannelId: String,
        timestamp: Long,
    ): Notification? {
        val matrixClient = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return null
        val imageLoader = imageLoaderHolder.get(matrixClient)
        val largeIcon = notificationBitmapLoader.getUserIcon(roomAvatarUrl, imageLoader)

        val caller = Person.Builder()
            .setName(senderDisplayName)
            .setIcon(largeIcon)
            .setImportant(true)
            .build()

        val answerIntent = IntentProvider.getPendingIntent(context, CallType.RoomCall(sessionId, roomId))
        val notificationData = CallNotificationData(
            sessionId = sessionId,
            roomId = roomId,
            eventId = eventId,
            senderId = senderId,
            roomName = roomName,
            senderName = senderDisplayName,
            avatarUrl = roomAvatarUrl,
            notificationChannelId = notificationChannelId,
            timestamp = timestamp
        )

        val declineIntent = PendingIntentCompat.getBroadcast(
            context,
            DECLINE_REQUEST_CODE,
            Intent(context, DeclineCallBroadcastReceiver::class.java).apply {
                putExtra(DeclineCallBroadcastReceiver.EXTRA_NOTIFICATION_DATA, notificationData)
            },
            PendingIntent.FLAG_CANCEL_CURRENT,
            false,
        )!!

        val fullScreenIntent = PendingIntentCompat.getActivity(
            context,
            FULL_SCREEN_INTENT_REQUEST_CODE,
            Intent(context, IncomingCallActivity::class.java).apply {
                putExtra(IncomingCallActivity.EXTRA_NOTIFICATION_DATA, notificationData)
            },
            PendingIntent.FLAG_CANCEL_CURRENT,
            false
        )

        // TODO use a fallback ringtone if the default ringtone is not available
        val ringtoneUri = runCatching { RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE) }.getOrNull()
        return NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(CommonDrawables.ic_notification_small)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setStyle(NotificationCompat.CallStyle.forIncomingCall(caller, declineIntent, answerIntent).setIsVideo(true))
            .addPerson(caller)
            .setAutoCancel(true)
            .setWhen(timestamp)
            .setOngoing(true)
            .setShowWhen(false)
            .apply {
                if (ringtoneUri != null) {
                    setSound(ringtoneUri, AudioManager.STREAM_RING)
                }
            }
            .setTimeoutAfter(ElementCallConfig.RINGING_CALL_DURATION_SECONDS.seconds.inWholeMilliseconds)
            .setContentIntent(answerIntent)
            .setDeleteIntent(declineIntent)
            .setFullScreenIntent(fullScreenIntent, true)
            .build()
            .apply {
                flags = flags.or(Notification.FLAG_INSISTENT)
            }
    }
}
