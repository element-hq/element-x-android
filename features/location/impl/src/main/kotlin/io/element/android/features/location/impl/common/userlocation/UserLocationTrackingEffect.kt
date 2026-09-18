/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.userlocation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import io.element.android.features.location.impl.common.MapDefaults
import kotlinx.coroutines.flow.distinctUntilChanged
import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.compose.map.MapState
import kotlin.math.abs

/**
 * Drop-in replacement for the library's LocationTrackingEffect.
 * TODO remove once https://github.com/maplibre/maplibre-compose/issues/808 is fixed
 */
@Composable
internal fun UserLocationTrackingEffect(
    locationState: UserLocationState,
    enabled: Boolean = true,
    precision: Double = 0.00001,
    onLocationChange: suspend (LocationMeasurement?) -> Unit,
) {
    val latestOnLocationChange by rememberUpdatedState(onLocationChange)
    val latestLocationState by rememberUpdatedState(locationState)
    LaunchedEffect(enabled) {
        if (!enabled) return@LaunchedEffect
        val locationStateFlow = snapshotFlow { latestLocationState.location }
        locationStateFlow
            .distinctUntilChanged { oldLocation, newLocation ->
                if (oldLocation != null && newLocation != null) {
                    when {
                        abs(oldLocation.position.latitude - newLocation.position.latitude) >= precision -> false
                        abs(oldLocation.position.longitude - newLocation.position.longitude) >= precision -> false
                        else -> true
                    }
                } else {
                    false
                }
            }
            .collect { location ->
                latestOnLocationChange(location)
            }
    }
}

@Composable
internal fun UserLocationTrackingEffect(
    mapState: MapState,
    locationState: UserLocationState,
    enabled: Boolean = true,
    precision: Double = 0.00001,
) {
    // Tracks whether the camera has already been placed on the user location once.
    var hasPositioned by remember(mapState) { mutableStateOf(false) }
    UserLocationTrackingEffect(
        locationState = locationState,
        enabled = enabled,
        precision = precision
    ) { location ->
        val target = location?.position ?: return@UserLocationTrackingEffect
        val position = mapState.cameraPosition.copy(
            target = target,
            // Force pointing to NORTH
            bearing = 0.0,
            zoom = mapState.cameraPosition.zoom.coerceAtLeast(MapDefaults.DEFAULT_ZOOM)
        )
        if (hasPositioned) {
            mapState.animateCameraPosition(position)
        } else {
            mapState.setCameraPosition(position)
            hasPositioned = true
        }
    }
}
