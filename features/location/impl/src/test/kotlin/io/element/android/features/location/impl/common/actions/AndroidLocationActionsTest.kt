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

package io.element.android.features.location.impl.common.actions

import com.google.common.truth.Truth.assertThat
import io.element.android.features.location.api.Location
import org.junit.Ignore
import org.junit.Test
import java.net.URLEncoder

@Ignore
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
        val expected = "geo:0,0?q=1.234568,123.456789"

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
}
