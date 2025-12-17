/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
package io.element.android.libraries.androidutils.throttler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Simple ThrottleFirst
 * See https://raw.githubusercontent.com/wiki/ReactiveX/RxJava/images/rx-operators/throttleFirst.png
 */
class FirstThrottler(
    private val minimumInterval: Long = 800,
    private val coroutineScope: CoroutineScope,
) {
    private val canHandle = AtomicBoolean(true)

    fun canHandle(): Boolean {
        return canHandle.getAndSet(false).also { result ->
            if (result) {
                coroutineScope.launch {
                    delay(minimumInterval)
                    canHandle.set(true)
                }
            }
        }
    }
}
