/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.send

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.location.api.Location
import io.element.android.features.location.api.internal.centerBottomEdge
import io.element.android.features.location.api.internal.rememberTileStyleUrl
import io.element.android.features.location.impl.R
import io.element.android.features.location.impl.common.MapDefaults
import io.element.android.features.location.impl.common.PermissionDeniedDialog
import io.element.android.features.location.impl.common.PermissionRationaleDialog
import io.element.android.features.location.impl.common.ui.LocationFloatingActionButton
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.BottomSheetScaffold
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.maplibre.compose.CameraMode
import io.element.android.libraries.maplibre.compose.CameraMoveStartedReason
import io.element.android.libraries.maplibre.compose.MapLibreMap
import io.element.android.libraries.maplibre.compose.rememberCameraPositionState
import io.element.android.libraries.ui.strings.CommonStrings
import org.maplibre.android.camera.CameraPosition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendLocationView(
    state: SendLocationState,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        state.eventSink(SendLocationEvents.RequestPermissions)
    }

    when (state.permissionDialog) {
        SendLocationState.Dialog.None -> Unit
        SendLocationState.Dialog.PermissionDenied -> PermissionDeniedDialog(
            onContinue = { state.eventSink(SendLocationEvents.OpenAppSettings) },
            onDismiss = { state.eventSink(SendLocationEvents.DismissDialog) },
            appName = state.appName,
        )
        SendLocationState.Dialog.PermissionRationale -> PermissionRationaleDialog(
            onContinue = { state.eventSink(SendLocationEvents.RequestPermissions) },
            onDismiss = { state.eventSink(SendLocationEvents.DismissDialog) },
            appName = state.appName,
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = MapDefaults.centerCameraPosition
    }

    LaunchedEffect(state.mode) {
        when (state.mode) {
            SendLocationState.Mode.PinLocation -> {
                cameraPositionState.cameraMode = CameraMode.NONE
            }
            SendLocationState.Mode.SenderLocation -> {
                cameraPositionState.position = CameraPosition.Builder()
                    .zoom(MapDefaults.DEFAULT_ZOOM)
                    .build()
                cameraPositionState.cameraMode = CameraMode.TRACKING
            }
        }
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
            state.eventSink(SendLocationEvents.SwitchToPinLocationMode)
        }
    }

    // BottomSheetScaffold doesn't manage the system insets for sheetContent and the FAB, so we need to do it manually.
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    BottomSheetScaffold(
        sheetContent = {
            Spacer(modifier = Modifier.height(16.dp))
            ListItem(
                headlineContent = {
                    Text(
                        stringResource(
                            when (state.mode) {
                                SendLocationState.Mode.PinLocation -> CommonStrings.screen_share_this_location_action
                                SendLocationState.Mode.SenderLocation -> CommonStrings.screen_share_my_location_action
                            }
                        )
                    )
                },
                modifier = Modifier.clickable(
                    // target is null when the map hasn't loaded (or api key is wrong) so we disable the button
                    enabled = cameraPositionState.position.target != null
                ) {
                    state.eventSink(
                        SendLocationEvents.SendLocation(
                            cameraPosition = SendLocationEvents.SendLocation.CameraPosition(
                                lat = cameraPositionState.position.target!!.latitude,
                                lon = cameraPositionState.position.target!!.longitude,
                                zoom = cameraPositionState.position.zoom,
                            ),
                            location = cameraPositionState.location?.let {
                                Location(
                                    lat = it.latitude,
                                    lon = it.longitude,
                                    accuracy = it.accuracy,
                                )
                            }
                        )
                    )
                    navigateUp()
                },
                leadingContent = {
                    Icon(
                        resourceId = R.drawable.pin_small,
                        contentDescription = null,
                        tint = Color.Unspecified,
                    )
                },
            )
            Spacer(modifier = Modifier.height(16.dp + navBarPadding))
        },
        modifier = modifier,
        scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded),
        ),
        sheetDragHandle = {},
        sheetSwipeEnabled = false,
        topBar = {
            TopAppBar(
                titleStr = stringResource(CommonStrings.screen_share_location_title),
                navigationIcon = {
                    BackButton(onClick = navigateUp)
                },
            )
        },
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .consumeWindowInsets(it),
            contentAlignment = Alignment.Center
        ) {
            MapLibreMap(
                styleUri = rememberTileStyleUrl(),
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapDefaults.uiSettings,
                symbolManagerSettings = MapDefaults.symbolManagerSettings,
                locationSettings = MapDefaults.locationSettings.copy(
                    locationEnabled = state.hasLocationPermission,
                ),
            )
            Icon(
                resourceId = CommonDrawables.pin,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.centerBottomEdge(this),
            )
            LocationFloatingActionButton(
                isMapCenteredOnUser = state.mode == SendLocationState.Mode.SenderLocation,
                onClick = { state.eventSink(SendLocationEvents.SwitchToMyLocationMode) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 18.dp, bottom = 72.dp + navBarPadding),
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun SendLocationViewPreview(
    @PreviewParameter(SendLocationStateProvider::class) state: SendLocationState
) = ElementPreview {
    SendLocationView(
        state = state,
        navigateUp = {},
    )
}
