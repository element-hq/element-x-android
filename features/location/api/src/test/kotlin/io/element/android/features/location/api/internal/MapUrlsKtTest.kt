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
    fun `static map non retina`() {
        assertThat(
            staticMapUrl(
                mapId = "aMapId",
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                width = 800,
                height = 600,
                retina = false,
                apiKey = "anApiKey"
            )
        ).isEqualTo("https://api.maptiler.com/maps/aMapId/static/-4.56,1.23,7.8/800x600.webp?key=anApiKey&attribution=bottomleft")
    }

    @Test
    fun `static map retina`() {
        assertThat(
            staticMapUrl(
                mapId = "aMapId",
                lat = 1.23,
                lon = -4.56,
                zoom = 7.8,
                width = 800,
                height = 600,
                retina = true,
                apiKey = "anApiKey"
            )
        ).isEqualTo("https://api.maptiler.com/maps/aMapId/static/-4.56,1.23,7.8/400x300@2x.webp?key=anApiKey&attribution=bottomleft")
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
}
