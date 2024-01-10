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

package io.element.android.libraries.core.extensions

import org.junit.Assert.assertEquals
import org.junit.Test

class BasicExtensionsTest {
    @Test(expected = IllegalArgumentException::class)
    fun `test ellipsize at 0`() {
        "1234567890".ellipsize(0)
    }

    @Test
    fun `test ellipsize at 1`() {
        assertEquals(
            "1…",
            "1234567890".ellipsize(1)
        )
    }

    @Test
    fun `test ellipsize at 5`() {
        val output = "1234567890".ellipsize(5)
        assertEquals("12345…", output)
    }

    @Test
    fun `test ellipsize noop 1`() {
        val input = "12345"
        val output = input.ellipsize(5)
        assertEquals(input, output)
    }

    @Test
    fun `test ellipsize noop 2`() {
        val input = "123"
        val output = input.ellipsize(5)
        assertEquals(input, output)
    }
}
