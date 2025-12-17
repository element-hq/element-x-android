/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.api

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DurationFormatterTest {
    @Test
    fun `format seconds only`() {
        assertThat(buildDuration().toHumanReadableDuration()).isEqualTo("0:00")
        assertThat(buildDuration(seconds = 1).toHumanReadableDuration()).isEqualTo("0:01")
        assertThat(buildDuration(seconds = 59).toHumanReadableDuration()).isEqualTo("0:59")
    }

    @Test
    fun `format minutes and seconds`() {
        assertThat(buildDuration(minutes = 1).toHumanReadableDuration()).isEqualTo("1:00")
        assertThat(buildDuration(minutes = 1, seconds = 30).toHumanReadableDuration()).isEqualTo("1:30")
        assertThat(buildDuration(minutes = 59, seconds = 59).toHumanReadableDuration()).isEqualTo("59:59")
    }

    @Test
    fun `format hours, minutes and seconds`() {
        assertThat(buildDuration(hours = 1).toHumanReadableDuration()).isEqualTo("1:00:00")
        assertThat(buildDuration(hours = 1, minutes = 1, seconds = 1).toHumanReadableDuration()).isEqualTo("1:01:01")
        assertThat(buildDuration(hours = 24, minutes = 59, seconds = 59).toHumanReadableDuration()).isEqualTo("24:59:59")
        assertThat(buildDuration(hours = 25, minutes = 0, seconds = 0).toHumanReadableDuration()).isEqualTo("25:00:00")
    }

    private fun buildDuration(
        hours: Int = 0,
        minutes: Int = 0,
        seconds: Int = 0
    ): Long {
        return (hours * 60 * 60 + minutes * 60 + seconds) * 1000L
    }
}
