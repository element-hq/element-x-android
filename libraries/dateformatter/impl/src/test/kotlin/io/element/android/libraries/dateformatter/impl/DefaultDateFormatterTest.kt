/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.dateformatter.api.DateFormatterMode
import kotlinx.datetime.Instant
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "en")
class DefaultDateFormatterTest {
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
        assertThat(formatter.format(ts, DateFormatterMode.Full)).isEqualTo("January 1, 1970 at 12:00 AM")
        assertThat(formatter.format(ts, DateFormatterMode.Month)).isEqualTo("January 1970")
        assertThat(formatter.format(ts, DateFormatterMode.Day)).isEqualTo("January 1, 1970")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate)).isEqualTo("01.01.1970")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly)).isEqualTo("12:00 AM")
    }

    @Test
    fun `test epoch relative`() {
        val now = "1980-04-06T18:35:24.00Z"
        val ts = 0L
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full, true)).isEqualTo("January 1, 1970 at 12:00 AM")
        assertThat(formatter.format(ts, DateFormatterMode.Month, true)).isEqualTo("January 1970")
        assertThat(formatter.format(ts, DateFormatterMode.Day, true)).isEqualTo("January 1, 1970")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate, true)).isEqualTo("01.01.1970")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly, true)).isEqualTo("12:00 AM")
    }

    @Test
    fun `test now`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-06T18:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full)).isEqualTo("April 6, 1980 at 6:35 PM")
        assertThat(formatter.format(ts, DateFormatterMode.Month)).isEqualTo("April 1980")
        assertThat(formatter.format(ts, DateFormatterMode.Day)).isEqualTo("Sunday 6 April")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate)).isEqualTo("6:35 PM")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly)).isEqualTo("6:35 PM")
    }

    @Test
    fun `test now relative`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-06T18:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full, true)).isEqualTo("6:35 PM")
        assertThat(formatter.format(ts, DateFormatterMode.Month, true)).isEqualTo("This month")
        assertThat(formatter.format(ts, DateFormatterMode.Day, true)).isEqualTo("Today")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate, true)).isEqualTo("6:35 PM")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly, true)).isEqualTo("6:35 PM")
    }

    @Test
    fun `test one second before`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-06T18:35:23.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full)).isEqualTo("April 6, 1980 at 6:35 PM")
        assertThat(formatter.format(ts, DateFormatterMode.Month)).isEqualTo("April 1980")
        assertThat(formatter.format(ts, DateFormatterMode.Day)).isEqualTo("Sunday 6 April")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate)).isEqualTo("6:35 PM")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly)).isEqualTo("6:35 PM")
    }

    @Test
    fun `test one second before relative`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-06T18:35:23.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full, true)).isEqualTo("6:35 PM")
        assertThat(formatter.format(ts, DateFormatterMode.Month, true)).isEqualTo("This month")
        assertThat(formatter.format(ts, DateFormatterMode.Day, true)).isEqualTo("Today")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate, true)).isEqualTo("6:35 PM")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly, true)).isEqualTo("6:35 PM")
    }

    @Test
    fun `test one minute before`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-06T18:34:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full)).isEqualTo("April 6, 1980 at 6:34 PM")
        assertThat(formatter.format(ts, DateFormatterMode.Month)).isEqualTo("April 1980")
        assertThat(formatter.format(ts, DateFormatterMode.Day)).isEqualTo("Sunday 6 April")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate)).isEqualTo("6:34 PM")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly)).isEqualTo("6:34 PM")
    }

    @Test
    fun `test one minute before relative`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-06T18:34:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full, true)).isEqualTo("6:34 PM")
        assertThat(formatter.format(ts, DateFormatterMode.Month, true)).isEqualTo("This month")
        assertThat(formatter.format(ts, DateFormatterMode.Day, true)).isEqualTo("Today")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate, true)).isEqualTo("6:34 PM")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly, true)).isEqualTo("6:34 PM")
    }

    @Test
    fun `test one hour before`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-06T17:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full)).isEqualTo("April 6, 1980 at 5:35 PM")
        assertThat(formatter.format(ts, DateFormatterMode.Month)).isEqualTo("April 1980")
        assertThat(formatter.format(ts, DateFormatterMode.Day)).isEqualTo("Sunday 6 April")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate)).isEqualTo("5:35 PM")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly)).isEqualTo("5:35 PM")
    }

    @Test
    fun `test one hour before relative`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-06T17:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full, true)).isEqualTo("5:35 PM")
        assertThat(formatter.format(ts, DateFormatterMode.Month, true)).isEqualTo("This month")
        assertThat(formatter.format(ts, DateFormatterMode.Day, true)).isEqualTo("Today")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate, true)).isEqualTo("5:35 PM")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly, true)).isEqualTo("5:35 PM")
    }

    @Test
    fun `test one day before same time`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-05T18:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full)).isEqualTo("April 5, 1980 at 6:35 PM")
        assertThat(formatter.format(ts, DateFormatterMode.Month)).isEqualTo("April 1980")
        assertThat(formatter.format(ts, DateFormatterMode.Day)).isEqualTo("Saturday 5 April")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate)).isEqualTo("5 Apr")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly)).isEqualTo("6:35 PM")
    }

    @Test
    fun `test one day before same time relative`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-05T18:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full, true)).isEqualTo("Yesterday at 6:35 PM")
        assertThat(formatter.format(ts, DateFormatterMode.Month, true)).isEqualTo("This month")
        assertThat(formatter.format(ts, DateFormatterMode.Day, true)).isEqualTo("Yesterday")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate, true)).isEqualTo("Yesterday")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly, true)).isEqualTo("6:35 PM")
    }

    @Test
    fun `test two days before same time`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-04T18:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full)).isEqualTo("April 4, 1980 at 6:35 PM")
        assertThat(formatter.format(ts, DateFormatterMode.Month)).isEqualTo("April 1980")
        assertThat(formatter.format(ts, DateFormatterMode.Day)).isEqualTo("Friday 4 April")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate)).isEqualTo("4 Apr")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly)).isEqualTo("6:35 PM")
    }

    @Test
    fun `test two days before same time relative`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-04-04T18:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full, true)).isEqualTo("Friday at 6:35 PM")
        assertThat(formatter.format(ts, DateFormatterMode.Month, true)).isEqualTo("This month")
        assertThat(formatter.format(ts, DateFormatterMode.Day, true)).isEqualTo("Friday")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate, true)).isEqualTo("4 Apr")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly, true)).isEqualTo("6:35 PM")
    }

    @Test
    fun `test one month before same time`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-03-06T18:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full)).isEqualTo("March 6, 1980 at 6:35 PM")
        assertThat(formatter.format(ts, DateFormatterMode.Month)).isEqualTo("March 1980")
        assertThat(formatter.format(ts, DateFormatterMode.Day)).isEqualTo("Thursday 6 March")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate)).isEqualTo("6 Mar")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly)).isEqualTo("6:35 PM")
    }

    @Test
    fun `test one month before same time relative`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1980-03-06T18:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full, true)).isEqualTo("Thursday 6 March at 6:35 PM")
        assertThat(formatter.format(ts, DateFormatterMode.Month, true)).isEqualTo("March 1980")
        assertThat(formatter.format(ts, DateFormatterMode.Day, true)).isEqualTo("Thursday 6 March")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate, true)).isEqualTo("6 Mar")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly, true)).isEqualTo("6:35 PM")
    }

    @Test
    fun `test one year before same time`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1979-04-06T18:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full)).isEqualTo("April 6, 1979 at 6:35 PM")
        assertThat(formatter.format(ts, DateFormatterMode.Month)).isEqualTo("April 1979")
        assertThat(formatter.format(ts, DateFormatterMode.Day)).isEqualTo("April 6, 1979")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate)).isEqualTo("06.04.1979")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly)).isEqualTo("6:35 PM")
    }

    @Test
    fun `test one year before same time relative`() {
        val now = "1980-04-06T18:35:24.00Z"
        val dat = "1979-04-06T18:35:24.00Z"
        val ts = Instant.parse(dat).toEpochMilliseconds()
        val formatter = createFormatter(now)
        assertThat(formatter.format(ts, DateFormatterMode.Full, true)).isEqualTo("April 6, 1979 at 6:35 PM")
        assertThat(formatter.format(ts, DateFormatterMode.Month, true)).isEqualTo("April 1979")
        assertThat(formatter.format(ts, DateFormatterMode.Day, true)).isEqualTo("April 6, 1979")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOrDate, true)).isEqualTo("06.04.1979")
        assertThat(formatter.format(ts, DateFormatterMode.TimeOnly, true)).isEqualTo("6:35 PM")
    }
}
