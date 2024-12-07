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
import androidx.compose.runtime.ProduceStateScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import io.element.android.features.location.api.Location
import io.element.android.features.location.impl.common.MapDefaults
import io.element.android.features.location.impl.common.actions.LocationActions
import io.element.android.features.maprealtime.impl.common.permissions.PermissionsEvents
import io.element.android.features.maprealtime.impl.common.permissions.PermissionsPresenter
import io.element.android.features.maprealtime.impl.common.permissions.PermissionsState
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.location.LiveLocationShare
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.location.AssetType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    // This is for demo purposes only. This should be replaced with actual vehicle locations.
//    private val _vehicleLocations = MutableStateFlow<List<LiveLocationShare>>(emptyList())
//    val vehicleLocations: StateFlow<List<LiveLocationShare>> = _vehicleLocations

    @Composable
    override fun present(): MapRealtimePresenterState {

        val liveLocationShares by produceState(initialValue = persistentListOf()) {
            observeLocationShares()
        }

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

        var isSharingLocation: Boolean by remember {
            mutableStateOf(false)
        }

        LaunchedEffect(Unit) {
            if (permissionsState.isAnyGranted) {
                permissionDialog = MapRealtimePresenterState.Dialog.None
            }
            // Start fetching GTFS-realtime data
//            scope.launch(Dispatchers.IO) {
//                startFetchingRealtimeLocations()
//            }
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
                is MapRealtimeEvents.SendLongPressLocation -> {
                    scope.launch {
                        sendLocation(event)
                    }
                }
                MapRealtimeEvents.StartLiveLocationShare -> {
                    isSharingLocation = true
                    scope.launch {
                        startLiveLocationShare()
                    }
                }
                MapRealtimeEvents.StopLiveLocationShare -> {
                    isSharingLocation = false
                    scope.launch {
                        stopLiveLocationShare()
                    }
                }
            }
        }

        return MapRealtimePresenterState(
            eventSink = ::handleEvents,
            permissionDialog = permissionDialog,
            hasLocationPermission = permissionsState.isAnyGranted,
            showMapTypeDialog = showMapTypeDialog,
            appName = appName,
            roomName = roomName,
            isSharingLocation = isSharingLocation,
            mapType = mapTypes.find { it.mapKey == mapTile } ?: mapTypes[2],
            liveLocationShares = liveLocationShares
        )
    }

    private fun ProduceStateScope<ImmutableList<LiveLocationShare>>.observeLocationShares() {
        val accumulatedShares = mutableListOf<LiveLocationShare>()

        room.liveLocationShareFlow
            .distinctUntilChanged()
            .onEach { locationShares ->
                // Location share is only ever an array of 1 element
                val newShare = locationShares.firstOrNull()

                if (newShare != null) {
                    val existingShareIndex = accumulatedShares.indexOfFirst { it.userId == newShare.userId }
                    if (existingShareIndex == -1) {
                        accumulatedShares.add(newShare)
                    } else {
                        accumulatedShares[existingShareIndex] = newShare
                    }
                    value = accumulatedShares.toImmutableList()
                }
            }
            .launchIn(this)
    }

//    private suspend fun startFetchingRealtimeLocations() {
//        // Coroutine to periodically fetch and update the vehicle positions
//        while (true) {
//            try {
//                val feedUrl = URL("https://www.fairfaxcounty.gov/gtfsrt/vehicles") // Replace with your GTFS URL
//                val feed = FeedMessage.parseFrom(feedUrl.openStream())
//
//                val locations = feed.entityList.take(20).mapNotNull { entity ->
//                    entity.vehicle?.let { vehicle ->
//                        val geoLocation = Location(
//                            lat = vehicle.position.latitude.toDouble(),
//                            lon = vehicle.position.longitude.toDouble(),
//                            accuracy = 1.0.toFloat() // TODO (tb): fix this to use the actual accuracy
//                        )
//
//                        val stringLocation = geoLocation.toGeoUri()
//
//                        LiveLocationShare(
//                            userId = UserId(busIdToString(vehicle.vehicle.id.toInt())),
//                            lastLocation = LastLocation(
//                                location = io.element.android.libraries.matrix.api.location.Location(
//                                    body = "Location was shared",
//                                    geoUri = stringLocation,
//                                    description = null,
//                                    zoomLevel = 0,
//                                ),
//                                ts = 0u
//                            )
//                        )
//                    }
//                }
//
//                _vehicleLocations.value = locations
//            } catch (e: Exception) {
//                e.printStackTrace() // Catch-all for any other exceptions
//            }
//            delay(5000) // Update interval, e.g., every 5 seconds
//        }
//    }

    fun busIdToString(busId: Int): String {
        // Map the bus ID to a string representation based on a mod and a base character
        val baseChar = 'A'
        val idValue = busId % 26  // Get a value between 0-25
        val letter = baseChar + idValue  // Convert to a letter between 'A' and 'Z'
        busId.toString().takeLast(2)  // Take last 2 digits as a suffix for uniqueness

        return "@$letter:srkt.dev"
    }

    private fun CoroutineScope.setMapTileProvider(mapProvider: String) = launch {
        mapTypeStore.setMapTileProvider(mapProvider)
    }

    private suspend fun sendLocation(event: MapRealtimeEvents.SendLongPressLocation) {
        val location = Location(
            lat = event.coords.latitude,
            lon = event.coords.longitude,
            accuracy = 1.0.toFloat() // TODO (tb): fix this to use the actual accuracy
        )
        val geoUri = location.toGeoUri()
        room.sendLocation(
            body = generateBody(geoUri),
            geoUri = geoUri,
            description = null,
            zoomLevel = MapDefaults.DEFAULT_ZOOM.toInt(),
            assetType = AssetType.SENDER
        )
    }

    private suspend fun startLiveLocationShare() {
        room.startLiveLocationShare((60 * 1000).toULong())
        delay(5000) // pause to test arrival times
//        room.sendUserLocationBeacon("geo:40.6892532,-74.0445482;u=35")
    }

    private suspend fun stopLiveLocationShare() {
        room.stopLiveLocationShare()
    }
}

private fun generateBody(uri: String): String = "Location was shared at $uri"

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

