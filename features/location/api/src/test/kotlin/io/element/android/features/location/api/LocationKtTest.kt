/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
