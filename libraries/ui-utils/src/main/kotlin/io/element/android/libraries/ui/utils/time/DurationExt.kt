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

package io.element.android.libraries.ui.utils.time

import kotlin.time.Duration

/**
 * Format a duration as minutes:seconds.
 *
 * For example,
 * - 0 seconds will be formatted as "0:00".
 * - 65 seconds will be formatted as "1:05".
 * - 2 hours will be formatted as "120:00".
 * - negative 10 seconds will be formatted as "-0:10".
 *
 * @return the formatted duration.
 */
fun Duration.formatShort(): String {
    // Format as minutes:seconds
    val seconds = (absoluteValue.inWholeSeconds % 60)
        .toString()
        .padStart(2, '0')

    val sign = isNegative().let { if (it) "-" else "" }

    return "$sign${absoluteValue.inWholeMinutes}:$seconds"
}
