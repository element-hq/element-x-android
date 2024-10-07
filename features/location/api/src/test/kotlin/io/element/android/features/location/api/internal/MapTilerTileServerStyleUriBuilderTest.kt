/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.location.api.internal

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MapTilerTileServerStyleUriBuilderTest {
    private val builder = MapTilerTileServerStyleUriBuilder(
        apiKey = "anApiKey",
        lightMapId = "aLightMapId",
        darkMapId = "aDarkMapId",
    )

    @Test
    fun `light map uri`() {
        assertThat(
            builder.build(darkMode = false)
        ).isEqualTo("https://api.maptiler.com/maps/aLightMapId/style.json?key=anApiKey")
    }

    @Test
    fun `dark map uri`() {
        assertThat(
            builder.build(darkMode = true)
        ).isEqualTo("https://api.maptiler.com/maps/aDarkMapId/style.json?key=anApiKey")
    }
}
