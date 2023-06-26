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

import kotlin.math.roundToInt

private const val API_KEY = "fU3vlMsMn4Jb6dnEIFsx"
private const val BASE_URL = "https://api.maptiler.com"
private const val LIGHT_MAP_ID = "9bc819c8-e627-474a-a348-ec144fe3d810"
private const val DARK_MAP_ID = "dea61faf-292b-4774-9660-58fcef89a7f3"
private const val STATIC_MAP_FORMAT = "webp"
private const val STATIC_MAP_SCALE = "" // Either "" (empty string) for normal image or "@2x" for retina images.
private const val STATIC_MAP_MAX_WIDTH_HEIGHT = 2048
private const val STATIC_MAP_MAX_ZOOM = 22.0

internal fun buildTileServerUrl(
    darkMode: Boolean
): String = if (!darkMode) {
    "$BASE_URL/maps/$LIGHT_MAP_ID/style.json?key=$API_KEY"
} else {
    "$BASE_URL/maps/$DARK_MAP_ID/style.json?key=$API_KEY"
}

/**
 * Builds a valid URL for maptiler.com static map api based on the given params.
 *
 * Coerces width and height to the API maximum of 2048 keeping the requested aspect ratio.
 * Coerces zoom to the API maximum of 22.
 *
 * NB: This will throw if either width or height are <= 0. You need to handle this case upstream
 *  (hint: views can't have negative width or height but can have 0 width or height sometimes).
 */
internal fun buildStaticMapsApiUrl(
    lat: Double,
    lon: Double,
    desiredZoom: Double,
    desiredWidth: Int,
    desiredHeight: Int,
    darkMode: Boolean
): String {
    require(desiredWidth > 0 && desiredHeight > 0) {
        "Width ($desiredHeight) and height ($desiredHeight) must be > 0"
    }
    require(desiredZoom >= 0) { "Zoom ($desiredZoom) must be >= 0" }
    val zoom = desiredZoom.coerceAtMost(STATIC_MAP_MAX_ZOOM) // API will error if outside 0-22 range.
    val width: Int
    val height: Int
    if (desiredWidth <= STATIC_MAP_MAX_WIDTH_HEIGHT && desiredHeight <= STATIC_MAP_MAX_WIDTH_HEIGHT) {
        width = desiredWidth
        height = desiredHeight
    } else {
        val aspectRatio = desiredWidth.toDouble() / desiredHeight.toDouble()
        if (desiredWidth >= desiredHeight) {
            width = desiredWidth.coerceAtMost(STATIC_MAP_MAX_WIDTH_HEIGHT)
            height = (width / aspectRatio).roundToInt()
        } else {
            height = desiredHeight.coerceAtMost(STATIC_MAP_MAX_WIDTH_HEIGHT)
            width = (height * aspectRatio).roundToInt()
        }
    }
    return if (!darkMode) {
        "$BASE_URL/maps/$LIGHT_MAP_ID/static/${lon},${lat},${zoom}/${width}x${height}$STATIC_MAP_SCALE.$STATIC_MAP_FORMAT?key=$API_KEY"
    } else {
        "$BASE_URL/maps/$DARK_MAP_ID/static/${lon},${lat},${zoom}/${width}x${height}$STATIC_MAP_SCALE.$STATIC_MAP_FORMAT?key=$API_KEY"
    }
}
