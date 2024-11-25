/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.call.impl.services

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import io.element.android.features.call.impl.R
import io.element.android.features.call.impl.ui.ElementCallActivity
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.push.api.notifications.ForegroundServiceType
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import timber.log.Timber

/**
 * A foreground service that shows a notification for an ongoing call while the UI is in background.
 */
class CallForegroundService : Service() {
    companion object {
        fun start(context: Context) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(context, CallForegroundService::class.java)
                ContextCompat.startForegroundService(context, intent)
            } else {
                Timber.w("Microphone permission is not granted, cannot start the call foreground service")
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
        val notificationId = NotificationIdProvider.getForegroundServiceNotificationId(ForegroundServiceType.ONGOING_CALL)
        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        } else {
            0
        }
        runCatching {
            ServiceCompat.startForeground(this, notificationId, notification, serviceType)
        }.onFailure {
            Timber.e(it, "Failed to start ongoing call foreground service")
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
