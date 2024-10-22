/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.maprealtime.impl

import android.Manifest
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import io.element.android.features.location.impl.common.MapDefaults
import io.element.android.features.location.impl.common.actions.LocationActions
import io.element.android.features.maprealtime.impl.common.permissions.PermissionsEvents
import io.element.android.features.maprealtime.impl.common.permissions.PermissionsPresenter
import io.element.android.features.maprealtime.impl.common.permissions.PermissionsState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.room.MatrixRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class MapRealtimePresenterPresenter @Inject constructor(
    permissionsPresenterFactory: PermissionsPresenter.Factory,
    private val locationActions: LocationActions,
    private val room: MatrixRoom,
    private val buildMeta: BuildMeta,
    private val mapTypeStore: MapTypeStore

) : Presenter<MapRealtimePresenterState> {

    private val permissionsPresenter = permissionsPresenterFactory.create(MapDefaults.permissions)

    @Composable
    override fun present(): MapRealtimePresenterState {

        // create a list of map tile providers to choose from
        val mapTypes = listOf(
            MapType("OSM", "openstreetmap"), MapType("Satellite", "satellite"),
            MapType("Streets", "streets-v2"), MapType("TOPO", "topo-v2")
        )
        val permissionsState: PermissionsState = permissionsPresenter.present()

        val appName by remember { derivedStateOf { buildMeta.applicationName } }

        val roomName by remember { derivedStateOf { room.displayName } }

        var showMapTypeDialog: Boolean by remember { mutableStateOf(false) }

        var permissionDialog: MapRealtimePresenterState.Dialog by remember {
            mutableStateOf(MapRealtimePresenterState.Dialog.None)
        }

        val scope = rememberCoroutineScope()

        val mapTile by mapTypeStore.mapTileProviderFlow.collectAsState(initial = "")

        LaunchedEffect(permissionsState.permissions) {
            if (permissionsState.isAnyGranted) {
                permissionDialog = MapRealtimePresenterState.Dialog.None
            }
        }

        fun handleEvents(event: MapRealtimeEvents) {
            when (event) {
                MapRealtimeEvents.CloseMapTypeDialog -> {
                    showMapTypeDialog = false
                }
                MapRealtimeEvents.DismissDialog -> permissionDialog = MapRealtimePresenterState.Dialog.None
                is MapRealtimeEvents.MapLongPress -> {
                    println(event.coords)
                }
                is MapRealtimeEvents.MapTypeSelected -> {
                    scope.launch {
                        setMapTileProvider(event.mapType.mapKey)
                    }
                }
                MapRealtimeEvents.OpenAppSettings -> {
                    locationActions.openSettings()
                    permissionDialog = MapRealtimePresenterState.Dialog.None
                }
                MapRealtimeEvents.OpenMapTypeDialog -> {
                    showMapTypeDialog = true
                }
                MapRealtimeEvents.RequestPermissions -> permissionsState.eventSink(PermissionsEvents.RequestPermissions)
            }
        }

        return MapRealtimePresenterState(
            eventSink = ::handleEvents,
            permissionDialog = permissionDialog,
            hasLocationPermission = permissionsState.isAnyGranted,
            showMapTypeDialog = showMapTypeDialog,
            appName = appName,
            roomName = roomName,
            isSharingLocation = false,
            mapType = mapTypes.find { it.mapKey == mapTile } ?: mapTypes[2],
        )
    }

    private fun CoroutineScope.setMapTileProvider(mapProvider: String) = launch {
        mapTypeStore.setMapTileProvider(mapProvider)
    }
}

/**
 * An effect that request location updates based on the provided request and ensures that the
 * updates are added and removed whenever the composable enters or exists the composition.
 */
@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@Composable
fun LocationUpdatesEffect(
    locationRequest: LocationRequest,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onUpdate: (result: LocationResult) -> Unit,
) {
    val context = LocalContext.current

    // Whenever on of these parameters changes, dispose and restart the effect.
    DisposableEffect(locationRequest, lifecycleOwner) {
        val locationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
//                currentOnUpdate(result)
            }
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                locationClient.requestLocationUpdates(
                    locationRequest, locationCallback, Looper.getMainLooper(),
                )
            } else if (event == Lifecycle.Event.ON_STOP) {
                locationClient.removeLocationUpdates(locationCallback)
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            locationClient.removeLocationUpdates(locationCallback)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

