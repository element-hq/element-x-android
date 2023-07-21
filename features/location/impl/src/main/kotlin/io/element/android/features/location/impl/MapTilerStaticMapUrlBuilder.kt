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

package io.element.android.features.location.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.location.api.StaticMapUrlBuilder
import io.element.android.libraries.di.AppScope
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Builds an URL for MapTiler's Static Maps API.
 *
 * https://docs.maptiler.com/cloud/api/static-maps/
 */
@ContributesBinding(AppScope::class)
class MapTilerStaticMapUrlBuilder @Inject constructor() : StaticMapUrlBuilder {
    override fun build(
        lat: Double,
        lon: Double,
        zoom: Double,
        width: Int,
        height: Int,
        darkMode: Boolean,
        scalingFactor: Float,
    ): String {
        val width: Int = (width / scalingFactor).roundToInt()
        val height: Int = (height / scalingFactor).roundToInt()
        val scale = if (scalingFactor >= 2) "@2x" else ""
        val mapId = when (darkMode) {
            true -> MapTilerConfig.DARK_MAP_ID
            false -> MapTilerConfig.LIGHT_MAP_ID
        }
        return "${MapTilerConfig.BASE_URL}/${mapId}/static/${lon},${lat},${zoom}/${width}x${height}${scale}.webp?key=${MapTilerConfig.API_KEY}&attribution=bottomleft"
    }
}
