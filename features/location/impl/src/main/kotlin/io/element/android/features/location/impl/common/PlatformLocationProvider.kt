/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Handler
import android.os.HandlerThread
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import androidx.core.os.ExecutorCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import org.maplibre.compose.location.DesiredAccuracy
import org.maplibre.compose.location.Location
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.PermissionException
import org.maplibre.compose.location.asMapLibreLocation
import org.maplibre.spatialk.units.Length
import org.maplibre.spatialk.units.extensions.inMeters
import kotlin.time.Duration

@SuppressLint("InlinedApi")
class PlatformLocationProvider(
    context: Context,
    private val updateInterval: Duration,
    private val minDistance: Length,
    private val desiredAccuracy: DesiredAccuracy = DesiredAccuracy.High,
    coroutineScope: CoroutineScope,
    sharingStarted: SharingStarted = SharingStarted.WhileSubscribed(stopTimeoutMillis = 1000),
) : LocationProvider {
    override val location: StateFlow<Location?>

    init {
        if (!handlerThread.isAlive) handlerThread.start()
        if (
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            throw PermissionException()
        }
        val locationManager = context.getSystemService(LocationManager::class.java)
        val provider = PROVIDERS_BY_PRIORITY.firstOrNull { LocationManagerCompat.hasProvider(locationManager, it) }
        val locationFlow = if (provider != null) {
            createProviderFlow(locationManager, provider)
        } else {
            emptyFlow()
        }
        location = locationFlow.stateIn(coroutineScope, sharingStarted, null)
    }

    @RequiresPermission(
        anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]
    )
    private fun createProviderFlow(locationManager: LocationManager, provider: String) = callbackFlow {
        send(locationManager.getLastKnownLocation(provider)?.asMapLibreLocation())
        val listener = LocationListenerCompat { trySend(it.asMapLibreLocation()) }
        val request = LocationRequestCompat.Builder(updateInterval.inWholeMilliseconds)
            .setQuality(desiredAccuracy.toLocationRequestQuality())
            .setMinUpdateDistanceMeters(minDistance.inMeters.toFloat())
            .build()
        LocationManagerCompat.requestLocationUpdates(
            locationManager,
            provider,
            request,
            ExecutorCompat.create(Handler(handlerThread.looper)),
            listener,
        )
        awaitClose { LocationManagerCompat.removeUpdates(locationManager, listener) }
    }

    private companion object {
        private val PROVIDERS_BY_PRIORITY = listOf(
            LocationManager.FUSED_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
        )
        private val handlerThread by lazy { HandlerThread("PlatformLocationProvider") }
    }
}

private fun DesiredAccuracy.toLocationRequestQuality(): Int = when (this) {
    DesiredAccuracy.Highest, DesiredAccuracy.High -> LocationRequestCompat.QUALITY_HIGH_ACCURACY
    DesiredAccuracy.Balanced -> LocationRequestCompat.QUALITY_BALANCED_POWER_ACCURACY
    DesiredAccuracy.Low, DesiredAccuracy.Lowest -> LocationRequestCompat.QUALITY_LOW_POWER
}

@Composable
@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]
)
fun rememberPlatformLocationProvider(
    updateInterval: Duration,
    minDistance: Length,
    desiredAccuracy: DesiredAccuracy = DesiredAccuracy.High,
    context: Context = LocalContext.current,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    sharingStarted: SharingStarted = SharingStarted.WhileSubscribed(stopTimeoutMillis = 1000),
): PlatformLocationProvider {
    return remember(context, updateInterval, minDistance, desiredAccuracy, coroutineScope, sharingStarted) {
        PlatformLocationProvider(
            context = context,
            updateInterval = updateInterval,
            minDistance = minDistance,
            desiredAccuracy = desiredAccuracy,
            coroutineScope = coroutineScope,
            sharingStarted = sharingStarted,
        )
    }
}
