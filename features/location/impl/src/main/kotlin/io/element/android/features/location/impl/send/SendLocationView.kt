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

package io.element.android.features.location.impl.send

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mapbox.mapboxsdk.camera.CameraPosition
import io.element.android.features.location.api.Location
import io.element.android.features.location.api.internal.rememberTileStyleUrl
import io.element.android.features.location.impl.MapDefaults
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.DayNightPreviews
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.BottomSheetScaffold
import io.element.android.libraries.designsystem.theme.components.FloatingActionButton
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.maplibre.compose.CameraMode
import io.element.android.libraries.maplibre.compose.CameraMoveStartedReason
import io.element.android.libraries.maplibre.compose.MapboxMap
import io.element.android.libraries.maplibre.compose.rememberCameraPositionState
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.designsystem.R as DesignSystemR

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SendLocationView(
    state: SendLocationState,
    modifier: Modifier = Modifier,
    navigateUp: () -> Unit = {},
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
                modifier = Modifier.clickable {
                    state.eventSink(
                        SendLocationEvents.SendLocation(
                            cameraPosition = SendLocationEvents.SendLocation.CameraPosition(
                                lat = cameraPositionState.position.target!!.latitude,
                                lon = cameraPositionState.position.target!!.longitude,
                                zoom = cameraPositionState.position.zoom,
                            ),
                            cameraPositionState.location?.let {
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
                    Icon(Icons.Default.LocationOn, null)
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
                title = {
                    Text(
                        text = stringResource(CommonStrings.screen_share_location_title),
                        style = ElementTheme.typography.aliasScreenTitle,
                    )
                },
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
            MapboxMap(
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
                resourceId = DesignSystemR.drawable.pin,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.align { size, space, _ ->
                    // Center bottom edge of pin (i.e. its arrow) to center of screen
                    IntOffset(
                        x = (space.width - size.width) / 2,
                        y = (space.height / 2) - size.height,
                    )
                }
            )
            FloatingActionButton(
                onClick = { state.eventSink(SendLocationEvents.SwitchToMyLocationMode) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 72.dp + navBarPadding),
            ) {
                when (state.mode) {
                    SendLocationState.Mode.PinLocation -> Icon(imageVector = Icons.Default.LocationSearching, contentDescription = null)
                    SendLocationState.Mode.SenderLocation -> Icon(imageVector = Icons.Default.MyLocation, contentDescription = null)
                }
            }
        }
    }
}

@DayNightPreviews
@Composable
fun SendLocationViewPreview(
    @PreviewParameter(SendLocationStateProvider::class) state: SendLocationState
) = ElementPreview {
    SendLocationView(
        state = state,
        navigateUp = {},
    )
}

@Composable
private fun PermissionRationaleDialog(
    onContinue: () -> Unit,
    onDismiss: () -> Unit,
    appName: String,
) {
    ConfirmationDialog(
        content = stringResource(CommonStrings.error_missing_location_rationale_android, appName),
        onSubmitClicked = onContinue,
        onDismiss = onDismiss,
        submitText = stringResource(CommonStrings.action_continue),
        cancelText = stringResource(CommonStrings.action_cancel),
    )
}

@Composable
private fun PermissionDeniedDialog(
    onContinue: () -> Unit,
    onDismiss: () -> Unit,
    appName: String,
) {
    ConfirmationDialog(
        content = stringResource(CommonStrings.error_missing_location_auth_android, appName),
        onSubmitClicked = onContinue,
        onDismiss = onDismiss,
        submitText = stringResource(CommonStrings.action_continue),
        cancelText = stringResource(CommonStrings.action_cancel),
    )
}
