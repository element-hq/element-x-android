/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationPuckColors
import org.maplibre.compose.location.LocationPuckSizes
import org.maplibre.spatialk.units.Length

@Composable
fun UserLocationPuck(
    location: LocationMeasurement?,
) {
    LocationPuck(
        idPrefix = "user-location",
        location = location,
        // Hide the accuracy circle.
        accuracyThreshold = Length.PositiveInfinity,
        // Hide the bearing (and bearing accuracy) indicator.
        bearing = null,
        sizes = LocationPuckSizes(
            dotRadius = 8.dp,
            dotStrokeWidth = 2.dp,
        ),
        colors = LocationPuckColors(
            dotFillColorCurrentLocation = ElementTheme.colors.iconAccentPrimary,
            dotFillColorOldLocation = ElementTheme.colors.iconAccentTertiary,
            dotStrokeColor = ElementTheme.colors.bgCanvasDefault,
        )
    )
}
