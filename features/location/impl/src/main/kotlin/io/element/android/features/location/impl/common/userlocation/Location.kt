/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.userlocation

import io.element.android.features.location.api.Location
import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.units.extensions.meters
import kotlin.time.Clock

fun Location.toLocationMeasurement(): LocationMeasurement {
    return LocationMeasurement(
        position = Position(
            latitude = lat,
            longitude = lon,
        ),
        horizontalAccuracy = accuracy?.toDouble()?.meters,
        measuredAt = Clock.System.now(),
    )
}
