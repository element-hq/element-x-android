/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.api.internal

import io.element.android.features.location.api.BuildConfig
import kotlin.math.roundToInt

/**
 * Builds an URL for MapTiler's Static Maps API.
 *
 * https://docs.maptiler.com/cloud/api/static-maps/
 */
internal class MapTilerStaticMapUrlBuilder(
    private val baseUrl: String,
    private val apiKey: String,
    private val lightMapId: String,
    private val darkMapId: String,
) : StaticMapUrlBuilder {
    constructor() : this(
        baseUrl = BuildConfig.MAPTILER_BASE_URL.removeSuffix("/"),
        apiKey = BuildConfig.MAPTILER_API_KEY,
        lightMapId = BuildConfig.MAPTILER_LIGHT_MAP_ID,
        darkMapId = BuildConfig.MAPTILER_DARK_MAP_ID,
    )

    override fun build(
        lat: Double,
        lon: Double,
        zoom: Double,
        darkMode: Boolean,
        width: Int,
        height: Int,
        density: Float
    ): String {
        val mapId = if (darkMode) darkMapId else lightMapId
        val finalZoom = zoom.coerceIn(zoomRange)

        // Request @2x density for xhdpi and above (xhdpi == 320dpi == 2x density).
        val is2x = density >= 2

        // Scale requested width/height according to the reported display density.
        val (finalWidth, finalHeight) = coerceWidthAndHeight(
            width = (width / density).roundToInt(),
            height = (height / density).roundToInt(),
            is2x = is2x,
        )

        val scale = if (is2x) "@2x" else ""

        // Since Maptiler doesn't support arbitrary dpi scaling, we stick to 2x sized
        // images even on displays with density higher than 2x, thereby yielding an
        // image smaller than the available space in pixels.
        // The resulting image will have to be scaled to fit the available space in order
        // to keep the perceived content size constant at the expense of sharpness.
        return "$baseUrl/$mapId/static/$lon,$lat,$finalZoom/${finalWidth}x${finalHeight}$scale.webp?key=$apiKey&attribution=bottomleft"
    }

    override fun isServiceAvailable() = apiKey.isNotEmpty()
}

private fun coerceWidthAndHeight(width: Int, height: Int, is2x: Boolean): Pair<Int, Int> {
    if (width <= 0 || height <= 0) {
        // This effectively yields an URL which asks for a 0x0 image which will result in an HTTP error,
        // but it's better than e.g. asking for a 1x1 image which would be unreadable and increase usage costs.
        return 0 to 0
    }
    val aspectRatio = width.toDouble() / height.toDouble()
    val range = if (is2x) widthHeightRange2x else widthHeightRange
    return if (width >= height) {
        width.coerceIn(range).let { coercedWidth ->
            coercedWidth to (coercedWidth / aspectRatio).roundToInt()
        }
    } else {
        height.coerceIn(range).let { coercedHeight ->
            (coercedHeight * aspectRatio).roundToInt() to coercedHeight
        }
    }
}

private val widthHeightRange = 1..2048 // API will error if outside 1-2048 range @1x.
private val widthHeightRange2x = 1..1024 // API will error if outside 1-1024 range @2x.
private val zoomRange = 0.0..22.0 // API will error if outside 0-22 range.
