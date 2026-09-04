/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.userlocation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.RoomScope
import kotlinx.coroutines.flow.mapNotNull
import org.maplibre.compose.location.LocationAccuracy
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.location.LocationRequest
import org.maplibre.spatialk.units.extensions.meters
import kotlin.time.Duration.Companion.seconds

@ContributesBinding(RoomScope::class)
class DefaultUserLocationStateFactory : UserLocationState.Factory {
    @Composable
    override fun create(hasLocationPermission: Boolean): UserLocationState {
        val locationProvider = if (hasLocationPermission) {
            rememberPlatformLocationProvider()
        } else {
            rememberNotGrantedLocationProvider()
        }
        val locationFlow = remember(locationProvider) {
            val locationRequest = LocationRequest(
                accuracy = LocationAccuracy.High,
                minimumInterval = 5.seconds,
                minimumDistance = 5.meters,
            )
            locationProvider
                .updates(locationRequest)
                .mapNotNull { event ->
                    when (event) {
                        is LocationEvent.Fix -> event.location
                        is LocationEvent.Unavailable -> null
                    }
                }
        }
        val location by locationFlow.collectAsState(null)
        return UserLocationState(location)
    }
}
