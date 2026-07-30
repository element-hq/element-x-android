/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.userlocation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.RoomScope
import org.maplibre.compose.location.DesiredAccuracy
import org.maplibre.compose.location.rememberNullLocationProvider
import org.maplibre.spatialk.units.extensions.meters
import kotlin.time.Duration.Companion.seconds

@ContributesBinding(RoomScope::class)
class DefaultUserLocationStateFactory : UserLocationState.Factory {
    @Composable
    override fun create(hasLocationPermission: Boolean): UserLocationState {
        val locationProvider = if (!hasLocationPermission) {
            rememberNullLocationProvider()
        } else {
            @SuppressLint("MissingPermission")
            rememberPlatformLocationProvider(
                updateInterval = 5.seconds,
                desiredAccuracy = DesiredAccuracy.High,
                minDistance = 5.meters,
            )
        }
        val location by locationProvider.location.collectAsState()
        return UserLocationState(location)
    }
}
