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
    private val builder = MapTilerTileServerStyleUriBuilder(
        baseUrl = "https://base.url",
        apiKey = "anApiKey",
        lightMapId = "aLightMapId",
        darkMapId = "aDarkMapId",
    )

    @Test
    fun `light map uri`() {
        assertThat(
            builder.build(
                customMapTilerConfig = null,
                darkMode = false,
            )
        ).isEqualTo("https://base.url/aLightMapId/style.json?key=anApiKey")
    }

    @Test
    fun `dark map uri`() {
        assertThat(
            builder.build(
                customMapTilerConfig = null,
                darkMode = true,
            )
        ).isEqualTo("https://base.url/aDarkMapId/style.json?key=anApiKey")
    }

    @Test
    fun `custom map uri light`() {
        assertThat(
            builder.build(
                customMapTilerConfig = MapTilerConfig(
                    apiKey = "API_KEY",
                    lightStyleId = "light",
                    darkStyleId = "dark",
                    baseUrl = "https://custom.url/style.json"
                ),
                darkMode = false,
            )
        ).isEqualTo("https://custom.url/style.json/light/style.json?key=API_KEY")
    }

    @Test
    fun `custom map uri dark`() {
        assertThat(
            builder.build(
                customMapTilerConfig = MapTilerConfig(
                    apiKey = "API_KEY",
                    lightStyleId = "light",
                    darkStyleId = "dark",
                    baseUrl = "https://custom.url/style.json"
                ),
                darkMode = true,
            )
        ).isEqualTo("https://custom.url/style.json/dark/style.json?key=API_KEY")
    }

    @Test
    fun `blank custom map uri falls back to the built-in style`() {
        assertThat(
            builder.build(
                customMapTilerConfig = MapTilerConfig(
                    apiKey = "API_KEY",
                    lightStyleId = "light",
                    darkStyleId = "dark",
                    baseUrl = "  "
                ),
                darkMode = false,
            )
        ).isEqualTo("https://base.url/light/style.json?key=API_KEY")
    }
}
