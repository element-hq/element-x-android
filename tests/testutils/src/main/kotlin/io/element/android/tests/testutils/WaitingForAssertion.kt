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

import kotlinx.coroutines.delay

suspend fun waitForPredicate(
    delayBetweenAttemptsMillis: Long = 1,
    maxNumberOfAttempts: Int = 20,
    predicate: () -> Boolean,
) {
    for (i in 0..maxNumberOfAttempts) {
        if (predicate()) return
        if (i < maxNumberOfAttempts) delay(delayBetweenAttemptsMillis)
    }
    throw AssertionError("Predicate was not true after $maxNumberOfAttempts attempts")
}
