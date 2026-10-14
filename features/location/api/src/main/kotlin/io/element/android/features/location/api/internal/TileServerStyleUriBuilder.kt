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
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.enterprise.api.remoteconfig.MapTilerConfig

/**
 * Builds a style URI for a MapLibre compatible tile server.
 *
 * Used for rendering dynamic maps.
 */
interface TileServerStyleUriBuilder {
    /**
     * Builds the style URI the map view loads its tiles from.
     *
     * @param mapTilerConfig the MapTiler configuration to use.
     * @param darkMode whether to request the dark map style.
     */
    fun build(
        mapTilerConfig: MapTilerConfig,
        darkMode: Boolean,
    ): String
}

/**
 * Provides and remembers a style URI for a MapLibre compatible tile server.
 *
 * Used for rendering dynamic maps.
 */
@Composable
fun rememberTileStyleUrl(
    mapTilerConfig: MapTilerConfig?,
): String {
    val darkMode = !ElementTheme.isLightTheme
    return remember(darkMode, mapTilerConfig) {
        // Without config, the map is not displayed, so the style URL is not used.
        mapTilerConfig?.let {
            MapTilerTileServerStyleUriBuilder().build(
                mapTilerConfig = it,
                darkMode = darkMode,
            )
        }.orEmpty()
    }
}
