/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.push.api.push.PushHandlingWakeLock
import io.element.android.libraries.push.impl.di.PushBindings
import io.element.android.libraries.push.impl.notifications.channels.NotificationChannels
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

private const val NOTIFICATION_ID = 1001

// This kind of foreground service can only last up to 3 minutes before onTimeout is called
private val wakelockTimeout = 3.minutes.inWholeMilliseconds

/**
 * Foreground service used to ensure the device stays awake while we handle the pushes and schedule and run the work to fetch the notification content.
 */
class FetchPushForegroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @Inject lateinit var notificationChannels: NotificationChannels
    @Inject lateinit var pushHandlingWakeLock: PushHandlingWakeLock
    @Inject @AppCoroutineScope lateinit var coroutineScope: CoroutineScope

    private val wakelock: PowerManager.WakeLock by lazy {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FetchPushService:WakeLock").apply {
            setReferenceCounted(false)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        bindings<PushBindings>().inject(this)

        wakelock.acquire(wakelockTimeout)

        val notificationCompat = NotificationCompat.Builder(this, notificationChannels.getSilentChannelId())
            .setSmallIcon(CommonDrawables.ic_notification)
            .setContentTitle(getString(CommonStrings.common_android_fetching_notifications_title))
            .setProgress(0, 0, true)
            .setVibrate(longArrayOf(0))
            .setSound(null)
            .build()
        startForeground(NOTIFICATION_ID, notificationCompat)

        // The timeout is not automatic before Android 15, so we need to schedule it ourselves
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            coroutineScope.launch {
                delay(wakelockTimeout)
                onTimeout(startId)
            }
        }

        return START_NOT_STICKY
    }

    override fun stopService(intent: Intent?): Boolean {
        wakelock.release()

        stopForeground(STOP_FOREGROUND_REMOVE)
        return super.stopService(intent)
    }

    override fun onTimeout(startId: Int) {
        super.onTimeout(startId)

        pushHandlingWakeLock.unlock()
    }

    companion object {
        fun startIfNeeded(context: Context) {
            // Don't start the foreground service if the device is already awake
            val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager
            if (powerManager.isInteractive) return

            start(context)
        }

        fun start(context: Context) {
            val intent = Intent(context, FetchPushForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FetchPushForegroundService::class.java)
            context.stopService(intent)
        }
    }
}
