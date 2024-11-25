/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class LocalDateTimeProvider @Inject constructor(
    private val clock: Clock,
    private val timezoneProvider: TimezoneProvider,
) {
    fun providesNow(): LocalDateTime {
        val now: Instant = clock.now()
        return now.toLocalDateTime(timezoneProvider.provide())
    }

    fun providesFromTimestamp(timestamp: Long): LocalDateTime {
        val tsInstant = Instant.fromEpochMilliseconds(timestamp)
        return tsInstant.toLocalDateTime(timezoneProvider.provide())
    }
}
