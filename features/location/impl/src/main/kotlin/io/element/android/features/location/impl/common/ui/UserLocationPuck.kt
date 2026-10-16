/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.ui

import androidx.compose.runtime.Composable
import io.element.android.compound.theme.ElementTheme
import org.maplibre.compose.layers.LocationIndicatorDefaults
import org.maplibre.compose.layers.LocationIndicatorLayer
import org.maplibre.compose.location.LocationMeasurement

@Composable
fun UserLocationPuck(
    location: LocationMeasurement?,
) {
    LocationIndicatorLayer(
        id = "user-location",
        location = location?.position,
        // Hide the accuracy circle and the bearing accuracy sector: leaving `accuracyRadius`
        // and `bearingAccuracy` null is what hides them.
        accuracyRadius = null,
        bearingAccuracy = null,
        // Hide the bearing indicator.
        bearing = null,
        bearingImage = null,
        topImage = LocationIndicatorDefaults.topImage(
            color = ElementTheme.colors.iconAccentPrimary,
            borderColor = ElementTheme.colors.bgCanvasDefault,
        ),
    )
}
