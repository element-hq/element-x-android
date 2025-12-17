/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

@Inject
class LocalDateTimeProvider(
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
