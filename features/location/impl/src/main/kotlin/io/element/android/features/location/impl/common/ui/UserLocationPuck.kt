/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.ui

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.location.impl.common.MapDefaults
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.location.DesiredAccuracy
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationPuckColors
import org.maplibre.compose.location.LocationPuckSizes
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.UserLocationState
import org.maplibre.compose.location.rememberAndroidLocationProvider
import org.maplibre.compose.location.rememberNullLocationProvider
import org.maplibre.compose.location.rememberUserLocationState
import kotlin.time.Duration.Companion.minutes

@Composable
fun UserLocationPuck(
    cameraState: CameraState,
    locationState: UserLocationState,
    trackUserLocation: Boolean,
) {
    LocationTrackingEffect(
        locationState = locationState,
        enabled = trackUserLocation,
    ) {
        val finalPosition = cameraState.position.copy(
            target = currentLocation.position,
            bearing = currentLocation.bearing ?: cameraState.position.bearing,
            zoom = cameraState.position.zoom.coerceAtLeast(MapDefaults.DEFAULT_ZOOM)
        )
        cameraState.animateTo(finalPosition)
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

@SuppressLint("MissingPermission")
@Composable
fun rememberUserLocationState(hasLocationPermission: Boolean): UserLocationState {
    val isPreview = LocalInspectionMode.current
    val locationProvider = if (isPreview || !hasLocationPermission) {
        rememberNullLocationProvider()
    } else {
        rememberAndroidLocationProvider(
            updateInterval = 1.minutes,
            desiredAccuracy = DesiredAccuracy.Balanced,
            minDistanceMeters = 50f,
        )
    }
    return rememberUserLocationState(locationProvider)
}
