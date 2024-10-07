/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.location.api.internal

import android.content.Context

/**
 * Builds an URL for a 3rd party service provider static maps API.
 */
interface StaticMapUrlBuilder {
    fun build(
        lat: Double,
        lon: Double,
        zoom: Double,
        darkMode: Boolean,
        width: Int,
        height: Int,
        density: Float,
    ): String
}

fun StaticMapUrlBuilder(context: Context): StaticMapUrlBuilder = MapTilerStaticMapUrlBuilder(context = context)
