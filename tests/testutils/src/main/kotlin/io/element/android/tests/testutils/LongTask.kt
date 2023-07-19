/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
