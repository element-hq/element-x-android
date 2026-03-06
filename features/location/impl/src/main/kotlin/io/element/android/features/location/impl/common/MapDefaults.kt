/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common

import android.Manifest
import androidx.compose.ui.Alignment
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.map.RenderOptions
import org.maplibre.spatialk.geojson.Position

/**
 * Common configuration values for the map.
 */
object MapDefaults {
    val options = MapOptions(
        renderOptions = RenderOptions.Standard,
        gestureOptions = GestureOptions.Standard,
        ornamentOptions = OrnamentOptions(
            isLogoEnabled = true,
            logoAlignment = Alignment.BottomStart,
            isAttributionEnabled = true,
            attributionAlignment = Alignment.BottomEnd,
            isCompassEnabled = false,
            isScaleBarEnabled = false,
        )
    )

    /*
    val uiSettings: MapUiSettings
        @Composable
        @ReadOnlyComposable
        get() = MapUiSettings(
            compassEnabled = false,
            rotationGesturesEnabled = false,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = false,
            zoomGesturesEnabled = true,
            logoGravity = Gravity.TOP,
            attributionGravity = Gravity.TOP,
            attributionTintColor = ElementTheme.colors.iconPrimary
        )

    val symbolManagerSettings: MapSymbolManagerSettings
        get() = MapSymbolManagerSettings(
            iconAllowOverlap = true
        )

    val locationSettings: MapLocationSettings
        get() = MapLocationSettings(
            locationEnabled = false,
            backgroundTintColor = Color.White,
            foregroundTintColor = Color.Black,
            backgroundStaleTintColor = Color.White,
            foregroundStaleTintColor = Color.Black,
            accuracyColor = Color.Black,
            pulseEnabled = true,
            pulseColor = Color.Black,
        )

     */

    val centerCameraPosition = CameraPosition(
        target = Position(49.843, 9.902056),
        zoom = 2.7,
    )
    const val DEFAULT_ZOOM = 15.0

    val permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
}
