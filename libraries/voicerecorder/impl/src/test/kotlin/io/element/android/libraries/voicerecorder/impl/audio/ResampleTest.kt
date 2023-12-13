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

class ResampleTest {
    @Test
    fun `resample works`() {
        listOf(0.0f).resample(10).let {
            assertThat(it).isEqualTo(listOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f))
        }
        listOf(1.0f).resample(10).let {
            assertThat(it).isEqualTo(listOf(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f))
        }
        listOf(0.0f, 1.0f).resample(10).let {
            assertThat(it).isEqualTo(listOf(0.0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f))
        }
        listOf(0.0f, 0.5f, 1.0f).resample(10).let {
            assertThat(it).isEqualTo(listOf(0.0f, 0.15f, 0.3f, 0.45000002f, 0.6f, 0.75f, 0.90000004f, 1.0f, 1.0f, 1.0f))
        }
        List(100) { it.toFloat() }.resample(10).let {
            assertThat(it).isEqualTo(listOf(0.0f, 10.0f, 20.0f, 30.0f, 40.0f, 50.0f, 60.0f, 70.0f, 80.0f, 90.0f))
        }
    }
}
