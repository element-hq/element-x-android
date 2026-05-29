/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import org.maplibre.compose.location.DesiredAccuracy
import org.maplibre.compose.location.Location
import org.maplibre.compose.location.rememberAndroidLocationProvider
import org.maplibre.compose.location.rememberNullLocationProvider
import org.maplibre.spatialk.units.extensions.meters
import kotlin.time.Duration.Companion.seconds

class UserLocationState(locationState: State<Location?>) {
    val location: Location? by locationState
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
    val locationState = locationProvider.location.collectAsState()
    return remember { UserLocationState(locationState) }
}
