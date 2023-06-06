/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.location.impl

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.annotation.RequiresPermission
import androidx.core.content.getSystemService
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.features.location.api.Location
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Returns a cold [Flow] that, once collected, emits [Location] updates every second.
 */
@RequiresPermission(
    anyOf = [
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    ]
)
fun locationUpdatesFlow(
    context: Context,
    coroutineDispatchers: CoroutineDispatchers,
): Flow<Location> = callbackFlow {
    val locationManager: LocationManager = checkNotNull(context.getSystemService())
    val provider = locationManager.bestAvailableProvider()
    // Try to eagerly emit the last known location as fast as possible
    locationManager.getLastKnownLocation(provider)?.let { location ->
        trySendBlocking(
            Location(
                lat = location.latitude,
                lon = location.longitude,
                accuracy = location.accuracy
            )
        )
    }
    val locationListener = LocationListenerCompat { location ->
        trySendBlocking(
            Location(
                lat = location.latitude,
                lon = location.longitude,
                accuracy = location.accuracy
            )
        )
    }
    LocationManagerCompat.requestLocationUpdates(
        locationManager,
        provider,
        buildLocationRequest(),
        coroutineDispatchers.io.asExecutor(),
        locationListener,
    )
    awaitClose {
        LocationManagerCompat.removeUpdates(locationManager, locationListener)
    }
}

private fun LocationManager.bestAvailableProvider(): String =
    checkNotNull(getProviders(true).maxByOrNull { providerPriority(it) }) {
        "No location provider available"
    }

private fun providerPriority(provider: String): Int = when (provider) {
    LocationManager.FUSED_PROVIDER -> 4
    LocationManager.GPS_PROVIDER -> 3
    LocationManager.NETWORK_PROVIDER -> 2
    LocationManager.PASSIVE_PROVIDER -> 1
    else -> 0
}

private fun buildLocationRequest() = LocationRequestCompat.Builder(1_000).apply {
    setMinUpdateIntervalMillis(1_000)
}.build()
