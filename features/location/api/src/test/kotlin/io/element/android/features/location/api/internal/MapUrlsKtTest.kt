/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.location.api.internal

import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class MapUrlsKtTest {
    @Test
    fun `static map 1x density`() {
        assertThat(
            staticMapUrl(
                mapId = "aMapId",
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                width = 800,
                height = 600,
                density = 1f,
                apiKey = "anApiKey"
            )
        ).isEqualTo("https://api.maptiler.com/maps/aMapId/static/-4.56,1.23,7.8/800x600.webp?key=anApiKey&attribution=bottomleft")
    }

    @Test
    fun `static map 1,5x density`() {
        assertThat(
            staticMapUrl(
                mapId = "aMapId",
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                width = 1200,
                height = 900,
                density = 1.5f,
                apiKey = "anApiKey"
            )
        ).isEqualTo("https://api.maptiler.com/maps/aMapId/static/-4.56,1.23,7.8/800x600.webp?key=anApiKey&attribution=bottomleft")
    }

    @Test
    fun `static map 2x density`() {
        assertThat(
            staticMapUrl(
                mapId = "aMapId",
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                width = 1600,
                height = 1200,
                density = 2f,
                apiKey = "anApiKey"
            )
        ).isEqualTo("https://api.maptiler.com/maps/aMapId/static/-4.56,1.23,7.8/800x600@2x.webp?key=anApiKey&attribution=bottomleft")
    }

    @Test
    fun `static map 3x density`() {
        assertThat(
            staticMapUrl(
                mapId = "aMapId",
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                width = 2400,
                height = 1800,
                density = 3f,
                apiKey = "anApiKey"
            )
        ).isEqualTo("https://api.maptiler.com/maps/aMapId/static/-4.56,1.23,7.8/800x600@2x.webp?key=anApiKey&attribution=bottomleft")
    }

    @Test
    fun `tile style url`() {
        assertThat(
            tileStyleUrl(
                mapId = "aMapId",
                apiKey = "anApiKey"
            )
        ).isEqualTo("https://api.maptiler.com/maps/aMapId/style.json?key=anApiKey")
    }

    @Test
    fun `too big image is coerced keeping aspect ratio`() {
        assertThat(
            staticMapUrl(
                mapId = "aMapId",
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                width = 4096,
                height = 2048,
                density = 1f,
                apiKey = "anApiKey"
            )
        ).isEqualTo("https://api.maptiler.com/maps/aMapId/static/-4.56,1.23,7.8/2048x1024.webp?key=anApiKey&attribution=bottomleft")

        assertThat(
            staticMapUrl(
                mapId = "aMapId",
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                width = 2048,
                height = 4096,
                density = 1f,
                apiKey = "anApiKey"
            )
        ).isEqualTo("https://api.maptiler.com/maps/aMapId/static/-4.56,1.23,7.8/1024x2048.webp?key=anApiKey&attribution=bottomleft")

        assertThat(
            staticMapUrl(
                mapId = "aMapId",
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                width = 4096,
                height = 2048,
                density = 2f,
                apiKey = "anApiKey"
            )
        ).isEqualTo("https://api.maptiler.com/maps/aMapId/static/-4.56,1.23,7.8/1024x512@2x.webp?key=anApiKey&attribution=bottomleft")

        assertThat(
            staticMapUrl(
                mapId = "aMapId",
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                width = 2048,
                height = 4096,
                density = 2f,
                apiKey = "anApiKey"
            )
        ).isEqualTo("https://api.maptiler.com/maps/aMapId/static/-4.56,1.23,7.8/512x1024@2x.webp?key=anApiKey&attribution=bottomleft")

        assertThat(
            staticMapUrl(
                mapId = "aMapId",
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                width = Int.MAX_VALUE,
                height = Int.MAX_VALUE,
                density = 2f,
                apiKey = "anApiKey"
            )
        ).isEqualTo("https://api.maptiler.com/maps/aMapId/static/-4.56,1.23,7.8/1024x1024@2x.webp?key=anApiKey&attribution=bottomleft")

    }

    @Test
    fun `too small image is coerced to 1x1`() {
        assertThat(
            staticMapUrl(
                mapId = "aMapId",
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                width = 0,
                height = 0,
                density = 1f,
                apiKey = "anApiKey"
            )
        ).isEqualTo("https://api.maptiler.com/maps/aMapId/static/-4.56,1.23,7.8/1x1.webp?key=anApiKey&attribution=bottomleft")

        assertThat(
            staticMapUrl(
                mapId = "aMapId",
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                width = 0,
                height = 0,
                density = 2f,
                apiKey = "anApiKey"
            )
        ).isEqualTo("https://api.maptiler.com/maps/aMapId/static/-4.56,1.23,7.8/1x1@2x.webp?key=anApiKey&attribution=bottomleft")

        assertThat(
            staticMapUrl(
                mapId = "aMapId",
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                width = Int.MIN_VALUE,
                height = Int.MIN_VALUE,
                density = 1f,
                apiKey = "anApiKey"
            )
        ).isEqualTo("https://api.maptiler.com/maps/aMapId/static/-4.56,1.23,7.8/1x1.webp?key=anApiKey&attribution=bottomleft")
    }
}
