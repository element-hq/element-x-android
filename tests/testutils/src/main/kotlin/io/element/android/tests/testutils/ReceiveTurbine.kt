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
import io.element.android.libraries.core.data.tryOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Consume all items until timeout is reached waiting for an event or we receive terminal event.
 * The timeout is applied for each event.
 * @return the list of consumed items.
 */
suspend fun <T : Any> ReceiveTurbine<T>.consumeItemsUntilTimeout(timeout: Duration = 100.milliseconds): List<T> {
    return consumeItemsUntilPredicate(timeout) { false }
}

/**
 * Consume items until predicate is true, or timeout is reached waiting for an event, or we receive terminal event.
 * The timeout is applied for each event.
 * @return the list of consumed items.
 */
suspend fun <T : Any> ReceiveTurbine<T>.consumeItemsUntilPredicate(
    timeout: Duration = 100.milliseconds,
    predicate: (T) -> Boolean,
): List<T> {
    val items = ArrayList<T>()
    tryOrNull {
        while (true) {
            when (val event = withTurbineTimeout(timeout) { awaitEvent() }) {
                is Event.Item<T> -> {
                    items.add(event.value)
                    if (predicate(event.value)) {
                        break
                    }
                }
                else -> break
            }
        }
    }
    return items
}
