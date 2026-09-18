/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.screenshot.utils

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import com.android.resources.Density

// Screenshots are captured at xxhdpi so the icon grids stay sharp. This is the density scale factor
// (480dpi / 160dpi), used to convert the dp bounds below into the pixel values [DeviceConfig] expects.
private const val DENSITY_SCALE = 2

// Upper bounds for self-sizing screenshots, in dp. Large enough to fit the tallest and widest
// Compound preview; `RenderingMode.SHRINK` then crops each image down to its actual content.
private const val MAX_WIDTH_DP = 2048
private const val MAX_HEIGHT_DP = 4800

/**
 * Creates a [Paparazzi] rule for the Compound screenshot tests.
 *
 * By default each screenshot sizes itself to its content ([RenderingMode.SHRINK]) instead of being
 * rendered at a fixed device frame size, and the [DeviceConfig] only acts as an upper bound.
 *
 * Pass [widthDp] and [heightDp] for previews that rely on `fillMaxSize`/`weight` and therefore need
 * a bounded frame to lay out correctly: they are then rendered at that fixed size instead.
 *
 * [useDeviceResolution] is enabled so screenshots are captured at full device resolution. Without
 * it, Paparazzi downscales every image to a 1000px max dimension, which makes large previews (such
 * as the icon grids) look pixelated.
 */
fun createPaparazziRule(
    widthDp: Int? = null,
    heightDp: Int? = null,
): Paparazzi {
    val selfSizing = widthDp == null || heightDp == null
    return Paparazzi(
        deviceConfig = DeviceConfig(
            screenWidth = (widthDp ?: MAX_WIDTH_DP) * DENSITY_SCALE,
            screenHeight = (heightDp ?: MAX_HEIGHT_DP) * DENSITY_SCALE,
            xdpi = 480,
            ydpi = 480,
            density = Density.XXHIGH,
            softButtons = false,
        ),
        renderingMode = if (selfSizing) RenderingMode.SHRINK else RenderingMode.NORMAL,
        supportsRtl = true,
        useDeviceResolution = true,
        maxPercentDifference = 0.01,
    )
}
