/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:JvmName("TileServerStyleUriBuilderKt")

package io.element.android.features.location.api.internal

import io.element.android.features.enterprise.api.remoteconfig.MapTilerConfig
import io.element.android.features.location.api.BuildConfig

internal class MapTilerTileServerStyleUriBuilder(
    private val baseUrl: String,
    private val apiKey: String,
    private val lightMapId: String,
    private val darkMapId: String,
) : TileServerStyleUriBuilder {
    constructor() : this(
        baseUrl = BuildConfig.MAPTILER_BASE_URL.removeSuffix("/"),
        apiKey = BuildConfig.MAPTILER_API_KEY,
        lightMapId = BuildConfig.MAPTILER_LIGHT_MAP_ID,
        darkMapId = BuildConfig.MAPTILER_DARK_MAP_ID,
    )

    override fun build(
        customMapTilerConfig: MapTilerConfig?,
        darkMode: Boolean,
    ): String {
        val baseUrl = customMapTilerConfig?.baseUrl.takeIf { !it.isNullOrBlank() } ?: baseUrl
        val apiKey = customMapTilerConfig?.apiKey ?: apiKey
        val mapId = if (darkMode) {
            customMapTilerConfig?.darkStyleId ?: darkMapId
        } else {
            customMapTilerConfig?.lightStyleId ?: lightMapId
        }

        return "$baseUrl/$mapId/style.json?key=$apiKey"
    }
}
