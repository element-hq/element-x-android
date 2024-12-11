/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.dateformatter.api.DateFormatterMode
import io.element.android.libraries.dateformatter.test.FakeClock
import io.element.android.tests.testutils.InstrumentationStringProvider
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "fr")
class DefaultDateFormatterFrTest {
    @Test
    fun `test null`() {
        val now = "1980-04-06T18:35:24.00Z"
        val ts: Long? = null
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts)).isEmpty()
    }

    @Test
    fun `test epoch`() {
        val now = "1980-04-06T18:35:24.00Z"
        val ts = 0L
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full)).isEqualTo("1 janvier 1970 à 00:00")
        assertThat(formatter.format(ts, DateFormatterMode.Month)).isEqualTo("Janvier 1970")
        assertThat(formatter.format(ts, DateFormatterMode.Day)).isEqualTo("1 janvier 1970")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate)).isEqualTo("01.01.1970")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly)).isEqualTo("00:00")
    }

    @Test
    fun `test epoch relative`() {
        val now = "1980-04-06T18:35:24.00Z"
        val ts = 0L
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full, true)).isEqualTo("1 janvier 1970 à 00:00")
        assertThat(formatter.format(ts, DateFormatterMode.Month, true)).isEqualTo("Janvier 1970")
        assertThat(formatter.format(ts, DateFormatterMode.Day, true)).isEqualTo("1 janvier 1970")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate, true)).isEqualTo("01.01.1970")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly, true)).isEqualTo("00:00")
    }

    @Test
    fun `test now`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-06T18:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full)).isEqualTo("6 avril 1980 à 18:35")
        assertThat(formatter.format(ts, DateFormatterMode.Month)).isEqualTo("Avril 1980")
        assertThat(formatter.format(ts, DateFormatterMode.Day)).isEqualTo("Dimanche 6 avril")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate)).isEqualTo("18:35")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly)).isEqualTo("18:35")
    }

    @Test
    fun `test now relative`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-06T18:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full, true)).isEqualTo("18:35")
        assertThat(formatter.format(ts, DateFormatterMode.Month, true)).isEqualTo("Ce mois-ci")
        assertThat(formatter.format(ts, DateFormatterMode.Day, true)).isEqualTo("Aujourd’hui")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate, true)).isEqualTo("18:35")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly, true)).isEqualTo("18:35")
    }

    @Test
    fun `test one second before`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-06T18:35:23.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full)).isEqualTo("6 avril 1980 à 18:35")
        assertThat(formatter.format(ts, DateFormatterMode.Month)).isEqualTo("Avril 1980")
        assertThat(formatter.format(ts, DateFormatterMode.Day)).isEqualTo("Dimanche 6 avril")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate)).isEqualTo("18:35")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly)).isEqualTo("18:35")
    }

    @Test
    fun `test one second before relative`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-06T18:35:23.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full, true)).isEqualTo("18:35")
        assertThat(formatter.format(ts, DateFormatterMode.Month, true)).isEqualTo("Ce mois-ci")
        assertThat(formatter.format(ts, DateFormatterMode.Day, true)).isEqualTo("Aujourd’hui")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate, true)).isEqualTo("18:35")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly, true)).isEqualTo("18:35")
    }

    @Test
    fun `test one minute before`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-06T18:34:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full)).isEqualTo("6 avril 1980 à 18:34")
        assertThat(formatter.format(ts, DateFormatterMode.Month)).isEqualTo("Avril 1980")
        assertThat(formatter.format(ts, DateFormatterMode.Day)).isEqualTo("Dimanche 6 avril")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate)).isEqualTo("18:34")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly)).isEqualTo("18:34")
    }

    @Test
    fun `test one minute before relative`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-06T18:34:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full, true)).isEqualTo("18:34")
        assertThat(formatter.format(ts, DateFormatterMode.Month, true)).isEqualTo("Ce mois-ci")
        assertThat(formatter.format(ts, DateFormatterMode.Day, true)).isEqualTo("Aujourd’hui")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate, true)).isEqualTo("18:34")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly, true)).isEqualTo("18:34")
    }

    @Test
    fun `test one hour before`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-06T17:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full)).isEqualTo("6 avril 1980 à 17:35")
        assertThat(formatter.format(ts, DateFormatterMode.Month)).isEqualTo("Avril 1980")
        assertThat(formatter.format(ts, DateFormatterMode.Day)).isEqualTo("Dimanche 6 avril")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate)).isEqualTo("17:35")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly)).isEqualTo("17:35")
    }

    @Test
    fun `test one hour before relative`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-06T17:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full, true)).isEqualTo("17:35")
        assertThat(formatter.format(ts, DateFormatterMode.Month, true)).isEqualTo("Ce mois-ci")
        assertThat(formatter.format(ts, DateFormatterMode.Day, true)).isEqualTo("Aujourd’hui")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate, true)).isEqualTo("17:35")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly, true)).isEqualTo("17:35")
    }

    @Test
    fun `test one day before same time`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-05T18:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full)).isEqualTo("5 avril 1980 à 18:35")
        assertThat(formatter.format(ts, DateFormatterMode.Month)).isEqualTo("Avril 1980")
        assertThat(formatter.format(ts, DateFormatterMode.Day)).isEqualTo("Samedi 5 avril")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate)).isEqualTo("5 avr.")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly)).isEqualTo("18:35")
    }

    @Test
    fun `test one day before same time relative`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-05T18:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full, true)).isEqualTo("Hier à 18:35")
        assertThat(formatter.format(ts, DateFormatterMode.Month, true)).isEqualTo("Ce mois-ci")
        assertThat(formatter.format(ts, DateFormatterMode.Day, true)).isEqualTo("Hier")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate, true)).isEqualTo("Hier")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly, true)).isEqualTo("18:35")
    }

    @Test
    fun `test one month before same time`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-03-06T18:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full)).isEqualTo("6 mars 1980 à 18:35")
        assertThat(formatter.format(ts, DateFormatterMode.Month)).isEqualTo("Mars 1980")
        assertThat(formatter.format(ts, DateFormatterMode.Day)).isEqualTo("Jeudi 6 mars")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate)).isEqualTo("6 mars")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly)).isEqualTo("18:35")
    }

    @Test
    fun `test one month before same time relative`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-03-06T18:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full, true)).isEqualTo("Jeudi 6 mars à 18:35")
        assertThat(formatter.format(ts, DateFormatterMode.Month, true)).isEqualTo("Mars 1980")
        assertThat(formatter.format(ts, DateFormatterMode.Day, true)).isEqualTo("Jeudi 6 mars")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate, true)).isEqualTo("6 mars")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly, true)).isEqualTo("18:35")
    }

    @Test
    fun `test one year before same time`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1979-04-06T18:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full)).isEqualTo("6 avril 1979 à 18:35")
        assertThat(formatter.format(ts, DateFormatterMode.Month)).isEqualTo("Avril 1979")
        assertThat(formatter.format(ts, DateFormatterMode.Day)).isEqualTo("6 avril 1979")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate)).isEqualTo("06.04.1979")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly)).isEqualTo("18:35")
    }

    @Test
    fun `test one year before same time relative`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1979-04-06T18:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full, true)).isEqualTo("6 avril 1979 à 18:35")
        assertThat(formatter.format(ts, DateFormatterMode.Month, true)).isEqualTo("Avril 1979")
        assertThat(formatter.format(ts, DateFormatterMode.Day, true)).isEqualTo("6 avril 1979")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate, true)).isEqualTo("06.04.1979")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly, true)).isEqualTo("18:35")
    }

    /**
     * Create DefaultLastMessageFormatter and set current time to the provided date.
     */
    private fun createFormatter(@Suppress("SameParameterValue") currentDate: String): DefaultDateFormatter {
        val clock = FakeClock().apply { givenInstant(Instant.parse(currentDate)) }
        val localDateTimeProvider = LocalDateTimeProvider(clock) { TimeZone.UTC }
        val dateFormatters = DateFormatters(
            localeChangeObserver = {},
            clock = clock,
            timeZoneProvider = { TimeZone.UTC },
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
}
