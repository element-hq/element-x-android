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
     * @param customMapTilerConfig the custom MapTiler configuration received from the server, or null if not available.
     * @param darkMode whether to request the dark map style.
     */
    fun build(
        customMapTilerConfig: MapTilerConfig?,
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
    customMapTileConfig: MapTilerConfig?,
): String {
    val darkMode = !ElementTheme.isLightTheme
    return remember(darkMode, customMapTileConfig) {
        MapTilerTileServerStyleUriBuilder().build(
            customMapTilerConfig = customMapTileConfig,
            darkMode = darkMode,
        )
    }
}
