/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import io.element.android.features.location.impl.common.UserLocationState
import kotlinx.coroutines.flow.distinctUntilChanged
import org.maplibre.compose.location.Location
import kotlin.math.abs

/**
 * Drop-in replacement for the library's LocationTrackingEffect.
 * TODO remove once https://github.com/maplibre/maplibre-compose/issues/808 is fixed
 */
@Composable
internal fun SimpleLocationTrackingEffect(
    locationState: UserLocationState,
    enabled: Boolean = true,
    precision: Double = 0.00001,
    onLocationChange: suspend (Location?) -> Unit,
) {
    val latestOnLocationChange by rememberUpdatedState(onLocationChange)
    LaunchedEffect(locationState, enabled) {
        if (!enabled) return@LaunchedEffect
        val locationStateFlow = snapshotFlow { locationState.location }
        locationStateFlow
            .distinctUntilChanged { oldLocation, newLocation ->
                if (oldLocation != null && newLocation != null) {
                    when {
                        abs(oldLocation.position.value.latitude - newLocation.position.value.latitude) >= precision -> false
                        abs(oldLocation.position.value.longitude - newLocation.position.value.longitude) >= precision -> false
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
