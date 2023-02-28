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

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import io.element.android.libraries.di.ApplicationContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.absoluteValue

class DateFormatters @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locale: Locale,
    private val clock: Clock,
    private val timeZone: TimeZone,
) {

    private val hourFormatter by lazy {
        if (DateFormat.is24HourFormat(context)) {
            DateTimeFormatter.ofPattern("HH:mm", locale)
        } else {
            DateTimeFormatter.ofPattern("h:mm a", locale)
        }
    }

    private val fullDateFormatter by lazy {
        val pattern = if (DateFormat.is24HourFormat(context)) {
            DateFormat.getBestDateTimePattern(locale, "EEE, d MMM yyyy HH:mm")
        } else {
            DateFormat.getBestDateTimePattern(locale, "EEE, d MMM yyyy h:mm a")
        }
        DateTimeFormatter.ofPattern(pattern, locale)
    }

    private val dateWithMonthFormatter: DateTimeFormatter by lazy {
        val pattern = DateFormat.getBestDateTimePattern(locale, "d MMM")
        DateTimeFormatter.ofPattern(pattern)
    }

    private val dateWithYearFormatter: DateTimeFormatter by lazy {
        val pattern = DateFormat.getBestDateTimePattern(locale, "d MMM y")
        DateTimeFormatter.ofPattern(pattern)
    }

    internal fun formatFullDate(localDateTime: LocalDateTime): String {
        return fullDateFormatter.format(localDateTime.toJavaLocalDateTime())
    }

    internal fun formatHour(localDateTime: LocalDateTime): String {
        return hourFormatter.format(localDateTime.toJavaLocalDateTime())
    }

    internal fun formatDateWithMonth(localDateTime: LocalDateTime): String {
        return dateWithMonthFormatter.format(localDateTime.toJavaLocalDateTime())
    }

    internal fun formatDateWithYear(localDateTime: LocalDateTime): String {
        return dateWithYearFormatter.format(localDateTime.toJavaLocalDateTime())
    }

    internal fun formatDate(
        dateToFormat: LocalDateTime,
        currentDate: LocalDateTime,
        useRelative: Boolean
    ): String {
        val period = Period.between(dateToFormat.date.toJavaLocalDate(), currentDate.date.toJavaLocalDate())
        return if (period.years.absoluteValue >= 1) {
            formatDateWithYear(dateToFormat)
        } else if (useRelative && period.days.absoluteValue < 2 && period.months.absoluteValue < 1) {
            getRelativeDay(dateToFormat.toInstant(timeZone).toEpochMilliseconds())
        } else {
            formatDateWithMonth(dateToFormat)
        }
    }

    private fun getRelativeDay(ts: Long): String {
        return DateUtils.getRelativeTimeSpanString(
            ts,
            clock.now().toEpochMilliseconds(),
            DateUtils.DAY_IN_MILLIS,
            DateUtils.FORMAT_SHOW_WEEKDAY
        ).toString()
    }
}
