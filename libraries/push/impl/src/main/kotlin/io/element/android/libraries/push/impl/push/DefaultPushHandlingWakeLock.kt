/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.push.api.push.PushHandlingWakeLock
import timber.log.Timber
import kotlin.time.Duration

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultPushHandlingWakeLock(
    @ApplicationContext private val context: Context,
) : PushHandlingWakeLock {
    override fun lock(time: Duration) {
        Timber.d("Acquiring wakelock for push handling, starting service.")
        FetchPushForegroundService.startIfNeeded(context)
    }

    override suspend fun unlock() {
        Timber.d("Releasing wakelock used for push handling.")
        FetchPushForegroundService.stop(context)
    }
}
