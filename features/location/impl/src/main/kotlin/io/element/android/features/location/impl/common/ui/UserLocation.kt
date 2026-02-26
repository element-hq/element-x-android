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
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationPuckColors
import org.maplibre.compose.location.LocationPuckSizes
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.UserLocationState

@Composable
fun UserLocation(
    cameraState: CameraState,
    locationState: UserLocationState,
    trackUserLocation: Boolean,
) {
    LocationTrackingEffect(
        locationState = locationState,
        enabled = trackUserLocation,
    ) {
        cameraState.updateFromLocation()
    }
    val location = locationState.location
    if (location != null) {
        LocationPuck(
            idPrefix = "user-location",
            locationState = locationState,
            cameraState = cameraState,
            accuracyThreshold = Float.POSITIVE_INFINITY,
            showBearingAccuracy = false,
            showBearing = false,
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
}
