/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl.lockscreen

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

/**
 * Posts a high-priority, full-screen-intent notification announcing a live PTT session, which
 * launches [PttLockScreenActivity] over the lock screen (heads-up when the device is unlocked).
 *
 * Interim: today it's fired by a local test trigger; ultimately it'll be posted from the "session
 * live" push signal delivered to room members (tasks #14-16).
 */
object PttLockScreenAlert {
    private const val CHANNEL_ID = "ptt_incoming_alert_channel"
    private const val NOTIFICATION_ID = 90_211

    fun post(context: Context, sessionId: SessionId, roomId: RoomId, roomName: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        val channel = NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
            .setName("Incoming push-to-talk")
            .build()
        notificationManager.createNotificationChannel(channel)

        val fullScreenIntent = PendingIntent.getActivity(
            context,
            0,
            PttLockScreenActivity.newIntent(context, sessionId, roomId, roomName),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(CommonDrawables.ic_notification)
            .setContentTitle("Push-to-talk")
            .setContentText(roomName.ifEmpty { "Session live — tap to join" })
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(fullScreenIntent)
            .setFullScreenIntent(fullScreenIntent, true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
