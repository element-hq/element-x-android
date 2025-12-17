/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
