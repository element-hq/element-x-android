/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
package io.element.android.features.call.impl.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.app.Person
import dev.zacsweers.metro.Inject
import io.element.android.appconfig.ElementCallConfig
import io.element.android.features.call.api.CallType
import io.element.android.features.call.impl.receivers.DeclineCallBroadcastReceiver
import io.element.android.features.call.impl.ui.IncomingCallActivity
import io.element.android.features.call.impl.utils.IntentProvider
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.ui.media.ImageLoaderHolder
import io.element.android.libraries.push.api.notifications.NotificationBitmapLoader
import kotlin.time.Duration.Companion.seconds

/**
 * Creates a notification for a ringing call.
 */
@Inject
class RingingCallNotificationCreator(
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
        expirationTimestamp: Long,
        textContent: String?,
    ): Notification? {
        val matrixClient = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: return null
        val imageLoader = imageLoaderHolder.get(matrixClient)
        val userIcon = notificationBitmapLoader.getUserIcon(
            avatarData = AvatarData(
                id = roomId.value,
                name = roomName,
                url = roomAvatarUrl,
                size = AvatarSize.RoomDetailsHeader,
            ),
            imageLoader = imageLoader,
        )

        val caller = Person.Builder()
            .setName(senderDisplayName)
            .setIcon(userIcon)
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
            timestamp = timestamp,
            textContent = textContent,
            expirationTimestamp = expirationTimestamp,
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

        return NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(CommonDrawables.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setStyle(NotificationCompat.CallStyle.forIncomingCall(caller, declineIntent, answerIntent).setIsVideo(true))
            .addPerson(caller)
            .setAutoCancel(true)
            .setWhen(timestamp)
            .setOngoing(true)
            .setShowWhen(false)
            // If textContent is null, the content text is set by the style (will be "Incoming call")
            .setContentText(textContent)
            .setSound(Settings.System.DEFAULT_RINGTONE_URI, AudioManager.STREAM_RING)
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
