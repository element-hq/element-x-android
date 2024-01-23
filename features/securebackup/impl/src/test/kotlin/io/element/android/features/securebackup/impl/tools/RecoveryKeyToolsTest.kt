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

package io.element.android.features.securebackup.impl.tools

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RecoveryKeyToolsTest {
    @Test
    fun `isRecoveryKeyFormatValid return false for invalid key`() {
        val sut = RecoveryKeyTools()
        assertThat(sut.isRecoveryKeyFormatValid("")).isFalse()
        // Wrong size
        assertThat(sut.isRecoveryKeyFormatValid("abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabc")).isFalse()
        assertThat(sut.isRecoveryKeyFormatValid("abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcda")).isFalse()
        // Wrong alphabet 0
        assertThat(sut.isRecoveryKeyFormatValid("0bcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd")).isFalse()
        // Wrong alphabet O
        assertThat(sut.isRecoveryKeyFormatValid("Obcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd")).isFalse()
        // Wrong alphabet l
        assertThat(sut.isRecoveryKeyFormatValid("lbcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd")).isFalse()
    }

    @Test
    fun `isRecoveryKeyFormatValid return true for valid key`() {
        val sut = RecoveryKeyTools()
        assertThat(sut.isRecoveryKeyFormatValid("abcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcdabcd")).isTrue()
        // Spaces does not count
        assertThat(sut.isRecoveryKeyFormatValid("abcd abcd abcd abcd abcd abcd abcd abcd abcd abcd abcd abcd")).isTrue()
    }
}
