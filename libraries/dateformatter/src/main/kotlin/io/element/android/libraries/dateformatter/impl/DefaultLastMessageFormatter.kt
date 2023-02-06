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
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.dateformatter.LastMessageFormatter
import io.element.android.libraries.di.AppScope
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.absoluteValue

@ContributesBinding(AppScope::class)
class DefaultLastMessageFormatter @Inject constructor(
    private val clock: Clock,
    private val locale: Locale,
) : LastMessageFormatter {
    private val onlyTimeFormatter: DateTimeFormatter by lazy {
        val pattern = DateFormat.getBestDateTimePattern(locale, "HH:mm") ?: "HH:mm"
        DateTimeFormatter.ofPattern(pattern)
    }

    private val dateWithMonthFormatter: DateTimeFormatter by lazy {
        val pattern = DateFormat.getBestDateTimePattern(locale, "d MMM") ?: "d MMM"
        DateTimeFormatter.ofPattern(pattern)
    }

    private val dateWithYearFormatter: DateTimeFormatter by lazy {
        val pattern = DateFormat.getBestDateTimePattern(locale, "dd.MM.yyyy") ?: "dd.MM.yyyy"
        DateTimeFormatter.ofPattern(pattern)
    }

    override fun format(timestamp: Long?): String {
        if (timestamp == null) return ""
        val now: Instant = clock.now()
        val tsInstant = Instant.fromEpochMilliseconds(timestamp)
        val nowDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val tsDateTime = tsInstant.toLocalDateTime(TimeZone.currentSystemDefault())
        val isSameDay = nowDateTime.date == tsDateTime.date
        return when {
            isSameDay -> {
                onlyTimeFormatter.format(tsDateTime.toJavaLocalDateTime())
            }
            else -> {
                formatDate(tsDateTime, nowDateTime)
            }
        }
    }

    private fun formatDate(
        date: LocalDateTime,
        currentDate: LocalDateTime,
    ): String {
        val period = Period.between(date.date.toJavaLocalDate(), currentDate.date.toJavaLocalDate())
        return if (period.years.absoluteValue >= 1) {
            formatDateWithYear(date)
        } else if (period.days.absoluteValue < 2 && period.months.absoluteValue < 1) {
            getRelativeDay(date.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds())
        } else {
            formatDateWithMonth(date)
        }
    }

    private fun formatDateWithMonth(localDateTime: LocalDateTime): String {
        return dateWithMonthFormatter.format(localDateTime.toJavaLocalDateTime())
    }

    private fun formatDateWithYear(localDateTime: LocalDateTime): String {
        return dateWithYearFormatter.format(localDateTime.toJavaLocalDateTime())
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
