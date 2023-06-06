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

package io.element.android.features.location.api

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.components.FloatingActionButton
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.features.location.api.R
import io.element.android.features.location.api.internal.buildTileServerUrl
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Composable wrapper around MapLibre's [MapView].
 */
@SuppressLint("MissingPermission")
@Composable
fun MapView(
    modifier: Modifier = Modifier,
    mapState: MapState = rememberMapState(),
    darkMode: Boolean = !ElementTheme.colors.isLight,
    onLocationClick: () -> Unit,
) {
    // When in preview, early return a Box with the received modifier preserving layout
    if (LocalInspectionMode.current) {
        @Suppress("ModifierReused") // False positive, the modifier is not reused due to the early return.
        Box(modifier = modifier)
        return
    }

    val context = LocalContext.current
    val mapView = remember {
        Mapbox.getInstance(context)
        MapView(context)
    }
    var mapRefs by remember { mutableStateOf<MapRefs?>(null) }

    // Build map
    LaunchedEffect(darkMode) {
        mapView.awaitMap().let { map ->
            map.uiSettings.apply {
                isCompassEnabled = false
            }
            map.setStyle(buildTileServerUrl(darkMode = darkMode)) { style ->
                mapRefs = MapRefs(
                    map = map,
                    symbolManager = SymbolManager(mapView, map, style).apply {
                        iconAllowOverlap = true
                    },
                    style = style
                )
            }
        }
    }

    // Update state position when moving map
    DisposableEffect(mapRefs) {
        var listener: MapboxMap.OnCameraIdleListener? = null

        mapRefs?.let { mapRefs ->
            listener = MapboxMap.OnCameraIdleListener {
                mapRefs.map.cameraPosition.target?.let { target ->
                    val position = MapState.CameraPosition(
                        lat = target.latitude,
                        lon = target.longitude,
                        zoom = mapRefs.map.cameraPosition.zoom
                    )
                    mapState.position = position
                    Timber.d("Camera moved to: $position")
                }
            }.apply {
                mapRefs.map.addOnCameraIdleListener(this)
                Timber.d("Added OnCameraIdleListener $this")
            }
        }

        onDispose {
            mapRefs?.let { mapRefs ->
                listener?.let {
                    mapRefs.map.removeOnCameraIdleListener(it).apply {
                        Timber.d("Removed OnCameraIdleListener $it")
                    }
                }
            }
        }
    }

    // Move map to given position when state has changed
    LaunchedEffect(mapRefs, mapState.position) {
        mapRefs?.map?.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(LatLng(mapState.position.lat, mapState.position.lon))
                    .zoom(mapState.position.zoom).build()
            )
        )
        Timber.d("Camera position updated to: ${mapState.position}")
    }

    // Draw pin
    LaunchedEffect(mapRefs, mapState.location) {
        mapRefs?.let { mapRefs ->
            mapState.location?.let { location ->
                context.getDrawable(R.drawable.pin)?.let { mapRefs.style.addImage("pin", it) }
                mapRefs.symbolManager.create(
                    SymbolOptions()
                        .withLatLng(LatLng(location.lat, location.lon))
                        .withIconImage("pin")
                        .withIconSize(1.3f)
                )
                Timber.d("Shown pin at location: $location")
            }
        }

    }

    // Draw markers
    LaunchedEffect(mapRefs, mapState.markers) {
        mapRefs?.let { mapRefs ->
            mapState.markers.forEachIndexed { index, marker ->
                context.getDrawable(marker.drawable)?.let { mapRefs.style.addImage("marker_$index", it) }
                mapRefs.symbolManager.create(
                    SymbolOptions()
                        .withLatLng(LatLng(marker.lat, marker.lon))
                        .withIconImage("marker_$index")
                        .withIconSize(1.0f)
                )
                Timber.d("Shown marker at location: $marker")
            }
        }
    }

    @Suppress("ModifierReused")
    Box(modifier = modifier) {
        AndroidView(factory = { mapView })
        FloatingActionButton(
            onClick = onLocationClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,  // TODO
            )
        }
    }
}

@Composable
fun rememberMapState(
    position: MapState.CameraPosition = MapState.CameraPosition(lat = 0.0, lon = 0.0, zoom = 0.0),
    location: Location? = null,
    markers: ImmutableList<MapState.Marker> = emptyList<MapState.Marker>().toImmutableList(),
): MapState = remember {
    MapState(
        position = position,
        location = location,
        markers = markers,
    )
} // TODO(Use remember saveable with Parcelable custom saver)

@Stable
class MapState(
    position: CameraPosition, // The position of the camera, it's what will be shared
    location: Location? = null, // The location retrieved by the location subsystem, if any.
    markers: ImmutableList<Marker> = emptyList<Marker>().toImmutableList(), // The pin's location, if any.
) {
    var position: CameraPosition by mutableStateOf(position)
    var location: Location? by mutableStateOf(location)
    var markers: ImmutableList<Marker> by mutableStateOf(markers)

    override fun toString(): String {
        return "MapState(position=$position, location=$location, markers=$markers)"
    }

    @Stable
    data class CameraPosition(
        val lat: Double,
        val lon: Double,
        val zoom: Double,
    )

    @Stable
    data class Marker(
        @DrawableRes val drawable: Int,
        val lat: Double,
        val lon: Double,
    )
}

private class MapRefs(
    val map: MapboxMap,
    val symbolManager: SymbolManager,
    val style: Style
)

/**
 * A suspending function that provides an instance of [MapboxMap] from this [MapView]. This is
 * an alternative to [MapView.getMapAsync] by using coroutines to obtain the [MapboxMap].
 *
 * Inspired from [com.google.maps.android.ktx.awaitMap]
 *
 * @return the [MapboxMap] instance
 */
private suspend inline fun MapView.awaitMap(): MapboxMap =
    suspendCoroutine { continuation ->
        getMapAsync {
            continuation.resume(it)
        }
    }

@Preview
@Composable
fun MapViewLightPreview() =
    ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
fun MapViewDarkPreview() =
    ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    MapView(
        modifier = Modifier.size(400.dp),
        onLocationClick = {},
    )
}
