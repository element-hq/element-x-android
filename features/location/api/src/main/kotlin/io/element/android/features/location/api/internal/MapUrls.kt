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
import io.element.android.features.location.api.R

/**
 * Provides the URL to an image that contains a statically-generated map of the given location.
 */
fun staticMapUrl(
    context: Context,
    lat: Double,
    lon: Double,
    zoom: Double,
    width: Int,
    height: Int,
    darkMode: Boolean,
): String {
    return "${baseUrl(darkMode)}/static/${lon},${lat},${zoom}/${width}x${height}@2x.webp?key=${context.apiKey}&attribution=bottomleft"
}

/**
 * Provides the URL to a MapLibre style document, used for rendering dynamic maps.
 */
fun tileStyleUrl(
    context: Context,
    darkMode: Boolean,
): String {
    return "${baseUrl(darkMode)}/style.json?key=${context.apiKey}"
}

private fun baseUrl(darkMode: Boolean) =
    "https://api.maptiler.com/maps/" +
        if (darkMode)
            "dea61faf-292b-4774-9660-58fcef89a7f3"
        else
            "9bc819c8-e627-474a-a348-ec144fe3d810"

private val Context.apiKey: String
    get() = getString(R.string.maptiler_api_key)
