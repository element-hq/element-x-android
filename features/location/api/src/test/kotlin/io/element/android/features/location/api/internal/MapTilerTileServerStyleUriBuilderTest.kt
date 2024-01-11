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
