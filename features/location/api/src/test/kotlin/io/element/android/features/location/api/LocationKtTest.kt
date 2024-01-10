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

package io.element.android.features.location.api

import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class LocationKtTest {

    @Test
    fun `parseGeoUri - returns null for invalid urls`() {
        assertThat(Location.fromGeoUri("")).isNull()
        assertThat(Location.fromGeoUri("http://example.com/")).isNull()
        assertThat(Location.fromGeoUri("geo:")).isNull()
        assertThat(Location.fromGeoUri("geo:1.234")).isNull()
        assertThat(Location.fromGeoUri("geo:1.234,")).isNull()
        assertThat(Location.fromGeoUri("geo:,1.234")).isNull()
        assertThat(Location.fromGeoUri("notgeo:1.234,5.678")).isNull()
        assertThat(Location.fromGeoUri("geo:+1.234,5.678")).isNull()
        assertThat(Location.fromGeoUri("geo:+1.234,*5.678")).isNull()
        assertThat(Location.fromGeoUri("geo:not,good")).isNull()
        assertThat(Location.fromGeoUri("geo:1.234,5.678;u=wrong")).isNull()
        assertThat(Location.fromGeoUri("geo:1.234,5.678trailing")).isNull()
    }

    @Test
    fun `parseGeoUri - returns location for valid urls`() {
        assertThat(Location.fromGeoUri("geo:1.234,5.678")).isEqualTo(Location(
            lat = 1.234,
            lon = 5.678,
            accuracy = 0f,
        ))

        assertThat(Location.fromGeoUri("geo:1,5")).isEqualTo(Location(
            lat = 1.0,
            lon = 5.0,
            accuracy = 0f,
        ))

        assertThat(Location.fromGeoUri("geo:1.234,5.678;u=3000")).isEqualTo(Location(
            lat = 1.234,
            lon = 5.678,
            accuracy = 3000f,
        ))

        assertThat(Location.fromGeoUri("geo:1,5;u=3000")).isEqualTo(Location(
            lat = 1.0,
            lon = 5.0,
            accuracy = 3000f,
        ))

        assertThat(Location.fromGeoUri("geo:-1.234,-5.678;u=9.10")).isEqualTo(Location(
            lat = -1.234,
            lon = -5.678,
            accuracy = 9.10f,
        ))

        assertThat(Location.fromGeoUri("geo:-1,-5;u=9.10")).isEqualTo(Location(
            lat = -1.0,
            lon = -5.0,
            accuracy = 9.10f,
        ))
    }

    @Test
    fun `encode geoUri - returns geoUri from a Location`() {
        assertThat(Location(1.0, 2.0, 3.0f).toGeoUri())
            .isEqualTo("geo:1.0,2.0;u=3.0")
    }
}
