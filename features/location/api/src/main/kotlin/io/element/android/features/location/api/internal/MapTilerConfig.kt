/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.location.api.internal

import android.content.Context
import io.element.android.features.location.api.R

internal const val MAPTILER_BASE_URL = "https://api.maptiler.com/maps"

internal fun Context.mapId(darkMode: Boolean) = when (darkMode) {
    true -> getString(R.string.maptiler_dark_map_id)
    false -> getString(R.string.maptiler_light_map_id)
}

internal val Context.apiKey: String
    get() = getString(R.string.maptiler_api_key)
