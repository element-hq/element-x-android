/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral.connection

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class ReconnectBackoffTest {
    @Test
    fun `grows exponentially up to the max without jitter`() {
        val backoff = ReconnectBackoff(initial = 1.seconds, max = 60.seconds, jitterRatio = 0.0)
        val delays = List(8) { backoff.nextDelay() }
        assertThat(delays).containsExactly(
            1.seconds, 2.seconds, 4.seconds, 8.seconds, 16.seconds, 32.seconds, 60.seconds, 60.seconds,
        ).inOrder()
    }

    @Test
    fun `reset goes back to the initial delay`() {
        val backoff = ReconnectBackoff(initial = 1.seconds, max = 60.seconds, jitterRatio = 0.0)
        repeat(4) { backoff.nextDelay() }
        backoff.reset()
        assertThat(backoff.nextDelay()).isEqualTo(1.seconds)
        assertThat(backoff.nextDelay()).isEqualTo(2.seconds)
    }

    @Test
    fun `jitter stays within the ratio and the bounds`() {
        val backoff = ReconnectBackoff(initial = 1.seconds, max = 60.seconds, jitterRatio = 0.2, random = Random(seed = 7))
        val first = backoff.nextDelay()
        assertThat(first).isAtLeast(0.8.seconds)
        assertThat(first).isAtMost(1.2.seconds)
        repeat(10) { backoff.nextDelay() }
        val capped = backoff.nextDelay()
        assertThat(capped).isAtLeast(48.seconds)
        assertThat(capped).isAtMost(60.seconds)
    }
}
