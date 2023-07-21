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

package io.element.android.features.location.impl

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MapTilerStaticMapUrlBuilderTest {

    private val builder = MapTilerStaticMapUrlBuilder()

    @Test
    fun `scaling factor 1`() {
        assertThat(
            builder.build(
                lat = 1.23,
                lon = -4.56,
                zoom = 7.89,
                width = 100,
                height = 100,
                darkMode = false,
                scalingFactor = 1f
            )
        ).isEqualTo(
            "https://api.maptiler.com/maps/basic-v2/static/-4.56,1.23,7.89/100x100.webp?key=&attribution=bottomleft"
        )
    }

    @Test
    fun `scaling factor 1,5`() {
        assertThat(
            builder.build(
                lat = 1.23,
                lon = -4.56,
                zoom = 7.89,
                width = 150,
                height = 150,
                darkMode = false,
                scalingFactor = 1.5f
            )
        ).isEqualTo(
            "https://api.maptiler.com/maps/basic-v2/static/-4.56,1.23,7.89/100x100.webp?key=&attribution=bottomleft"
        )
    }

    @Test
    fun `scaling factor 2`() {
        assertThat(
            builder.build(
                lat = 1.23,
                lon = -4.56,
                zoom = 7.89,
                width = 200,
                height = 200,
                darkMode = false,
                scalingFactor = 2f
            )
        ).isEqualTo(
            "https://api.maptiler.com/maps/basic-v2/static/-4.56,1.23,7.89/100x100@2x.webp?key=&attribution=bottomleft"
        )
    }

    @Test
    fun `density 3`() {
        assertThat(
            builder.build(
                lat = 1.23,
                lon = -4.56,
                zoom = 7.89,
                width = 300,
                height = 300,
                darkMode = false,
                scalingFactor = 3f
            )
        ).isEqualTo(
            "https://api.maptiler.com/maps/basic-v2/static/-4.56,1.23,7.89/100x100@2x.webp?key=&attribution=bottomleft"
        )
    }
}
