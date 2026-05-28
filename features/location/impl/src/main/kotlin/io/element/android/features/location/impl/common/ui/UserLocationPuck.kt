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
import org.maplibre.compose.location.UserLocationState
import org.maplibre.compose.location.rememberAndroidLocationProvider
import org.maplibre.compose.location.rememberNullLocationProvider
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.spatialk.units.extensions.meters
import kotlin.time.Duration.Companion.seconds

@Composable
fun UserLocationPuck(
    cameraState: CameraState,
    locationState: UserLocationState,
    trackUserLocation: Boolean,
) {
    SimpleLocationTrackingEffect(
        locationState = locationState,
        enabled = trackUserLocation,
    ) { currentLocation ->
        val target = currentLocation?.position?.value ?: cameraState.position.target
        val newPosition = cameraState.position.copy(
            target = target,
            // Force pointing to NORTH
            bearing = 0.0,
            zoom = cameraState.position.zoom.coerceAtLeast(MapDefaults.DEFAULT_ZOOM)
        )
        cameraState.animateTo(newPosition)
    }
    val location = locationState.location
    if (location != null) {
        LocationPuck(
            idPrefix = "user-location",
            location = location,
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
            updateInterval = 5.seconds,
            desiredAccuracy = DesiredAccuracy.High,
            minDistance = 5.meters,
        )
    }
    return rememberUserLocationState(locationProvider)
}
