/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.maprealtime.impl

import androidx.compose.runtime.Composable
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
import timber.log.Timber
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
        val context = LocalContext.current

        val liveLocationShares by produceState(initialValue = persistentListOf()) {
            observeLocationShares()
        }

        // Observe locations from the service

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
        }

        LaunchedEffect(Unit) {
            LocationForegroundService.locationFlow.collect { location ->
                scope.launch {
                    Timber.e(("SENDING LOCATION" + location?.toGeoUri()))
                    sendLiveLocation(location?.toGeoUri() ?: "")
                }
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

                    LocationForegroundService.start(context)
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
    }

    private suspend fun stopLiveLocationShare() {
        room.stopLiveLocationShare()
    }

    private suspend fun sendLiveLocation(geoUri: String) {
        room.sendLiveLocation(geoUri)
    }
}

private fun generateBody(uri: String): String = "Location was shared at $uri"


