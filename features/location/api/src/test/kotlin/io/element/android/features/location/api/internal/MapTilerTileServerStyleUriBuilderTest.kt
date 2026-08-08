/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.api.internal

import com.google.common.truth.Truth.assertThat
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
                customMapStyleUrl = null,
                darkMode = false,
            )
        ).isEqualTo("https://base.url/aLightMapId/style.json?key=anApiKey")
    }

    @Test
    fun `dark map uri`() {
        assertThat(
            builder.build(
                customMapStyleUrl = null,
                darkMode = true,
            )
        ).isEqualTo("https://base.url/aDarkMapId/style.json?key=anApiKey")
    }

    @Test
    fun `custom map uri light`() {
        assertThat(
            builder.build(
                customMapStyleUrl = "https://custom.url/style.json",
                darkMode = false,
            )
        ).isEqualTo("https://custom.url/style.json")
    }

    @Test
    fun `custom map uri dark`() {
        assertThat(
            builder.build(
                customMapStyleUrl = "https://custom.url/style.json",
                darkMode = true,
            )
        ).isEqualTo("https://custom.url/style.json")
    }

    @Test
    fun `custom map uri with its own api key is not altered`() {
        assertThat(
            builder.build(
                customMapStyleUrl = "https://api.maptiler.com/maps/streets-v2/style.json?key=anOperatorKey",
                darkMode = false,
            )
        ).isEqualTo("https://api.maptiler.com/maps/streets-v2/style.json?key=anOperatorKey")
    }

    @Test
    fun `custom map uri with an existing query is not altered`() {
        assertThat(
            builder.build(
                customMapStyleUrl = "https://self.hosted/style.json?foo=bar",
                darkMode = true,
            )
        ).isEqualTo("https://self.hosted/style.json?foo=bar")
    }

    @Test
    fun `blank custom map uri falls back to the built-in style`() {
        assertThat(
            builder.build(
                customMapStyleUrl = "  ",
                darkMode = false,
            )
        ).isEqualTo("https://base.url/aLightMapId/style.json?key=anApiKey")
    }
}
