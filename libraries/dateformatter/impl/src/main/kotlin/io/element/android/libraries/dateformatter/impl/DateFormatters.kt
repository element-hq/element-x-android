/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import android.text.format.DateUtils
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import timber.log.Timber
import java.time.Period
import java.util.Locale
import javax.inject.Inject
import kotlin.math.absoluteValue

@SingleIn(AppScope::class)
class DateFormatters @Inject constructor(
    localeChangeObserver: LocaleChangeObserver,
    private val clock: Clock,
    private val timeZoneProvider: TimezoneProvider,
    locale: Locale,
) : LocaleChangeListener {
    init {
        localeChangeObserver.addListener(this)
    }

    private var dateTimeFormatters: DateTimeFormatters = DateTimeFormatters(locale)

    override fun onLocaleChange() {
        Timber.w("Locale changed, updating formatters")
        dateTimeFormatters = DateTimeFormatters(Locale.getDefault())
    }

    internal fun formatTime(localDateTime: LocalDateTime): String {
        return dateTimeFormatters.onlyTimeFormatter.format(localDateTime.toJavaLocalDateTime())
    }

    internal fun formatDateWithMonthAndYear(localDateTime: LocalDateTime): String {
        return dateTimeFormatters.dateWithMonthAndYearFormatter.format(localDateTime.toJavaLocalDateTime())
    }

    internal fun formatDateWithMonth(localDateTime: LocalDateTime): String {
        return dateTimeFormatters.dateWithMonthFormatter.format(localDateTime.toJavaLocalDateTime())
    }

    internal fun formatDateWithDay(localDateTime: LocalDateTime): String {
        return dateTimeFormatters.dateWithDayFormatter.format(localDateTime.toJavaLocalDateTime())
    }

    internal fun formatDateWithYear(localDateTime: LocalDateTime): String {
        return dateTimeFormatters.dateWithYearFormatter.format(localDateTime.toJavaLocalDateTime())
    }

    internal fun formatDateWithFullFormat(localDateTime: LocalDateTime): String {
        return dateTimeFormatters.dateWithFullFormatFormatter.format(localDateTime.toJavaLocalDateTime())
    }

    internal fun formatDateWithFullFormatNoYear(localDateTime: LocalDateTime): String {
        return dateTimeFormatters.dateWithFullFormatNoYearFormatter.format(localDateTime.toJavaLocalDateTime())
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

    internal fun getRelativeDay(ts: Long, default: String = ""): String {
        return DateUtils.getRelativeTimeSpanString(
            ts,
            clock.now().toEpochMilliseconds(),
            DateUtils.DAY_IN_MILLIS,
            DateUtils.FORMAT_SHOW_WEEKDAY
        )?.toString() ?: default
    }
}
