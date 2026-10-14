/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.api

import androidx.compose.runtime.staticCompositionLocalOf
import io.element.android.features.enterprise.api.remoteconfig.MapTilerConfig

/**
 * Provides the [MapTilerConfig] for the MapLibre maps, or null if no map provider is configured.
 */
val LocalMapTilerConfig = staticCompositionLocalOf { buildTimeMapTilerConfig() }

/**
 * The [MapTilerConfig] defined at build time, or null if no MapTiler API key has been provided.
 */
fun buildTimeMapTilerConfig(): MapTilerConfig? {
    // BuildConfig fields are null when not provided at build time.
    val apiKey: String? = BuildConfig.MAPTILER_API_KEY
    if (apiKey.isNullOrEmpty()) return null
    return MapTilerConfig(
        apiKey = apiKey,
        lightStyleId = BuildConfig.MAPTILER_LIGHT_MAP_ID,
        darkStyleId = BuildConfig.MAPTILER_DARK_MAP_ID,
        baseUrl = BuildConfig.MAPTILER_BASE_URL,
    )
}
