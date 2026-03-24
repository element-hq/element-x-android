/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.Locale
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "en", sdk = [Build.VERSION_CODES.TIRAMISU])
class DefaultDurationFormatterTest {
    private fun createDurationFormatter(): DefaultDurationFormatter {
        return DefaultDurationFormatter(
            localeChangeObserver = {},
            locale = Locale.US,
        )
    }

    @Test
    fun `test zero duration`() {
        val formatter = createDurationFormatter()
        assertThat(formatter.format(0.seconds)).isEqualTo("0 seconds")
    }

    @Test
    fun `test 1 second`() {
        val formatter = createDurationFormatter()
        assertThat(formatter.format(1.seconds)).isEqualTo("1 second")
    }

    @Test
    fun `test 30 seconds`() {
        val formatter = createDurationFormatter()
        assertThat(formatter.format(30.seconds)).isEqualTo("30 seconds")
    }

    @Test
    fun `test 59 seconds`() {
        val formatter = createDurationFormatter()
        assertThat(formatter.format(59.seconds)).isEqualTo("59 seconds")
    }

    @Test
    fun `test 1 minute`() {
        val formatter = createDurationFormatter()
        assertThat(formatter.format(1.minutes)).isEqualTo("1 minute")
    }

    @Test
    fun `test 1 minute 29 seconds rounds to 1 minute`() {
        val formatter = createDurationFormatter()
        assertThat(formatter.format(1.minutes + 29.seconds)).isEqualTo("1 minute")
    }

    @Test
    fun `test 1 minute 30 seconds rounds to 2 minutes`() {
        val formatter = createDurationFormatter()
        assertThat(formatter.format(1.minutes + 30.seconds)).isEqualTo("2 minutes")
    }

    @Test
    fun `test 45 minutes`() {
        val formatter = createDurationFormatter()
        assertThat(formatter.format(45.minutes)).isEqualTo("45 minutes")
    }

    @Test
    fun `test 59 minutes`() {
        val formatter = createDurationFormatter()
        assertThat(formatter.format(59.minutes)).isEqualTo("59 minutes")
    }

    @Test
    fun `test 1 hour`() {
        val formatter = createDurationFormatter()
        assertThat(formatter.format(1.hours)).isEqualTo("1 hour")
    }

    @Test
    fun `test 1 hour 29 minutes rounds to 1 hour`() {
        val formatter = createDurationFormatter()
        assertThat(formatter.format(1.hours + 29.minutes)).isEqualTo("1 hour")
    }

    @Test
    fun `test 1 hour 30 minutes rounds to 2 hours`() {
        val formatter = createDurationFormatter()
        assertThat(formatter.format(1.hours + 30.minutes)).isEqualTo("2 hours")
    }

    @Test
    fun `test 2 hours 30 minutes rounds to 3 hours`() {
        val formatter = createDurationFormatter()
        assertThat(formatter.format(2.hours + 30.minutes)).isEqualTo("3 hours")
    }

    @Test
    fun `test 5 hours`() {
        val formatter = createDurationFormatter()
        assertThat(formatter.format(5.hours)).isEqualTo("5 hours")
    }

    @Test
    fun `test 24 hours`() {
        val formatter = createDurationFormatter()
        assertThat(formatter.format(24.hours)).isEqualTo("24 hours")
    }

    @Test
    fun `test rounding at seconds threshold - 499ms rounds to 0 seconds`() {
        val formatter = createDurationFormatter()
        assertThat(formatter.format(499.milliseconds)).isEqualTo("0 seconds")
    }

    @Test
    fun `test rounding at seconds threshold - 500ms rounds to 1 second`() {
        val formatter = createDurationFormatter()
        assertThat(formatter.format(500.milliseconds)).isEqualTo("1 second")
    }
}
