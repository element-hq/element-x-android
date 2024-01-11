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

package io.element.android.features.location.api.internal

import android.content.Context
import kotlin.math.roundToInt

/**
 * Builds an URL for MapTiler's Static Maps API.
 *
 * https://docs.maptiler.com/cloud/api/static-maps/
 */
internal class MapTilerStaticMapUrlBuilder(
    private val apiKey: String,
    private val lightMapId: String,
    private val darkMapId: String,
) : StaticMapUrlBuilder {
    constructor(context: Context) : this(
        apiKey = context.apiKey,
        lightMapId = context.mapId(darkMode = false),
        darkMapId = context.mapId(darkMode = true),
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
        return "$MAPTILER_BASE_URL/$mapId/static/$lon,$lat,$finalZoom/${finalWidth}x${finalHeight}$scale.webp?key=$apiKey&attribution=bottomleft"
    }
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
