/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.userlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.HandlerThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.location.LocationListenerCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.location.LocationRequestCompat
import androidx.core.os.ExecutorCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.maplibre.compose.location.LocationAccuracy
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.LocationRequest
import org.maplibre.compose.location.LocationUnavailableReason
import org.maplibre.compose.location.asMapLibreLocation
import org.maplibre.spatialk.units.extensions.inMeters

class PlatformLocationProvider(
    private val context: Context,
) : LocationProvider {

    @SuppressLint("MissingPermission")
    override fun updates(request: LocationRequest): Flow<LocationEvent> = callbackFlow {
        if (!context.hasLocationPermission()) {
            trySend(LocationEvent.Unavailable(LocationUnavailableReason.PermissionDenied))
            close()
            return@callbackFlow
        }
        val locationManager = context.getSystemService(LocationManager::class.java)
        val listener = object : LocationListenerCompat {
            override fun onLocationChanged(location: Location) {
                trySend(LocationEvent.Fix(location.asMapLibreLocation()))
            }

            override fun onProviderDisabled(provider: String) {
                trySend(LocationEvent.Unavailable(LocationUnavailableReason.ServicesDisabled))
            }
        }
        var registered = false

        fun refreshRegistration() {
            if (registered) {
                LocationManagerCompat.removeUpdates(locationManager, listener)
                registered = false
            }
            if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
                trySend(LocationEvent.Unavailable(LocationUnavailableReason.ServicesDisabled))
                return
            }
            val provider = PROVIDERS_BY_PRIORITY.firstOrNull { LocationManagerCompat.hasProvider(locationManager, it) }
            if (provider == null) {
                trySend(LocationEvent.Unavailable(LocationUnavailableReason.Unsupported))
                return
            }
            locationManager.getLastKnownLocation(provider)?.let { location ->
                trySend(LocationEvent.Fix(location.asMapLibreLocation()))
            }
            val locationRequestCompat = LocationRequestCompat.Builder(request.minimumInterval.inWholeMilliseconds)
                .setQuality(request.accuracy.toLocationRequestQuality())
                .setMinUpdateDistanceMeters(request.minimumDistance.inMeters.toFloat())
                .build()
            LocationManagerCompat.requestLocationUpdates(
                locationManager,
                provider,
                locationRequestCompat,
                ExecutorCompat.create(Handler(handlerThread.looper)),
                listener,
            )
            registered = true
        }

        // Re-select and re-register when the user toggles location settings, so updates resume
        // automatically once a usable provider comes back.
        val settingsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                try {
                    refreshRegistration()
                } catch (error: SecurityException) {
                    trySend(LocationEvent.Unavailable(LocationUnavailableReason.PermissionDenied, error))
                    close()
                } catch (error: IllegalArgumentException) {
                    trySend(LocationEvent.Unavailable(LocationUnavailableReason.UnexpectedFailure, error))
                    close()
                }
            }
        }

        try {
            ContextCompat.registerReceiver(
                context,
                settingsReceiver,
                IntentFilter().apply {
                    addAction(LocationManager.MODE_CHANGED_ACTION)
                    addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
                },
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            refreshRegistration()
        } catch (error: SecurityException) {
            trySend(LocationEvent.Unavailable(LocationUnavailableReason.PermissionDenied, error))
            close()
        } catch (error: IllegalArgumentException) {
            trySend(LocationEvent.Unavailable(LocationUnavailableReason.UnexpectedFailure, error))
            close()
        }
        awaitClose {
            runCatching { context.unregisterReceiver(settingsReceiver) }
            LocationManagerCompat.removeUpdates(locationManager, listener)
        }
    }

    private companion object {
        private val PROVIDERS_BY_PRIORITY = listOf(
            LocationManager.FUSED_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
        )
        private val handlerThread by lazy {
            HandlerThread("PlatformLocationProvider").apply { start() }
        }
    }
}

private fun Context.hasLocationPermission(): Boolean =
    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

private fun LocationAccuracy.toLocationRequestQuality(): Int = when (this) {
    LocationAccuracy.BestForNavigation, LocationAccuracy.High -> LocationRequestCompat.QUALITY_HIGH_ACCURACY
    LocationAccuracy.Balanced -> LocationRequestCompat.QUALITY_BALANCED_POWER_ACCURACY
    LocationAccuracy.Low, LocationAccuracy.Lowest -> LocationRequestCompat.QUALITY_LOW_POWER
}

@Composable
fun rememberPlatformLocationProvider(context: Context = LocalContext.current): PlatformLocationProvider {
    return remember(context) {
        PlatformLocationProvider(context = context)
    }
}
