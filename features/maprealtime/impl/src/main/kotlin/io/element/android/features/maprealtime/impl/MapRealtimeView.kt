/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.maprealtime.impl

import android.location.Location
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.LocationSearching
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.location.impl.all.composables.MapToolbar
import io.element.android.features.maprealtime.impl.common.PermissionDeniedDialog
import io.element.android.features.maprealtime.impl.common.PermissionRationaleDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.Style
import org.ramani.compose.CameraPosition
import org.ramani.compose.CompassMargins
import org.ramani.compose.LocationRequestProperties
import org.ramani.compose.LocationStyling
import org.ramani.compose.MapLibre
import org.ramani.compose.UiSettings

@Composable
fun MapRealtimeView(
    state: MapRealtimePresenterState,
    onBackPressed: () -> Unit,
    modifer: Modifier = Modifier,
    onMessagesPressed: () -> Unit,
    onJoinCallClick: () -> Unit,
    isCallOngoing: Boolean
) {

    LaunchedEffect(Unit) {
        state.eventSink(MapRealtimeEvents.RequestPermissions)
    }

    when (state.permissionDialog) {
        MapRealtimePresenterState.Dialog.None -> Unit
        MapRealtimePresenterState.Dialog.PermissionDenied -> PermissionDeniedDialog(
            onContinue = { state.eventSink(MapRealtimeEvents.OpenAppSettings) },
            onDismiss = { state.eventSink(MapRealtimeEvents.DismissDialog) },
            appName = state.appName,
        )
        MapRealtimePresenterState.Dialog.PermissionRationale -> PermissionRationaleDialog(
            onContinue = { state.eventSink(MapRealtimeEvents.RequestPermissions) },
            onDismiss = { state.eventSink(MapRealtimeEvents.DismissDialog) },
            appName = state.appName,
        )
    }

    val cameraPosition = rememberSaveable {
        mutableStateOf(CameraPosition())
    }

    val locationRequestState = rememberSaveable { mutableStateOf<LocationRequestProperties>(LocationRequestProperties(interval = 250L)) }

    val currentUserLocation = rememberSaveable { mutableStateOf(Location(null)) }

//    val styleUrl = rememberSaveable { mutableStateOf("https://demotiles.maplibre.org/style.json") }
//    val styleUrl = rememberSaveable { mutableStateOf(state.styleUrl) }
    val styleBuilder = Style.Builder().fromUri(state.styleUrl)

    val myCompassMargins = CompassMargins(left = 0, top = 850, right = 45)
    val uiSettings = UiSettings(compassMargins = myCompassMargins)

    Box(modifier = Modifier.fillMaxSize()) {

        MapLibre(
            modifier = Modifier
                .fillMaxSize(),
            styleBuilder = styleBuilder,
            cameraPosition = cameraPosition.value,
            locationRequestProperties = locationRequestState.value,
            renderMode = RenderMode.COMPASS,
            userLocation = currentUserLocation,
            onMapLongClick = { latLng ->
                state.eventSink(MapRealtimeEvents.MapLongPress(latLng))
            },
            locationStyling = LocationStyling(
                enablePulse = false,
                accuracyColor = 0xFF2496F9.toInt(),
            ),
            uiSettings = uiSettings,
        ) {
            state.liveLocationShares.map { item ->
                val st = if (state.mapType.mapKey == "satellite") "White" else "Black"
                LocationSymbol(item, st)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp)
                .windowInsetsPadding(WindowInsets.statusBars), // Adds padding on the right side
            verticalArrangement = Arrangement.spacedBy(8.dp), // Space between buttons,
            horizontalAlignment = Alignment.End
        ) {
            MapToolbar(
                onBackPressed = onBackPressed,
                title = state.roomName,
                onMessagesPressed = onMessagesPressed,
                onJoinCallClicked = onJoinCallClick,
                isCallOngoing = isCallOngoing
            )
            LocationButton(
                state = state,
                onClick = {
                    if (state.isSharingLocation) {
                        state.eventSink(MapRealtimeEvents.StopLiveLocationShare)
                    } else {
                        state.eventSink(MapRealtimeEvents.StartLiveLocationShare)
                    }
                }
            )
            RoundedIconButton(icon = Icons.Outlined.Layers, onClick = { state.eventSink(MapRealtimeEvents.OpenMapTypeDialog) })
            RoundedIconButton(icon = Icons.Outlined.LocationSearching, onClick = {
                cameraPosition.value = CameraPosition(cameraPosition.value).apply {
                    this.target = LatLng(
                        currentUserLocation.value.latitude,
                        currentUserLocation.value.longitude
                    )
                    this.zoom = 17.0
                }
            })
        }
        MapTypeBottomSheet(state = state, onTileProviderSelected = { provider ->
            state.eventSink(
                MapRealtimeEvents.MapTypeSelected(
                    provider
                )
            )
        })
    }
}

@Composable
fun LocationButton(
    state: MapRealtimePresenterState,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(48.dp)
    ) {
        if (state.isWaitingForLocation) {
            Canvas(modifier = Modifier.size(48.dp)) {
                drawCircle(color = Color.Green)
            }

            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp,
            )
        } else {
            val (imageVector, backgroundColor) = if (state.isSharingLocation) {
                Icons.Outlined.Stop to Color.Red
            } else {
                Icons.Outlined.PlayArrow to Color.Green
            }

            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(backgroundColor, CircleShape)
            ) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = "Icon",
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
fun RoundedIconButton(icon: ImageVector, onClick: () -> Unit, color: Color = Color.White) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(color, CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Icon",
            tint = Color.Black
        )
    }
}

@PreviewsDayNight
@Composable
internal fun MapRealtimeViewPreview(
    @PreviewParameter(MapRealtimePresenterStateProvider::class) state: MapRealtimePresenterState
) = ElementPreview {
    MapRealtimeView(
        state = state,
        onBackPressed = TODO(),
        modifer = TODO(),
        onMessagesPressed = TODO(),
        onJoinCallClick = TODO(),
        isCallOngoing = false
    )
}

