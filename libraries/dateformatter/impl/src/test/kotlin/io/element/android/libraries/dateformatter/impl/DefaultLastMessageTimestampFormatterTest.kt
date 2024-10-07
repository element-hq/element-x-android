/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.dateformatter.api.LastMessageTimestampFormatter
import io.element.android.libraries.dateformatter.test.FakeClock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import java.util.Locale

class DefaultLastMessageTimestampFormatterTest {
    @Test
    fun `test null`() {
        val now = "1980-04-06T18:35:24.00Z"
        val formatter = createFormatter(now)
        assertThat(formatter.format(null)).isEmpty()
    }

    @Test
    fun `test epoch`() {
        val now = "1980-04-06T18:35:24.00Z"
        val formatter = createFormatter(now)
        assertThat(formatter.format(0)).isEqualTo("01.01.1970")
    }

    @Test
    fun `test now`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-06T18:35:24.00Z"
        val formatter = createFormatter(now)
        assertThat(formatter.format(Instant.parse(dat).toEpochMilliseconds())).isEqualTo("6:35 PM")
    }

    @Test
    fun `test one second before`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-06T18:35:23.00Z"
        val formatter = createFormatter(now)
        assertThat(formatter.format(Instant.parse(dat).toEpochMilliseconds())).isEqualTo("6:35 PM")
    }

    @Test
    fun `test one minute before`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-06T18:34:24.00Z"
        val formatter = createFormatter(now)
        assertThat(formatter.format(Instant.parse(dat).toEpochMilliseconds())).isEqualTo("6:34 PM")
    }

    @Test
    fun `test one hour before`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-06T17:35:24.00Z"
        val formatter = createFormatter(now)
        assertThat(formatter.format(Instant.parse(dat).toEpochMilliseconds())).isEqualTo("5:35 PM")
    }

    @Test
    fun `test one day before same time`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-05T18:35:24.00Z"
        val formatter = createFormatter(now)
        // TODO DateUtils.getRelativeTimeSpanString returns null.
        assertThat(formatter.format(Instant.parse(dat).toEpochMilliseconds())).isEqualTo("")
    }

    @Test
    fun `test one month before same time`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-03-06T18:35:24.00Z"
        val formatter = createFormatter(now)
        assertThat(formatter.format(Instant.parse(dat).toEpochMilliseconds())).isEqualTo("6 Mar")
    }

    @Test
    fun `test one year before same time`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1979-04-06T18:35:24.00Z"
        val formatter = createFormatter(now)
        assertThat(formatter.format(Instant.parse(dat).toEpochMilliseconds())).isEqualTo("06.04.1979")
    }

    @Test
    fun `test full format`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1979-04-06T18:35:24.00Z"
        val clock = FakeClock().apply { givenInstant(Instant.parse(now)) }
        val dateFormatters = DateFormatters(Locale.US, clock, TimeZone.UTC)
        assertThat(dateFormatters.formatDateWithFullFormat(Instant.parse(dat).toLocalDateTime(TimeZone.UTC))).isEqualTo("Friday, April 6, 1979")
    }

    /**
     * Create DefaultLastMessageFormatter and set current time to the provided date.
     */
    private fun createFormatter(@Suppress("SameParameterValue") currentDate: String): LastMessageTimestampFormatter {
        val clock = FakeClock().apply { givenInstant(Instant.parse(currentDate)) }
        val localDateTimeProvider = LocalDateTimeProvider(clock, TimeZone.UTC)
        val dateFormatters = DateFormatters(Locale.US, clock, TimeZone.UTC)
        return DefaultLastMessageTimestampFormatter(localDateTimeProvider, dateFormatters)
    }
}
