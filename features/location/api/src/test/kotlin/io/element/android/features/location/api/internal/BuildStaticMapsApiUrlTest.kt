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
import io.element.android.features.location.api.internal.buildStaticMapsApiUrl
import org.junit.Test

class BuildStaticMapsApiUrlTest {
    @Test
    fun `buildStaticMapsApiUrl builds light mode url`() {
        assertThat(
            buildStaticMapsApiUrl(
                lat = 1.234,
                lon = 5.678,
                desiredZoom = 1.2,
                desiredWidth = 100,
                desiredHeight = 200,
                darkMode = false
            )
        ).isEqualTo(
            "https://api.maptiler.com/maps/9bc819c8-e627-474a-a348-ec144fe3d810/static/5.678,1.234,1.2/100x200.webp?key=fU3vlMsMn4Jb6dnEIFsx"
        )
    }

    @Test
    fun `buildStaticMapsApiUrl builds dark mode url`() {
        assertThat(
            buildStaticMapsApiUrl(
                lat = 1.234,
                lon = 5.678,
                desiredZoom = 1.2,
                desiredWidth = 100,
                desiredHeight = 200,
                darkMode = true
            )
        ).isEqualTo(
            "https://api.maptiler.com/maps/dea61faf-292b-4774-9660-58fcef89a7f3/static/5.678,1.234,1.2/100x200.webp?key=fU3vlMsMn4Jb6dnEIFsx"
        )
    }

    @Test
    fun `buildStaticMapsApiUrl coerces zoom at 22 and width and height at max 2048 keeping aspect ratio`() {
        assertThat(
            buildStaticMapsApiUrl(
                lat = 1.234,
                lon = 5.678,
                desiredZoom = 100.0,
                desiredWidth = 8192,
                desiredHeight = 4096,
                darkMode = false
            )
        ).isEqualTo(
            "https://api.maptiler.com/maps/9bc819c8-e627-474a-a348-ec144fe3d810/static/5.678,1.234,22.0/2048x1024.webp?key=fU3vlMsMn4Jb6dnEIFsx"
        )
    }
}
