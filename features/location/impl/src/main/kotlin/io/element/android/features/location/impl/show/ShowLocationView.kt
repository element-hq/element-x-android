/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package io.element.android.features.location.impl.show

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.location.impl.common.MapDefaults
import io.element.android.features.location.impl.common.ui.LocationConstraintsDialog
import io.element.android.features.location.impl.common.ui.LocationFloatingActionButton
import io.element.android.features.location.impl.common.ui.LocationPinMarkers
import io.element.android.features.location.impl.common.ui.LocationShareRow
import io.element.android.features.location.impl.common.ui.MapBottomSheetScaffold
import io.element.android.features.location.impl.common.ui.UserLocationPuck
import io.element.android.features.location.impl.common.ui.rememberUserLocationState
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.spatialk.geojson.Position

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowLocationView(
    state: ShowLocationState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LocationConstraintsDialog(
        state = state.dialogState,
        appName = state.appName,
        onRequestPermissions = { state.eventSink(ShowLocationEvent.RequestPermissions) },
        onOpenAppSettings = { state.eventSink(ShowLocationEvent.OpenAppSettings) },
        onOpenLocationSettings = { state.eventSink(ShowLocationEvent.OpenLocationSettings) },
        onDismiss = { state.eventSink(ShowLocationEvent.DismissDialog) },
    )

    val cameraState = rememberCameraState(firstPosition = MapDefaults.defaultCameraPosition)
    var hasAnimatedToFocusedLocation by remember { mutableStateOf(false) }
    LaunchedEffect(state.focusedLocation) {
        if (state.focusedLocation != null && !hasAnimatedToFocusedLocation) {
            hasAnimatedToFocusedLocation = true
            val position = CameraPosition(
                target = Position(latitude = state.focusedLocation.location.lat, longitude = state.focusedLocation.location.lon),
                zoom = MapDefaults.DEFAULT_ZOOM
            )
            cameraState.position = position
        }
    }
    LaunchedEffect(cameraState.isCameraMoving) {
        if (cameraState.moveReason == CameraMoveReason.GESTURE) {
            state.eventSink(ShowLocationEvent.TrackMyLocation(false))
        }
    }

    val userLocationState = rememberUserLocationState(state.hasLocationPermission)
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(SheetValue.Expanded)
    )
    LaunchedEffect(state.isSheetDraggable) {
        if (!state.isSheetDraggable) {
            scaffoldState.bottomSheetState.expand()
        }
    }
    MapBottomSheetScaffold(
        sheetDragHandle = if (state.isSheetDraggable) {
            { BottomSheetDefaults.DragHandle() }
        } else {
            null
        },
        sheetSwipeEnabled = state.isSheetDraggable,
        scaffoldState = scaffoldState,
        cameraState = cameraState,
        modifier = modifier,
        topBar = {
            TopAppBar(
                titleStr = stringResource(CommonStrings.screen_view_location_title),
                navigationIcon = {
                    BackButton(
                        onClick = onBackClick,
                    )
                },
            )
        },
        sheetContent = { sheetPaddings ->
            val coroutineScope = rememberCoroutineScope()
            if (!state.isSheetDraggable) {
                // If sheet is draggable the DragHandle has already some padding
                Spacer(Modifier.height(20.dp))
            }
            if (state.locationShares.isEmpty()) {
                Text(
                    text = stringResource(CommonStrings.screen_live_location_sheet_nobody_sharing),
                    style = ElementTheme.typography.fontBodyLgMedium,
                    color = ElementTheme.colors.textPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp),
                    textAlign = TextAlign.Center,
                )
            } else {
                Text(
                    text = stringResource(CommonStrings.screen_static_location_sheet_title),
                    style = ElementTheme.typography.fontBodyLgMedium,
                    color = ElementTheme.colors.textPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                LazyColumn {
                    items(state.locationShares) { locationShare ->
                        LocationShareRow(
                            item = locationShare,
                            onShareClick = { state.eventSink(ShowLocationEvent.Share(locationShare.location)) },
                            modifier = Modifier.clickable {
                                state.eventSink(ShowLocationEvent.TrackMyLocation(false))
                                val position = CameraPosition(
                                    padding = sheetPaddings,
                                    target = Position(locationShare.location.lon, locationShare.location.lat),
                                    zoom = MapDefaults.DEFAULT_ZOOM
                                )
                                coroutineScope.launch {
                                    cameraState.animateTo(finalPosition = position)
                                }
                            }
                        )
                    }
                }
            }
        },
        mapContent = {
            UserLocationPuck(
                cameraState = cameraState,
                locationState = userLocationState,
                trackUserLocation = state.isTrackMyLocation
            )
            val markers = remember(state.locationShares) {
                state.locationShares.map { it.toMarkerData() }
            }
            LocationPinMarkers(markers)
        },
        overlayContent = {
            LocationFloatingActionButton(
                isMapCenteredOnUser = state.isTrackMyLocation,
                onClick = { state.eventSink(ShowLocationEvent.TrackMyLocation(true)) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(all = 16.dp),
            )
        }
    )
}

@PreviewsDayNight
@Composable
internal fun ShowLocationViewPreview(@PreviewParameter(ShowLocationStateProvider::class) state: ShowLocationState) = ElementPreview {
    ShowLocationView(
        state = state,
        onBackClick = {},
    )
}
