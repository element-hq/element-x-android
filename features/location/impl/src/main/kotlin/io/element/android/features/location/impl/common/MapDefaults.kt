/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common

import android.Manifest
import android.view.Gravity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.maplibre.compose.MapLocationSettings
import io.element.android.libraries.maplibre.compose.MapSymbolManagerSettings
import io.element.android.libraries.maplibre.compose.MapUiSettings
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng

/**
 * Common configuration values for the map.
 */
object MapDefaults {
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

    val centerCameraPosition = CameraPosition.Builder()
        .target(LatLng(49.843, 9.902056))
        .zoom(2.7)
        .build()

    const val DEFAULT_ZOOM = 15.0

    val permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
}
