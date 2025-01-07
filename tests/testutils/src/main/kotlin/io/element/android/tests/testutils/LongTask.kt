/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.testutils

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Workaround for https://github.com/cashapp/molecule/issues/249.
 * This functions should be removed/deprecated right after we find a proper fix.
 */
suspend inline fun <T> simulateLongTask(lambda: () -> T): T {
    delay(1)
    return lambda()
}

/**
 * Can be used for testing events in Presenter, where the event does not emit new state.
 * If the (virtual) timeout is passed, we release the latch manually.
 */
suspend fun awaitWithLatch(timeout: Duration = 300.milliseconds, block: (CompletableDeferred<Unit>) -> Unit) {
    val latch = CompletableDeferred<Unit>()
    try {
        withTimeout(timeout) {
            latch.also(block).await()
        }
    } catch (exception: TimeoutCancellationException) {
        latch.complete(Unit)
    }
}
