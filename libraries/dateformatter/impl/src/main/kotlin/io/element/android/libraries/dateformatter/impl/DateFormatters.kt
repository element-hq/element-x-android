/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import android.text.format.DateFormat
import android.text.format.DateUtils
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
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
    private val timeZoneProvider: TimezoneProvider,
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
            getRelativeDay(dateToFormat.toInstant(timeZoneProvider.provide()).toEpochMilliseconds())
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
