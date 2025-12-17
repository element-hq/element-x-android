/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.actions

import com.google.common.truth.Truth.assertThat
import io.element.android.features.location.api.Location
import org.junit.Test
import java.net.URLEncoder
import java.util.Locale

internal class AndroidLocationActionsTest {
    // We use an Android-native encoder in the actual app, switch to an equivalent JVM one for the tests
    private fun urlEncoder(input: String) = URLEncoder.encode(input, "US-ASCII")

    @Test
    fun `buildUrl - truncates excessive decimals to 6dp`() {
        val location = Location(
            lat = 1.234567890123,
            lon = 123.456789012345,
            accuracy = 0f
        )

        val actual = buildUrl(location, null, ::urlEncoder)
        val expected = "geo:0,0?q=1.234568,123.456789 ()"

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `buildUrl - appends label if set`() {
        val location = Location(
            lat = 1.000001,
            lon = 2.000001,
            accuracy = 0f
        )

        val actual = buildUrl(location, "point", ::urlEncoder)
        val expected = "geo:0,0?q=1.000001,2.000001 (point)"

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `buildUrl - URL encodes label`() {
        val location = Location(
            lat = 1.000001,
            lon = 2.000001,
            accuracy = 0f
        )

        val actual = buildUrl(location, "(weird/stuff here)", ::urlEncoder)
        val expected = "geo:0,0?q=1.000001,2.000001 (%28weird%2Fstuff+here%29)"

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `buildUrl - URL encodes coordinates in locale with comma decimal separator`() {
        val location = Location(
            lat = 1.000001,
            lon = 2.000001,
            accuracy = 0f
        )
        // Set a locale with comma as decimal separator
        @Suppress("DEPRECATION")
        Locale.setDefault(Locale.Category.FORMAT, Locale("pt", "BR"))

        val actual = buildUrl(location, "(weird/stuff here)", ::urlEncoder)
        val expected = "geo:0,0?q=1.000001,2.000001 (%28weird%2Fstuff+here%29)"

        assertThat(actual).isEqualTo(expected)
    }
}
