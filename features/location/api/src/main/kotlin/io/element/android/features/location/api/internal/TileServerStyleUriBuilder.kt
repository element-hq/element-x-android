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

/**
 * Builds a style URI for a MapLibre compatible tile server.
 *
 * Used for rendering dynamic maps.
 */
interface TileServerStyleUriBuilder {
    fun build(
        customMapStyleUrl: String?,
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
    customMapStyleUrl: String?,
): String {
    val darkMode = !ElementTheme.isLightTheme
    return remember(darkMode, customMapStyleUrl) {
        MapTilerTileServerStyleUriBuilder().build(
            customMapStyleUrl = customMapStyleUrl,
            darkMode = darkMode,
        )
    }
}
