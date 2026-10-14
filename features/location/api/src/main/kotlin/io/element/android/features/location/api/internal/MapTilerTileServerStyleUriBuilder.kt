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

internal class MapTilerTileServerStyleUriBuilder : TileServerStyleUriBuilder {
    override fun build(
        mapTilerConfig: MapTilerConfig,
        darkMode: Boolean,
    ): String {
        val baseUrl = mapTilerConfig.baseUrlOrDefault()
        val mapId = if (darkMode) {
            mapTilerConfig.darkMapIdOrDefault()
        } else {
            mapTilerConfig.lightMapIdOrDefault()
        }
        return "$baseUrl/$mapId/style.json?key=${mapTilerConfig.apiKey}"
    }
}
