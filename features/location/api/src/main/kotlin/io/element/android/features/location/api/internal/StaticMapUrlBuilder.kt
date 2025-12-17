/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.api.internal

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

    fun isServiceAvailable(): Boolean
}

fun StaticMapUrlBuilder(): StaticMapUrlBuilder = MapTilerStaticMapUrlBuilder()
