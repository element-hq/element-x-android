/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.location.api.internal

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.element.android.compound.theme.ElementTheme

/**
 * Builds a style URI for a MapLibre compatible tile server.
 *
 * Used for rendering dynamic maps.
 */
interface TileServerStyleUriBuilder {
    fun build(
        darkMode: Boolean,
    ): String
}

fun TileServerStyleUriBuilder(context: Context): TileServerStyleUriBuilder = MapTilerTileServerStyleUriBuilder(context = context)

/**
 * Provides and remembers a style URI for a MapLibre compatible tile server.
 *
 * Used for rendering dynamic maps.
 */
@Composable
fun rememberTileStyleUrl(): String {
    val context = LocalContext.current
    val darkMode = !ElementTheme.isLightTheme
    return remember(darkMode) {
        TileServerStyleUriBuilder(context).build(darkMode)
    }
}
