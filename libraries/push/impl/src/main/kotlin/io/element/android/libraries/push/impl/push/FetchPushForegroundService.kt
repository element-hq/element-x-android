/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.minutes

private const val NOTIFICATION_ID = 1001

// This kind of foreground service can only last up to 3 minutes before onTimeout is called
private val wakelockTimeout = 3.minutes.inWholeMilliseconds

// The channel ID to use for the notification of the foreground service.
private const val CHANNEL_ID = "fetch_push_notification_channel"

// The tag to use for the wakelock, this is used for debugging purposes and should be unique to this service.
private const val WAKELOCK_TAG = "FetchPushService:WakeLock"

/**
 * Foreground service used to ensure the device stays awake while we handle the pushes and schedule and run the work to fetch the notification content.
 */
class FetchPushForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val wakelock: PowerManager.WakeLock by lazy {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG).apply {
            setReferenceCounted(false)
        }
    }

    private var isOnForeground = false

    private fun ensureNotificationChannelExists() {
        NotificationManagerCompat.from(this).createNotificationChannelsCompat(
            listOf(
                NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                    .setName(getString(CommonStrings.common_fetching_notifications_title_android).ifEmpty { "Syncing notifications…" })
                    .setVibrationEnabled(false)
                    .setSound(null, null)
                    .build()
            )
        )
    }

    override fun onCreate() {
        Timber.i("Creating FetchPushForegroundService to handle incoming push, acquiring wakelock for up to $wakelockTimeout ms")
        ensureNotificationChannelExists()

        // Start the foreground service as soon as possible
        val notificationCompat = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(CommonDrawables.ic_notification)
            .setContentTitle(getString(CommonStrings.common_fetching_notifications_title_android).ifEmpty { "Syncing notifications…" })
            .setProgress(0, 0, true)
            .setVibrate(longArrayOf(0))
            .setSound(null)
            .build()

        // Try to start the service in foreground. This can fail, even in cases where it's supposed to work according to the docs.
        // In those cases we catch the exception and handle the failure so we don't try to start the wakelock or stop the service
        // from running in foreground later.
        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
        } else {
            0
        }
        runCatchingExceptions {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notificationCompat, serviceType)
        }
            .onSuccess {
                isOnForeground = true
                Timber.d("FetchPushForegroundService started in foreground successfully")
            }
            .onFailure {
                isOnForeground = false
                Timber.e(it, "Failed to start FetchPushForegroundService in foreground")
            }

        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isOnForeground) {
            Timber.w("FetchPushForegroundService is not running in foreground, stopping it to avoid crash")
            stopSelf()
            return START_NOT_STICKY
        }

        wakelock.acquire(wakelockTimeout)

        // The timeout is not automatic before Android 15, so we need to schedule it ourselves
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            MainScope().launch {
                delay(wakelockTimeout)
                onTimeoutAction(calledByTheSystem = false)
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        if (isOnForeground) {
            Timber.i("Destroying FetchPushForegroundService, releasing wakelock and stopping foreground")
            if (wakelock.isHeld) {
                wakelock.release()
            }
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        } else {
            Timber.w("Destroying FetchPushForegroundService that was not running in foreground, this is unexpected")
        }
    }

    override fun onTimeout(startId: Int) {
        super.onTimeout(startId)
        onTimeoutAction(calledByTheSystem = true)
    }

    private fun onTimeoutAction(calledByTheSystem: Boolean) {
        Timber.w("onTimeoutAction, calledByTheSystem: $calledByTheSystem, isOnForeground: $isOnForeground")
        if (isOnForeground) {
            Timber.w("Wakelock timeout reached, stopping FetchPushForegroundService")
            stopSelf()
        }
    }
}
