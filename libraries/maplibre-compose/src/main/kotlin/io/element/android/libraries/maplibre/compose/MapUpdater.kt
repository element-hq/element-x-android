/*
 * Copyright (c) 2023 New Vector Ltd
 * Copyright 2021 Google LLC
 * Copied and adapted from android-maps-compose (https://github.com/googlemaps/android-maps-compose)
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
@file:Suppress("MatchingDeclarationName")
package io.element.android.libraries.maplibre.compose

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener
import com.mapbox.mapboxsdk.location.engine.LocationEngineRequest
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style

private const val LOCATION_REQUEST_INTERVAL = 750L

internal class MapPropertiesNode(
    val map: MapboxMap,
    style: Style,
    context: Context,
    cameraPositionState: CameraPositionState,
) : MapNode {

    init {
        map.locationComponent.activateLocationComponent(
            LocationComponentActivationOptions.Builder(context, style)
                .locationComponentOptions(
                    LocationComponentOptions.builder(context)
                        .pulseEnabled(true)
                        .build()
                )
                .locationEngineRequest(
                    LocationEngineRequest.Builder(LOCATION_REQUEST_INTERVAL)
                        .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                        .setFastestInterval(LOCATION_REQUEST_INTERVAL)
                        .build()
                )
                .build()
        )
        cameraPositionState.setMap(map)
    }

    var cameraPositionState = cameraPositionState
        set(value) {
            if (value == field) return
            field.setMap(null)
            field = value
            value.setMap(map)
        }

    override fun onAttached() {
        map.addOnCameraIdleListener {
            cameraPositionState.isMoving = false
            // setOnCameraMoveListener is only invoked when the camera position
            // is changed via .animate(). To handle updating state when .move()
            // is used, it's necessary to set the camera's position here as well
            cameraPositionState.rawPosition = map.cameraPosition
            // Updating user location on every camera move due to lack of a better location updates API.
            cameraPositionState.location = map.locationComponent.lastKnownLocation
        }
        map.addOnCameraMoveCancelListener {
            cameraPositionState.isMoving = false
        }
        map.addOnCameraMoveStartedListener {
            cameraPositionState.cameraMoveStartedReason = CameraMoveStartedReason.fromInt(it)
            cameraPositionState.isMoving = true
        }
        map.addOnCameraMoveListener {
            cameraPositionState.rawPosition = map.cameraPosition
            // Updating user location on every camera move due to lack of a better location updates API.
            cameraPositionState.location = map.locationComponent.lastKnownLocation
        }
        map.locationComponent.addOnCameraTrackingChangedListener(object : OnCameraTrackingChangedListener {
            override fun onCameraTrackingDismissed() {}

            override fun onCameraTrackingChanged(currentMode: Int) {
                cameraPositionState.rawCameraMode = CameraMode.fromInternal(currentMode)
            }
        })
    }

    override fun onRemoved() {
        cameraPositionState.setMap(null)
    }

    override fun onCleared() {
        cameraPositionState.setMap(null)
    }
}

/**
 * Used to keep the primary map properties up to date. This should never leave the map composition.
 */
@SuppressLint("MissingPermission")
@Suppress("NOTHING_TO_INLINE")
@Composable
internal inline fun MapUpdater(
    cameraPositionState: CameraPositionState,
    mapLocationSettings: MapLocationSettings,
    mapUiSettings: MapUiSettings,
    mapSymbolManagerSettings: MapSymbolManagerSettings,
) {
    val mapApplier = currentComposer.applier as MapApplier
    val map = mapApplier.map
    val style = mapApplier.style
    val symbolManager = mapApplier.symbolManager
    val context = LocalContext.current
    ComposeNode<MapPropertiesNode, MapApplier>(
        factory = {
            MapPropertiesNode(
                map = map,
                style = style,
                context = context,
                cameraPositionState = cameraPositionState,
            )
        },
        update = {
            set(mapLocationSettings.locationEnabled) { map.locationComponent.isLocationComponentEnabled = it }

            set(mapUiSettings.compassEnabled) { map.uiSettings.isCompassEnabled = it }
            set(mapUiSettings.rotationGesturesEnabled) { map.uiSettings.isRotateGesturesEnabled = it }
            set(mapUiSettings.scrollGesturesEnabled) { map.uiSettings.isScrollGesturesEnabled = it }
            set(mapUiSettings.tiltGesturesEnabled) { map.uiSettings.isTiltGesturesEnabled = it }
            set(mapUiSettings.zoomGesturesEnabled) { map.uiSettings.isZoomGesturesEnabled = it }
            set(mapUiSettings.logoGravity) { map.uiSettings.logoGravity = it }
            set(mapUiSettings.attributionGravity) { map.uiSettings.attributionGravity = it }
            set(mapUiSettings.attributionTintColor) { map.uiSettings.setAttributionTintColor(it.toArgb()) }

            set(mapSymbolManagerSettings.iconAllowOverlap) { symbolManager.iconAllowOverlap = it }

            update(cameraPositionState) { this.cameraPositionState = it }
        }
    )
}
