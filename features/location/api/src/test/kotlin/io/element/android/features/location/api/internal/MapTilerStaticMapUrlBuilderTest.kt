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

class MapTilerStaticMapUrlBuilderTest {
    private val builder = MapTilerStaticMapUrlBuilder(
        baseUrl = "https://base.url",
        apiKey = "anApiKey",
        lightMapId = "aLightMapId",
        darkMapId = "aDarkMapId",
    )

    @Test
    fun `isServiceAvailable returns true if api key is not empty`() {
        assertThat(builder.isServiceAvailable()).isTrue()
    }

    @Test
    fun `isServiceAvailable returns false if api key is empty`() {
        val builderWithoutKey = MapTilerStaticMapUrlBuilder(
            baseUrl = "https://base.url",
            apiKey = "",
            lightMapId = "aLightMapId",
            darkMapId = "aDarkMapId",
        )
        assertThat(builderWithoutKey.isServiceAvailable()).isFalse()
    }

    @Test
    fun `static map 1x density`() {
        assertThat(
            builder.build(
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                darkMode = false,
                width = 800,
                height = 600,
                density = 1f,
            )
        ).isEqualTo("https://base.url/aLightMapId/static/-4.56,1.23,7.8/800x600.webp?key=anApiKey&attribution=bottomleft")
    }

    @Test
    fun `static map 1,5x density`() {
        assertThat(
            builder.build(
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                darkMode = false,
                width = 1200,
                height = 900,
                density = 1.5f,
            )
        ).isEqualTo("https://base.url/aLightMapId/static/-4.56,1.23,7.8/800x600.webp?key=anApiKey&attribution=bottomleft")
    }

    @Test
    fun `static map 2x density`() {
        assertThat(
            builder.build(
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                darkMode = false,
                width = 1600,
                height = 1200,
                density = 2f,
            )
        ).isEqualTo("https://base.url/aLightMapId/static/-4.56,1.23,7.8/800x600@2x.webp?key=anApiKey&attribution=bottomleft")
    }

    @Test
    fun `static map 3x density`() {
        assertThat(
            builder.build(
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                darkMode = false,
                width = 2400,
                height = 1800,
                density = 3f,
            )
        ).isEqualTo("https://base.url/aLightMapId/static/-4.56,1.23,7.8/800x600@2x.webp?key=anApiKey&attribution=bottomleft")
    }

    @Test
    fun `too big image is coerced keeping aspect ratio`() {
        assertThat(
            builder.build(
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                darkMode = false,
                width = 4096,
                height = 2048,
                density = 1f,
            )
        ).isEqualTo("https://base.url/aLightMapId/static/-4.56,1.23,7.8/2048x1024.webp?key=anApiKey&attribution=bottomleft")

        assertThat(
            builder.build(
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                darkMode = false,
                width = 2048,
                height = 4096,
                density = 1f,
            )
        ).isEqualTo("https://base.url/aLightMapId/static/-4.56,1.23,7.8/1024x2048.webp?key=anApiKey&attribution=bottomleft")

        assertThat(
            builder.build(
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                darkMode = false,
                width = 4096,
                height = 2048,
                density = 2f,
            )
        ).isEqualTo("https://base.url/aLightMapId/static/-4.56,1.23,7.8/1024x512@2x.webp?key=anApiKey&attribution=bottomleft")

        assertThat(
            builder.build(
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                darkMode = false,
                width = 2048,
                height = 4096,
                density = 2f,
            )
        ).isEqualTo("https://base.url/aLightMapId/static/-4.56,1.23,7.8/512x1024@2x.webp?key=anApiKey&attribution=bottomleft")

        assertThat(
            builder.build(
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                darkMode = false,
                width = Int.MAX_VALUE,
                height = Int.MAX_VALUE,
                density = 2f,
            )
        ).isEqualTo("https://base.url/aLightMapId/static/-4.56,1.23,7.8/1024x1024@2x.webp?key=anApiKey&attribution=bottomleft")
    }

    @Test
    fun `too small image is coerced to 0x0`() {
        assertThat(
            builder.build(
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                darkMode = false,
                width = 0,
                height = 0,
                density = 1f,
            )
        ).isEqualTo("https://base.url/aLightMapId/static/-4.56,1.23,7.8/0x0.webp?key=anApiKey&attribution=bottomleft")

        assertThat(
            builder.build(
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                darkMode = false,
                width = 0,
                height = 0,
                density = 2f,
            )
        ).isEqualTo("https://base.url/aLightMapId/static/-4.56,1.23,7.8/0x0@2x.webp?key=anApiKey&attribution=bottomleft")

        assertThat(
            builder.build(
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                darkMode = false,
                width = Int.MIN_VALUE,
                height = Int.MIN_VALUE,
                density = 1f,
            )
        ).isEqualTo("https://base.url/aLightMapId/static/-4.56,1.23,7.8/0x0.webp?key=anApiKey&attribution=bottomleft")
    }
}
