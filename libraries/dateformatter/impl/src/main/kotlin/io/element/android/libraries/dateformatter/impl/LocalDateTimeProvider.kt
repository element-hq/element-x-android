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

package io.element.android.libraries.dateformatter.impl

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class LocalDateTimeProvider @Inject constructor(
    private val clock: Clock,
    private val timezone: TimeZone,
) {
    fun providesNow(): LocalDateTime {
        val now: Instant = clock.now()
        return now.toLocalDateTime(timezone)
    }

    fun providesFromTimestamp(timestamp: Long): LocalDateTime {
        val tsInstant = Instant.fromEpochMilliseconds(timestamp)
        return tsInstant.toLocalDateTime(timezone)
    }
}
