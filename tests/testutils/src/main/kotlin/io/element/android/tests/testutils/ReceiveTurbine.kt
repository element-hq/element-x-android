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

import app.cash.turbine.Event
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.withTurbineTimeout
import io.element.android.libraries.core.bool.orFalse
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Consume all items until timeout is reached waiting for an event or we receive terminal event.
 * The timeout is applied for each event.
 * @return the list of consumed items.
 */
suspend fun <T : Any> ReceiveTurbine<T>.consumeItemsUntilTimeout(timeout: Duration = 100.milliseconds): List<T> {
    return consumeItemsUntilPredicate(timeout, ignoreTimeoutError = true) { false }
}

/**
 * Consume all items which are emitted sequentially.
 * Use the smallest timeout possible internally to avoid wasting time.
 * Same as calling skipItems(x) and then awaitItem() but without assumption on the number of items.
 * @return the last item emitted.
 */
suspend fun <T : Any> ReceiveTurbine<T>.awaitLastSequentialItem(): T {
    return consumeItemsUntilTimeout(1.milliseconds).last()
}

/**
 * Consume items until predicate is true, or timeout is reached waiting for an event, or we receive terminal event.
 * The timeout is applied for each event.
 * @return the list of consumed items.
 */
suspend fun <T : Any> ReceiveTurbine<T>.consumeItemsUntilPredicate(
    timeout: Duration = 3.seconds,
    ignoreTimeoutError: Boolean = false,
    predicate: (T) -> Boolean,
): List<T> {
    val items = ArrayList<T>()
    var exitLoop = false
    try {
        while (!exitLoop) {
            when (val event = withTurbineTimeout(timeout) { awaitEvent() }) {
                is Event.Item<T> -> {
                    items.add(event.value)
                    exitLoop = predicate(event.value)
                }
                Event.Complete -> error("Unexpected complete")
                is Event.Error -> throw event.throwable
            }
        }
    } catch (assertionError: AssertionError) {
        // TurbineAssertionError is internal :/, so rely on the message
        if (assertionError.message?.startsWith("No value produced in").orFalse() && ignoreTimeoutError) {
            // Timeout, ignore
        } else {
            throw assertionError
        }
    }
    return items
}
