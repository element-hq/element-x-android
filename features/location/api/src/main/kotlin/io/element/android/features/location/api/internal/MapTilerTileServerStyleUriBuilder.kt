/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

@file:JvmName("TileServerStyleUriBuilderKt")

package io.element.android.features.location.api.internal

import android.content.Context

internal class MapTilerTileServerStyleUriBuilder(
    private val apiKey: String,
    private val lightMapId: String,
    private val darkMapId: String,
) : TileServerStyleUriBuilder {
    constructor(context: Context) : this(
        apiKey = context.apiKey,
        lightMapId = context.mapId(darkMode = false),
        darkMapId = context.mapId(darkMode = true),
    )

    override fun build(darkMode: Boolean): String {
        val mapId = if (darkMode) darkMapId else lightMapId
        return "$MAPTILER_BASE_URL/$mapId/style.json?key=$apiKey"
    }
}
