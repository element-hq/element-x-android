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
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.element.android.features.location.api.R
import io.element.android.libraries.theme.ElementTheme
import kotlin.math.roundToInt

/**
 * Provides the URL to an image that contains a statically-generated map of the given location.
 */
fun Context.staticMapUrl(
    baseUrl: String = MAPTILER_BASE_URL,
    darkMode: Boolean,
    lat: Double,
    lon: Double,
    zoom: Double,
    width: Int,
    height: Int,
    density: Float,
): String = staticMapUrl(
    baseUrl = baseUrl,
    mapId = mapId(darkMode),
    lat = lat,
    lon = lon,
    zoom = zoom,
    width = width,
    height = height,
    density = density,
    apiKey = apiKey,
)

/**
 * Builds a maptiler URL for a static map image.
 *
 * API doc: https://docs.maptiler.com/cloud/api/static-maps/
 */
@VisibleForTesting
internal fun staticMapUrl(
    baseUrl: String = MAPTILER_BASE_URL,
    mapId: String,
    lat: Double,
    lon: Double,
    zoom: Double,
    width: Int,
    height: Int,
    density: Float,
    apiKey: String,
): String {
    val zoom = zoom.coerceIn(zoomRange)

    // Request @2x density for xhdpi and above (xhdpi == 320dpi == 2x density).
    val is2x = density >= 2

    // Scale requested width/height according to the reported display density.
    val (width, height) = coerceToValidWidthHeight(
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
    return "${baseUrl}/${mapId}/static/${lon},${lat},${zoom}/${width}x${height}${scale}.webp?key=${apiKey}&attribution=bottomleft"
}

private fun coerceToValidWidthHeight(width: Int, height: Int, is2x: Boolean): Pair<Int, Int> {
    val range = if (is2x) widthHeightRange2x else widthHeightRange
    val aspectRatio = width.coerceAtLeast(1).toDouble() / height.coerceAtLeast(1).toDouble()
    return if (width >= height) {
        width.coerceIn(range).let { width ->
            width to (width / aspectRatio).roundToInt()
        }
    } else {
        height.coerceIn(range).let { height ->
            (height * aspectRatio).roundToInt() to height
        }
    }
}

/**
 * Utility function to remember the tile server URL based on the current theme.
 */
@Composable
fun rememberTileStyleUrl(): String {
    val context = LocalContext.current
    val darkMode = !ElementTheme.isLightTheme
    return remember(darkMode) {
        tileStyleUrl(
            mapId = context.mapId(darkMode),
            apiKey = context.apiKey,
        )
    }
}

/**
 * Provides the URL to a MapLibre style document, used for rendering dynamic maps.
 */
@VisibleForTesting
internal fun tileStyleUrl(
    baseUrl: String = MAPTILER_BASE_URL,
    mapId: String,
    apiKey: String,
): String {
    return "${baseUrl}/${mapId}/style.json?key=${apiKey}"
}

private const val MAPTILER_BASE_URL = "https://api.maptiler.com/maps"
private val widthHeightRange = 1..2048 // API will error if outside 1-2048 range @1x.
private val widthHeightRange2x = 1..1024 // API will error if outside 1-1024 range @2x.
private val zoomRange = 0.0..22.0 // API will error if outside 0-22 range.

private fun Context.mapId(darkMode: Boolean) = when (darkMode) {
    true -> getString(R.string.maptiler_dark_map_id)
    false -> getString(R.string.maptiler_light_map_id)
}

private val Context.apiKey: String
    get() = getString(R.string.maptiler_api_key)
