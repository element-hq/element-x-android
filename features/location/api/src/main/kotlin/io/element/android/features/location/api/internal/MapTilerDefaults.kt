/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.api.internal

import io.element.android.features.enterprise.api.remoteconfig.MapTilerConfig

/**
 * Public MapTiler defaults, used when a [MapTilerConfig] does not define a value.
 * Values from another configuration (e.g. the build time one) must never be used as a fallback,
 * to avoid sending an API key to a server it was not meant for.
 */
internal object MapTilerDefaults {
    const val BASE_URL = "https://api.maptiler.com/maps"
    const val LIGHT_MAP_ID = "basic-v2"
    const val DARK_MAP_ID = "basic-v2-dark"
}

internal fun MapTilerConfig.baseUrlOrDefault(): String {
    return baseUrl?.takeIf { it.isNotBlank() }?.removeSuffix("/") ?: MapTilerDefaults.BASE_URL
}

internal fun MapTilerConfig.lightMapIdOrDefault(): String {
    return lightStyleId?.takeIf { it.isNotBlank() } ?: MapTilerDefaults.LIGHT_MAP_ID
}

internal fun MapTilerConfig.darkMapIdOrDefault(): String {
    return darkStyleId?.takeIf { it.isNotBlank() } ?: MapTilerDefaults.DARK_MAP_ID
}
