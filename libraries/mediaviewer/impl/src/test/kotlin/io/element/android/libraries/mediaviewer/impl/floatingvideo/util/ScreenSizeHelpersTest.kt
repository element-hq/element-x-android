/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.floatingvideo.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ScreenSizeHelpersTest {
    @Test
    fun `calculateDimensions minimized landscape uses the wide fraction`() {
        val result = calculateDimensions(aspectRatio = 16f / 9f, isMinimized = true, screenWidth = 1000)
        assertThat(result.x).isEqualTo(600)
    }

    @Test
    fun `calculateDimensions minimized portrait uses the narrow fraction`() {
        val result = calculateDimensions(aspectRatio = 9f / 16f, isMinimized = true, screenWidth = 1000)
        assertThat(result.x).isEqualTo(350)
    }

    @Test
    fun `calculateDimensions maximized always uses the full fraction`() {
        val result = calculateDimensions(aspectRatio = 9f / 16f, isMinimized = false, screenWidth = 1000)
        assertThat(result.x).isEqualTo(900)
    }

    @Test
    fun `calculateDimensions derives height from width and aspect ratio`() {
        val result = calculateDimensions(aspectRatio = 2f, isMinimized = false, screenWidth = 1000)
        assertThat(result.y).isEqualTo(result.x / 2)
    }

    @Test
    fun `calculateDimensions falls back to 16by9 for a zero aspect ratio`() {
        val invalid = calculateDimensions(aspectRatio = 0f, isMinimized = false, screenWidth = 1000)
        val fallback = calculateDimensions(aspectRatio = 16f / 9f, isMinimized = false, screenWidth = 1000)
        assertThat(invalid).isEqualTo(fallback)
    }

    @Test
    fun `calculateDimensions falls back to 16by9 for a negative aspect ratio`() {
        val invalid = calculateDimensions(aspectRatio = -1f, isMinimized = false, screenWidth = 1000)
        val fallback = calculateDimensions(aspectRatio = 16f / 9f, isMinimized = false, screenWidth = 1000)
        assertThat(invalid).isEqualTo(fallback)
    }

    @Test
    fun `calculateDimensions falls back to 16by9 for a NaN aspect ratio`() {
        val invalid = calculateDimensions(aspectRatio = Float.NaN, isMinimized = false, screenWidth = 1000)
        val fallback = calculateDimensions(aspectRatio = 16f / 9f, isMinimized = false, screenWidth = 1000)
        assertThat(invalid).isEqualTo(fallback)
    }

    @Test
    fun `coerceInScreenBounds keeps the position when it stays within bounds`() {
        val result = coerceInScreenBounds(currentPosition = 100, delta = 50, screenSize = 1000, windowSize = 200)
        assertThat(result).isEqualTo(150)
    }

    @Test
    fun `coerceInScreenBounds clamps to zero when the delta pushes off the start edge`() {
        val result = coerceInScreenBounds(currentPosition = 10, delta = -50, screenSize = 1000, windowSize = 200)
        assertThat(result).isEqualTo(0)
    }

    @Test
    fun `coerceInScreenBounds clamps so the window stays fully on screen at the end edge`() {
        val result = coerceInScreenBounds(currentPosition = 700, delta = 500, screenSize = 1000, windowSize = 200)
        assertThat(result).isEqualTo(800)
    }

    @Test
    fun `coerceInScreenBounds does not throw when the window is bigger than the screen`() {
        val result = coerceInScreenBounds(currentPosition = 50, delta = 10, screenSize = 300, windowSize = 500)
        assertThat(result).isEqualTo(0)
    }
}
