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
): String = staticMapUrl(
    baseUrl = baseUrl,
    mapId = mapId(darkMode),
    lat = lat,
    lon = lon,
    zoom = zoom,
    width = width,
    height = height,
    retina = resources.configuration.densityDpi >= 320, // Use retina for hdpi and above (retina == 320dpi == xhdpi).
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
    retina: Boolean,
    apiKey: String,
): String {
    val width = if (retina) width / 2 else width
    val height = if (retina) height / 2 else height
    val scale = if (retina) "@2x" else ""
    return "${baseUrl}/${mapId}/static/${lon},${lat},${zoom}/${width}x${height}${scale}.webp?key=${apiKey}&attribution=bottomleft"
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

private fun Context.mapId(darkMode: Boolean) = when (darkMode) {
    true -> getString(R.string.maptiler_dark_map_id)
    false -> getString(R.string.maptiler_light_map_id)
}

private val Context.apiKey: String
    get() = getString(R.string.maptiler_api_key)
