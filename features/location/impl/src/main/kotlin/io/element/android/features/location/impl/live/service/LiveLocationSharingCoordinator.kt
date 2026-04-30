/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.live.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.features.location.api.Location
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.services.toolbox.api.systemclock.SystemClock
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.seconds

private val THROTTLE_WINDOW = 3.seconds

@OptIn(ExperimentalAtomicApi::class)
@SingleIn(AppScope::class)
class LiveLocationSharingCoordinator internal constructor(
    private val startService: () -> Unit,
    private val stopService: () -> Unit,
    private val nowMillis: () -> Long,
) {

    @Inject
    constructor(@ApplicationContext context: Context, clock: SystemClock) : this(
        startService = {
            ContextCompat.startForegroundService(context, Intent(context, LiveLocationSharingService::class.java))
        },
        stopService = {
            context.stopService(Intent(context, LiveLocationSharingService::class.java))
        },
        nowMillis = clock::epochMillis
    )

    private val receivers = ConcurrentHashMap<SessionId, LiveLocationReceiver>()
    private val lastDispatchMillis = AtomicLong(0L)

    fun register(sessionId: SessionId, receiver: LiveLocationReceiver) {
        val wasEmpty = receivers.isEmpty()
        Timber.d("LiveLocationSharingCoordinator registering receiver for session $sessionId (wasEmpty=$wasEmpty)")
        receivers[sessionId] = receiver
        if (wasEmpty) {
            Timber.d("LiveLocationSharingCoordinator starting service")
            runCatching(startService).onFailure {
                Timber.e(it, "Failed to start live location sharing service")
            }
        }
    }

    fun unregister(sessionId: SessionId) {
        Timber.d("LiveLocationSharingCoordinator unregistering receiver for session $sessionId")
        receivers.remove(sessionId)
        if (receivers.isEmpty()) {
            Timber.d("LiveLocationSharingCoordinator stopping service (no more receivers)")
            runCatching(stopService).onFailure {
                Timber.e(it, "Failed to stop live location sharing service")
            }
        }
    }

    suspend fun dispatch(location: Location) {
        val currentTimeMillis = nowMillis()
        val millisSincePrevious = currentTimeMillis - lastDispatchMillis.load()
        if (millisSincePrevious < THROTTLE_WINDOW.inWholeMilliseconds) {
            Timber.d("Received location before $THROTTLE_WINDOW, ignore.")
            return
        }
        lastDispatchMillis.store(currentTimeMillis)
        receivers.forEach { (sessionId, receiver) ->
            Timber.d("Dispatch received location for session $sessionId ")
            runCatching {
                receiver.onLocationUpdate(location)
            }.onFailure {
                Timber.e(it, "Failed to dispatch live location update for session $sessionId")
            }
        }
    }
}
