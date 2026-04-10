/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.push.api.push.PushHandlingWakeLock
import io.element.android.libraries.push.impl.di.PushBindings
import io.element.android.libraries.push.impl.notifications.channels.NotificationChannels
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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

    private var isOnForeground = false

    override fun onCreate() {
        Timber.d("Creating FetchPushForegroundService")

        bindings<PushBindings>().inject(this)

        Timber.d("Starting FetchPushForegroundService with wakelock timeout of $wakelockTimeout ms")
        // Start the foreground service as soon as possible
        val notificationCompat = NotificationCompat.Builder(this, notificationChannels.getSilentChannelId())
            .setSmallIcon(CommonDrawables.ic_notification)
            .setContentTitle(getString(CommonStrings.common_android_fetching_notifications_title))
            .setProgress(0, 0, true)
            .setVibrate(longArrayOf(0))
            .setSound(null)
            .build()

        // Try to start the service in foreground. This can fail, even in cases where it's supposed to work according to the docs.
        // In those cases we catch the exception and handle the failure so we don't try to start the wakelock or stop the service
        // from running in foreground later.
        runCatchingExceptions {
            startForeground(NOTIFICATION_ID, notificationCompat)
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
            coroutineScope.launch {
                delay(wakelockTimeout)
                onTimeoutAction(calledByTheSystem = false)
            }
        }

        return START_NOT_STICKY
    }

    override fun stopService(intent: Intent?): Boolean {
        if (isOnForeground) {
            wakelock.release()
            stopForeground(STOP_FOREGROUND_REMOVE)
        }

        return super.stopService(intent)
    }

    override fun onTimeout(startId: Int) {
        super.onTimeout(startId)
        onTimeoutAction(calledByTheSystem = true)
    }

    private fun onTimeoutAction(calledByTheSystem: Boolean) {
        Timber.d("onTimeoutAction, calledByTheSystem: $calledByTheSystem, isOnForeground: $isOnForeground")
        if (isOnForeground) {
            Timber.d("Wakelock timeout reached, stopping FetchPushForegroundService")
            coroutineScope.launch { pushHandlingWakeLock.unlock() }
        }
    }

    companion object {
        private val stopMutex = Mutex()

        fun startIfNeeded(context: Context) {
            // Don't start the foreground service if the device is already awake
            val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager
            if (powerManager.isInteractive) return

            start(context)
        }

        fun start(context: Context) {
            val intent = Intent(context, FetchPushForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                runCatchingExceptions { context.startForegroundService(intent) }
                    .onFailure { throwable ->
                        Timber.e(
                            throwable,
                            "Failed to start FetchPushForegroundService, notifications may take longer than usual to sync"
                        )
                    }
            } else {
                context.startService(intent)
            }
        }

        suspend fun stop(context: Context) = stopMutex.withLock {
            val runningServiceInfo = getRunningServiceInfo(context)
            if (runningServiceInfo != null) {
                val intent = Intent(context, FetchPushForegroundService::class.java)
                // If it's still not running in foreground, it means the service is still starting,
                // so we delay the stop to give it time to start and be set as foreground, otherwise we can crash
                // with `ForegroundServiceDidNotStartInTimeException`.
                var isInForeground = runningServiceInfo.foreground
                withTimeoutOrNull(5.seconds) {
                    while (!isInForeground) {
                        delay(50)
                        val updatedServiceInfo = getRunningServiceInfo(context)
                        if (updatedServiceInfo == null) {
                            Timber.d("FetchPushForegroundService is no longer running, no need to stop it.")
                            return@withTimeoutOrNull
                        }
                        isInForeground = updatedServiceInfo.foreground == true
                    }
                } ?: Timber.w("FetchPushForegroundService did not start in foreground after 5s, stopping it anyway.")
                context.stopService(intent)
            }
        }

        @Suppress("DEPRECATION")
        private fun getRunningServiceInfo(context: Context): ActivityManager.RunningServiceInfo? {
            val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            return activityManager.getRunningServices(Int.MAX_VALUE)
                .firstOrNull { it.service.className == FetchPushForegroundService::class.java.name }
        }
    }
}
