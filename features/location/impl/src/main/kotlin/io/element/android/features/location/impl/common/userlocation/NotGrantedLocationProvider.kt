/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.userlocation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.LocationRequest
import org.maplibre.compose.location.LocationUnavailableReason

class NotGrantedLocationProvider : LocationProvider {
    override fun updates(request: LocationRequest): Flow<LocationEvent> {
        return flowOf(LocationEvent.Unavailable(LocationUnavailableReason.PermissionDenied))
    }
}

@Composable
fun rememberNotGrantedLocationProvider(): NotGrantedLocationProvider {
    return remember { NotGrantedLocationProvider() }
}
