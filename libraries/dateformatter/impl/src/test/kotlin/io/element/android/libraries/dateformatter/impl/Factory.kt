/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import io.element.android.tests.testutils.InstrumentationStringProvider
import kotlinx.datetime.TimeZone
import java.util.Locale
import kotlin.time.Instant

/**
 * Create DefaultDateFormatter and set current time to the provided date.
 */
fun createFormatter(currentDate: String): DefaultDateFormatter {
    val clock = FakeClock().apply { givenInstant(Instant.parse(currentDate)) }
    val localDateTimeProvider = LocalDateTimeProvider(clock) { TimeZone.UTC }
    val dateFormatters = DateFormatters(
        localeChangeObserver = {},
        clock = clock,
        timeZoneProvider = { TimeZone.UTC },
        locale = Locale.getDefault(),
    )
    val stringProvider = InstrumentationStringProvider()
    val dateFormatterDay = DefaultDateFormatterDay(
        localDateTimeProvider = localDateTimeProvider,
        dateFormatters = dateFormatters,
    )
    return DefaultDateFormatter(
        dateFormatterFull = DateFormatterFull(
            stringProvider = stringProvider,
            localDateTimeProvider = localDateTimeProvider,
            dateFormatters = dateFormatters,
            dateFormatterDay = dateFormatterDay,
        ),
        dateFormatterMonth = DateFormatterMonth(
            stringProvider = stringProvider,
            localDateTimeProvider = localDateTimeProvider,
            dateFormatters = dateFormatters,
        ),
        dateFormatterDay = dateFormatterDay,
        dateFormatterTime = DateFormatterTime(
            localDateTimeProvider = localDateTimeProvider,
            dateFormatters = dateFormatters,
        ),
        dateFormatterTimeOnly = DateFormatterTimeOnly(
            localDateTimeProvider = localDateTimeProvider,
            dateFormatters = dateFormatters,
        ),
    )
}
