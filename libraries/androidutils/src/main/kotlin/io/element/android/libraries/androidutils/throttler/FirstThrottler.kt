/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */
package io.element.android.libraries.androidutils.throttler

import android.os.SystemClock

/**
 * Simple ThrottleFirst
 * See https://raw.githubusercontent.com/wiki/ReactiveX/RxJava/images/rx-operators/throttleFirst.png
 */
class FirstThrottler(private val minimumInterval: Long = 800) {
    private var lastDate = 0L

    sealed interface CanHandleResult {
        data object Yes : CanHandleResult
        data class No(val shouldWaitMillis: Long) : CanHandleResult

        fun waitMillis(): Long {
            return when (this) {
                Yes -> 0
                is No -> shouldWaitMillis
            }
        }
    }

    fun canHandle(): CanHandleResult {
        val now = SystemClock.elapsedRealtime()
        val delaySinceLast = now - lastDate
        if (delaySinceLast > minimumInterval) {
            lastDate = now
            return CanHandleResult.Yes
        }

        // Too early
        return CanHandleResult.No(minimumInterval - delaySinceLast)
    }
}
