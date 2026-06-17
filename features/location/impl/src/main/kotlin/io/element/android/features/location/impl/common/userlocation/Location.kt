/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.userlocation

import io.element.android.features.location.api.Location
import org.maplibre.compose.location.PositionWithAccuracy
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.extensions.meters
import kotlin.time.TimeSource
import org.maplibre.compose.location.Location as MapLibreLocation

fun Location.asMapLibreLocation(): MapLibreLocation {
    return MapLibreLocation(
        position = PositionWithAccuracy(
            value = Position(latitude = lat, longitude = lon),
            accuracy = accuracy?.toDouble()?.meters
        ),
        // Not relevant as not used
        timestamp = TimeSource.Monotonic.markNow(),
    )
}
