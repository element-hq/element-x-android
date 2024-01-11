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
