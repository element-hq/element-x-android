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
 * Provides a [MapTilerConfig] for the MapLibre maps.
 */
val LocalMapTilerConfig = staticCompositionLocalOf {
    MapTilerConfig(
        apiKey = BuildConfig.MAPTILER_API_KEY,
        lightStyleId = BuildConfig.MAPTILER_LIGHT_MAP_ID,
        darkStyleId = BuildConfig.MAPTILER_DARK_MAP_ID,
        baseUrl = BuildConfig.MAPTILER_BASE_URL
    )
}
