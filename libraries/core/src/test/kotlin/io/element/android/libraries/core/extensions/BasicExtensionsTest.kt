/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.extensions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun `given text with RtL unicode override, when checking contains RtL Override, then returns true`() {
        val textWithRtlOverride = "hello\u202Eworld"
        val result = textWithRtlOverride.containsRtLOverride()
        assertTrue(result)
    }

    @Test
    fun `given text without RtL unicode override, when checking contains RtL Override, then returns false`() {
        val textWithRtlOverride = "hello world"
        val result = textWithRtlOverride.containsRtLOverride()
        assertFalse(result)
    }

    @Test
    fun `given text with RtL unicode override, when ensuring ends LtR, then appends a LtR unicode override`() {
        val textWithRtlOverride = "123\u202E456"
        val result = textWithRtlOverride.ensureEndsLeftToRight()
        assertEquals("$textWithRtlOverride\u202D", result)
    }

    @Test
    fun `given text with unicode direction overrides, when filtering direction overrides, then removes all overrides`() {
        val textWithDirectionOverrides = "123\u202E456\u202d789"
        val result = textWithDirectionOverrides.filterDirectionOverrides()
        assertEquals("123456789", result)
    }
}
