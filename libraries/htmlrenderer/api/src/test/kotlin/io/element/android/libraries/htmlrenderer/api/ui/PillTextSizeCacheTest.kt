/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.api.ui

import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Test

class PillTextSizeCacheTest {
    private val style = TextStyle(fontSize = 16.sp)
    private val density = Density(density = 2f, fontScale = 1f)

    @After
    fun tearDown() = PillTextSizeCache.clear()

    @Test
    fun `the same text is only measured once`() {
        val textMeasurer = aTextMeasurer(IntSize(100, 40))
        val first = PillTextSizeCache.getOrMeasure("@Alice", style, density, LayoutDirection.Ltr, textMeasurer)
        val second = PillTextSizeCache.getOrMeasure("@Alice", style, density, LayoutDirection.Ltr, textMeasurer)
        assertThat(first).isEqualTo(IntSize(100, 40))
        assertThat(second).isEqualTo(IntSize(100, 40))
        verify(exactly = 1) { textMeasurer.measure(text = "@Alice", style = any(), density = any(), layoutDirection = any()) }
    }

    @Test
    fun `a different text, style, density or layout direction is measured again`() {
        val textMeasurer = aTextMeasurer(IntSize(100, 40))
        PillTextSizeCache.getOrMeasure("@Alice", style, density, LayoutDirection.Ltr, textMeasurer)
        PillTextSizeCache.getOrMeasure("@Bob", style, density, LayoutDirection.Ltr, textMeasurer)
        PillTextSizeCache.getOrMeasure("@Alice", style.copy(fontSize = 20.sp), density, LayoutDirection.Ltr, textMeasurer)
        PillTextSizeCache.getOrMeasure("@Alice", style, Density(density = 2f, fontScale = 1.5f), LayoutDirection.Ltr, textMeasurer)
        PillTextSizeCache.getOrMeasure("@Alice", style, density, LayoutDirection.Rtl, textMeasurer)
        verify(exactly = 5) { textMeasurer.measure(text = any<String>(), style = any(), density = any(), layoutDirection = any()) }
    }

    private fun aTextMeasurer(size: IntSize): TextMeasurer {
        val result = mockk<TextLayoutResult> { every { this@mockk.size } returns size }
        return mockk {
            every { measure(text = any<String>(), style = any(), density = any(), layoutDirection = any()) } returns result
        }
    }
}
