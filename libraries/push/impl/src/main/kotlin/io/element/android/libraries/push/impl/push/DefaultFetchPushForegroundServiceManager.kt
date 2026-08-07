/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.push.api.push.FetchPushForegroundServiceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultFetchPushForegroundServiceManager(
    @ApplicationContext private val context: Context,
) : FetchPushForegroundServiceManager {
    private val stopMutex = Mutex()

    override fun start(): Boolean {
        Timber.d("Acquiring wakelock for push handling, starting service.")

        // Don't start the foreground service if the device is already awake
        val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager
        if (powerManager.isInteractive) {
            Timber.d("Device is already in an interactive state, no need to start FetchPushForegroundService")
            return false
        }

        val intent = Intent(context, FetchPushForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runCatchingExceptions { ContextCompat.startForegroundService(context, intent) }
                .onFailure { throwable ->
                    Timber.e(throwable, "Failed to start FetchPushForegroundService, notifications may take longer than usual to sync")
                }
        } else {
            context.startService(intent)
        }

        return true
    }

    override suspend fun stop(): Boolean {
        Timber.d("Releasing wakelock used for push handling, stopping service.")
        return stopMutex.withLock {
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
            } else {
                false
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getRunningServiceInfo(context: Context): ActivityManager.RunningServiceInfo? {
        val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return activityManager.getRunningServices(Int.MAX_VALUE)
            .firstOrNull { it.service.className == FetchPushForegroundService::class.java.name }
    }
}
