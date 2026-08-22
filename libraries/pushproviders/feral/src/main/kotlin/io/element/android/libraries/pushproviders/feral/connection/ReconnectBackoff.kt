/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral.connection

import io.element.android.appconfig.FeralPushConfig
import kotlin.random.Random
import kotlin.time.Duration

/**
 * Exponential backoff with jitter: [initial], 2x, 4x… capped at [max]; each delay is scaled by a
 * random factor in `1 ± jitterRatio`. [reset] after a healthy connection.
 */
class ReconnectBackoff(
    private val initial: Duration = FeralPushConfig.BACKOFF_MIN,
    private val max: Duration = FeralPushConfig.BACKOFF_MAX,
    private val jitterRatio: Double = 0.2,
    private val random: Random = Random.Default,
) {
    private var current: Duration = initial

    fun nextDelay(): Duration {
        val jitter = 1.0 + (random.nextDouble() * 2 - 1) * jitterRatio
        val delay = current * jitter
        current = (current * 2).coerceAtMost(max)
        return delay.coerceIn(Duration.ZERO, max)
    }

    fun reset() {
        current = initial
    }
}
