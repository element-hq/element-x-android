/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.show

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.compound.tokens.generated.TypographyTokens
import io.element.android.features.location.api.internal.rememberTileStyleUrl
import io.element.android.features.location.impl.common.MapDefaults
import io.element.android.features.location.impl.common.PermissionDeniedDialog
import io.element.android.features.location.impl.common.PermissionRationaleDialog
import io.element.android.features.location.impl.common.ui.LocationFloatingActionButton
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.maplibre.compose.CameraMode
import io.element.android.libraries.maplibre.compose.CameraMoveStartedReason
import io.element.android.libraries.maplibre.compose.IconAnchor
import io.element.android.libraries.maplibre.compose.MapLibreMap
import io.element.android.libraries.maplibre.compose.Symbol
import io.element.android.libraries.maplibre.compose.rememberCameraPositionState
import io.element.android.libraries.maplibre.compose.rememberSymbolState
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.toImmutableMap
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowLocationView(
    state: ShowLocationState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state.permissionDialog) {
        ShowLocationState.Dialog.None -> Unit
        ShowLocationState.Dialog.PermissionDenied -> PermissionDeniedDialog(
            onContinue = { state.eventSink(ShowLocationEvents.OpenAppSettings) },
            onDismiss = { state.eventSink(ShowLocationEvents.DismissDialog) },
            appName = state.appName,
        )
        ShowLocationState.Dialog.PermissionRationale -> PermissionRationaleDialog(
            onContinue = { state.eventSink(ShowLocationEvents.RequestPermissions) },
            onDismiss = { state.eventSink(ShowLocationEvents.DismissDialog) },
            appName = state.appName,
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.Builder()
            .target(LatLng(state.location.lat, state.location.lon))
            .zoom(MapDefaults.DEFAULT_ZOOM)
            .build()
    }

    LaunchedEffect(state.isTrackMyLocation) {
        when (state.isTrackMyLocation) {
            false -> cameraPositionState.cameraMode = CameraMode.NONE
            true -> {
                cameraPositionState.position = CameraPosition.Builder()
                    .zoom(MapDefaults.DEFAULT_ZOOM)
                    .build()
                cameraPositionState.cameraMode = CameraMode.TRACKING
            }
        }
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
            state.eventSink(ShowLocationEvents.TrackMyLocation(false))
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                titleStr = stringResource(CommonStrings.screen_view_location_title),
                navigationIcon = {
                    BackButton(
                        onClick = onBackClick,
                    )
                },
                actions = {
                    IconButton(
                        onClick = { state.eventSink(ShowLocationEvents.Share) }
                    ) {
                        Icon(
                            imageVector = CompoundIcons.ShareAndroid(),
                            contentDescription = stringResource(CommonStrings.action_share),
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            LocationFloatingActionButton(
                isMapCenteredOnUser = state.isTrackMyLocation,
                onClick = { state.eventSink(ShowLocationEvents.TrackMyLocation(true)) },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .fillMaxSize(),
        ) {
            state.description?.let {
                Text(
                    text = it,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = TypographyTokens.fontBodyMdRegular,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                )
            }

            MapLibreMap(
                styleUri = rememberTileStyleUrl(),
                modifier = Modifier.fillMaxSize(),
                images = mapOf(PIN_ID to CommonDrawables.pin).toImmutableMap(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapDefaults.uiSettings,
                symbolManagerSettings = MapDefaults.symbolManagerSettings,
                locationSettings = MapDefaults.locationSettings.copy(
                    locationEnabled = state.hasLocationPermission,
                ),
            ) {
                Symbol(
                    iconId = PIN_ID,
                    state = rememberSymbolState(
                        position = LatLng(state.location.lat, state.location.lon)
                    ),
                    iconAnchor = IconAnchor.BOTTOM,
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ShowLocationViewPreview(@PreviewParameter(ShowLocationStateProvider::class) state: ShowLocationState) = ElementPreview {
    ShowLocationView(
        state = state,
        onBackClick = {},
    )
}

private const val PIN_ID = "pin"
