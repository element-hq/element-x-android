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
            builder.build(darkMode = false)
        ).isEqualTo("https://base.url/aLightMapId/style.json?key=anApiKey")
    }

    @Test
    fun `dark map uri`() {
        assertThat(
            builder.build(darkMode = true)
        ).isEqualTo("https://base.url/aDarkMapId/style.json?key=anApiKey")
    }
}
