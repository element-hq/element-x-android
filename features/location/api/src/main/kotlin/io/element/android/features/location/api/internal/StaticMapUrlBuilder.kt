/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.api.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.element.android.features.enterprise.api.remoteconfig.MapTilerConfig
/**
 * Builds an URL for a 3rd party service provider static maps API.
 */
interface StaticMapUrlBuilder {
    /**
     * Builds the URL of a map image centred on the given coordinates.
     *
     * @param lat latitude of the centre of the map.
     * @param lon longitude of the centre of the map.
     * @param zoom zoom level to render at.
     * @param darkMode whether to request the dark map style.
     * @param width width of the image in density independent pixels.
     * @param height height of the image in density independent pixels.
     * @param density screen density, used to request a matching pixel size.
     */
    fun build(
        lat: Double,
        lon: Double,
        zoom: Double,
        darkMode: Boolean,
        width: Int,
        height: Int,
        density: Float,
    ): String

    /** Whether a map provider is configured for this build; the URLs are unusable when it is not. */
    fun isServiceAvailable(): Boolean
}

@Composable
fun rememberStaticMapBuilder(mapTilerConfig: MapTilerConfig?): StaticMapUrlBuilder {
    return remember(mapTilerConfig) {
        if (mapTilerConfig != null) {
            MapTilerStaticMapUrlBuilder(mapTilerConfig)
        } else {
            MapTilerStaticMapUrlBuilder()
        }
    }
}
