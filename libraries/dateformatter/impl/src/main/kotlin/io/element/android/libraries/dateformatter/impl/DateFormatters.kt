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

import android.text.format.DateFormat
import android.text.format.DateUtils
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import javax.inject.Inject
import kotlin.math.absoluteValue

class DateFormatters @Inject constructor(
    private val locale: Locale,
    private val clock: Clock,
    private val timeZone: TimeZone,
) {
    private val onlyTimeFormatter: DateTimeFormatter by lazy {
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
    }

    private val dateWithMonthFormatter: DateTimeFormatter by lazy {
        val pattern = DateFormat.getBestDateTimePattern(locale, "d MMM") ?: "d MMM"
        DateTimeFormatter.ofPattern(pattern, locale)
    }

    private val dateWithYearFormatter: DateTimeFormatter by lazy {
        val pattern = DateFormat.getBestDateTimePattern(locale, "dd.MM.yyyy") ?: "dd.MM.yyyy"
        DateTimeFormatter.ofPattern(pattern, locale)
    }

    private val dateWithFullFormatFormatter: DateTimeFormatter by lazy {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)
    }

    internal fun formatTime(localDateTime: LocalDateTime): String {
        return onlyTimeFormatter.format(localDateTime.toJavaLocalDateTime())
    }

    internal fun formatDateWithMonth(localDateTime: LocalDateTime): String {
        return dateWithMonthFormatter.format(localDateTime.toJavaLocalDateTime())
    }

    internal fun formatDateWithYear(localDateTime: LocalDateTime): String {
        return dateWithYearFormatter.format(localDateTime.toJavaLocalDateTime())
    }

    internal fun formatDateWithFullFormat(localDateTime: LocalDateTime): String {
        return dateWithFullFormatFormatter.format(localDateTime.toJavaLocalDateTime())
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
        )?.toString() ?: ""
    }
}
