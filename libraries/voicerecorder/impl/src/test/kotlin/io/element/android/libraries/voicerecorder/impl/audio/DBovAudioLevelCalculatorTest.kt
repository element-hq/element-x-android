/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.impl.audio

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DBovAudioLevelCalculatorTest {
    @Test
    fun `given max values, it returns 1`() {
        val calculator = DBovAudioLevelCalculator()
        val buffer = ShortArray(100) { Short.MAX_VALUE }
        val level = calculator.calculateAudioLevel(buffer)
        assertThat(level).isEqualTo(1.0f)
    }

    @Test
    fun `given mixed values, it returns values within range`() {
        val calculator = DBovAudioLevelCalculator()
        val buffer = shortArrayOf(100, -200, 300, -400, 500, -600, 700, -800, 900, -1000)
        val level = calculator.calculateAudioLevel(buffer)
        assertThat(level).apply {
            isGreaterThan(0f)
            isLessThan(1f)
        }
    }

    @Test
    fun `given min values, it returns 0`() {
        val calculator = DBovAudioLevelCalculator()
        val buffer = ShortArray(100) { 0 }
        val level = calculator.calculateAudioLevel(buffer)
        assertThat(level).isEqualTo(0.0f)
    }
}
