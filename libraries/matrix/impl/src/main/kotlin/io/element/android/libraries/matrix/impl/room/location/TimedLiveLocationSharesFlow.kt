/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.location

import io.element.android.libraries.matrix.api.room.location.LiveLocationShare
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

/**
 * Makes sure to filter and emit live location based on the endTimestamp.
 */
internal fun Flow<List<LiveLocationShare>>.timedByExpiry(
    currentTimeMillis: () -> Long = System::currentTimeMillis,
): Flow<List<LiveLocationShare>> = channelFlow {
    var timerJob: Job? = null

    fun List<LiveLocationShare>.nextExpiryAfter(timestamp: Long): Long? {
        return this
            .asSequence()
            .map { it.endTimestamp }
            .filter { it > timestamp }
            .minOrNull()
    }

    fun List<LiveLocationShare>.filterLive(): List<LiveLocationShare> {
        val currentTimeMillis = currentTimeMillis()
        return filter { it.endTimestamp > currentTimeMillis }
    }

    fun reschedule(shares: List<LiveLocationShare>) {
        timerJob?.cancel()
        timerJob = launch {
            val currentTimeMillis = currentTimeMillis()
            val nextExpiry = shares.nextExpiryAfter(currentTimeMillis) ?: return@launch
            delay((nextExpiry - currentTimeMillis).coerceAtLeast(0))
            val liveShares = shares.filterLive()
            send(liveShares)
            reschedule(liveShares)
        }
    }

    collect { shares ->
        val liveShares = shares.filterLive()
        send(liveShares)
        reschedule(liveShares)
    }
}
