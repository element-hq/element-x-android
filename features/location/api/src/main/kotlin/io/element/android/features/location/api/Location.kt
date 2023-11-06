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

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

private const val GEO_URI_REGEX = """geo:(?<latitude>-?\d+(?:\.\d+)?),(?<longitude>-?\d+(?:\.\d+)?)(?:;u=(?<uncertainty>\d+(?:\.\d+)?))?"""

@SuppressLint("NewApi")
@Parcelize
data class Location(
    val lat: Double,
    val lon: Double,
    val accuracy: Float,
) : Parcelable {
    companion object {
        fun fromGeoUri(geoUri: String): Location? {
            val result = Regex(GEO_URI_REGEX).matchEntire(geoUri) ?: return null
            return Location(
                lat = result.groups["latitude"]?.value?.toDoubleOrNull() ?: return null,
                lon = result.groups["longitude"]?.value?.toDoubleOrNull() ?: return null,
                accuracy = result.groups["uncertainty"]?.value?.toFloatOrNull() ?: 0f,
            )
        }
    }

    fun toGeoUri(): String {
        return "geo:$lat,$lon;u=$accuracy"
    }
}
