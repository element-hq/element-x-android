/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import android.content.Context
import android.os.PowerManager
import androidx.core.content.getSystemService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.push.api.push.PushHandlingWakeLock
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultPushHandlingWakeLock(
    @ApplicationContext private val context: Context,
) : PushHandlingWakeLock {
    private val wakeLocks = ConcurrentHashMap<String, PowerManager.WakeLock>()

    override fun lock(key: String, time: Duration) {
        Timber.d("Acquiring wakelock for push handling, key: $key.")
        // Get or create a wakelock for this instance to ensure the device stays awake while we handle the push and schedule and run the work
        val wakeLock = wakeLocks.getOrPut(key) {
            val powerManager = context.getSystemService<PowerManager>()!!
            powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag(key)).apply {
                setReferenceCounted(false)
            }
        }

        if (wakeLock.isHeld) {
            // If the wakelock is already held, we need to release it before acquiring it again with a new timeout
            wakeLock.release()
        }

        wakeLock.acquire(time.inWholeMilliseconds)
    }

    override fun unlock(key: String) {
        Timber.d("Releasing wakelock used for push handling, key: $key.")
        wakeLocks[key]?.release()
    }

    private fun tag(key: String) = "push:wakelock:$key"
}
