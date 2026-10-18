/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.api.internal

import com.google.common.truth.Truth.assertThat
import io.element.android.features.enterprise.api.remoteconfig.MapTilerConfig
import org.junit.Test

class MapTilerTileServerStyleUriBuilderTest {
    private val builder = MapTilerTileServerStyleUriBuilder()

    private val config = MapTilerConfig(
        apiKey = "API_KEY",
        lightStyleId = "light",
        darkStyleId = "dark",
        baseUrl = "https://custom.url/maps/",
    )

    @Test
    fun `light map uri`() {
        assertThat(
            builder.build(
                mapTilerConfig = config,
                darkMode = false,
            )
        ).isEqualTo("https://custom.url/maps/light/style.json?key=API_KEY")
    }

    @Test
    fun `dark map uri`() {
        assertThat(
            builder.build(
                mapTilerConfig = config,
                darkMode = true,
            )
        ).isEqualTo("https://custom.url/maps/dark/style.json?key=API_KEY")
    }

    @Test
    fun `missing values fall back to the public MapTiler defaults`() {
        val partialConfig = MapTilerConfig(
            apiKey = "API_KEY",
            lightStyleId = null,
            darkStyleId = " ",
            baseUrl = "  ",
        )
        assertThat(
            builder.build(
                mapTilerConfig = partialConfig,
                darkMode = false,
            )
        ).isEqualTo("https://api.maptiler.com/maps/basic-v2/style.json?key=API_KEY")
        assertThat(
            builder.build(
                mapTilerConfig = partialConfig,
                darkMode = true,
            )
        ).isEqualTo("https://api.maptiler.com/maps/basic-v2-dark/style.json?key=API_KEY")
    }

    @Test
    fun `blank api key is kept as is`() {
        assertThat(
            builder.build(
                mapTilerConfig = config.copy(apiKey = ""),
                darkMode = false,
            )
        ).isEqualTo("https://custom.url/maps/light/style.json?key=")
    }
}
