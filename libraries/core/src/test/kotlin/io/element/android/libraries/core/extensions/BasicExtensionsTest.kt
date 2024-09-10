/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
