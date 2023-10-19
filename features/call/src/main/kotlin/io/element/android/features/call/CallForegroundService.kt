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

package io.element.android.features.call

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.graphics.drawable.IconCompat
import io.element.android.features.call.ui.ElementCallActivity
import io.element.android.libraries.designsystem.utils.CommonDrawables

class CallForegroundService : Service() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, CallForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, CallForegroundService::class.java)
            context.stopService(intent)
        }
    }

    private lateinit var notificationManagerCompat: NotificationManagerCompat

    override fun onCreate() {
        super.onCreate()

        notificationManagerCompat = NotificationManagerCompat.from(this)

        val foregroundServiceChannel = NotificationChannelCompat.Builder(
            "call_foreground_service_channel",
            NotificationManagerCompat.IMPORTANCE_LOW,
        ).setName(
            getString(R.string.call_foreground_service_channel_title_android).ifEmpty { "Ongoing call" }
        ).build()
        notificationManagerCompat.createNotificationChannel(foregroundServiceChannel)

        val callActivityIntent = Intent(this, ElementCallActivity::class.java)
        val pendingIntent = PendingIntentCompat.getActivity(this, 0, callActivityIntent, 0, false)
        val notification = NotificationCompat.Builder(this, foregroundServiceChannel.id)
            .setSmallIcon(IconCompat.createWithResource(this, CommonDrawables.ic_notification_small))
            .setContentTitle(getString(R.string.call_foreground_service_title_android))
            .setContentText(getString(R.string.call_foreground_service_message_android))
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
    }

    @Suppress("DEPRECATION")
    override fun onDestroy() {
        super.onDestroy()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
