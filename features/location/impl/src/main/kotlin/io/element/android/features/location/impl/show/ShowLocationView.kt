/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.show

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.compound.tokens.generated.TypographyTokens
import io.element.android.features.location.api.ShowLocationMode
import io.element.android.features.location.impl.common.MapDefaults
import io.element.android.features.location.impl.common.PermissionDeniedDialog
import io.element.android.features.location.impl.common.PermissionRationaleDialog
import io.element.android.features.location.impl.common.ui.LocationFloatingActionButton
import io.element.android.features.location.impl.common.ui.MapBottomSheetScaffold
import io.element.android.features.location.impl.common.ui.MapProjected
import io.element.android.features.location.impl.common.ui.UserLocationPuck
import io.element.android.libraries.designsystem.components.LocationPinMarker
import io.element.android.libraries.designsystem.components.PinVariant
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.ui.strings.CommonStrings
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.location.DesiredAccuracy
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberNullLocationProvider
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.spatialk.geojson.Position
import kotlin.time.Duration.Companion.minutes

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

    val initialPosition = when (val mode = state.mode) {
        is ShowLocationMode.Static -> CameraPosition(
            target = Position(latitude = mode.location.lat, longitude = mode.location.lon),
            zoom = MapDefaults.DEFAULT_ZOOM
        )
        ShowLocationMode.Live -> CameraPosition(
            zoom = MapDefaults.DEFAULT_ZOOM
        )
    }
    val cameraState = rememberCameraState(firstPosition = initialPosition)
    val locationProvider = if (state.hasLocationPermission) {
        rememberDefaultLocationProvider(
            updateInterval = 1.minutes,
            desiredAccuracy = DesiredAccuracy.Balanced,
            minDistanceMeters = 50.0,
        )
    } else {
        rememberNullLocationProvider()
    }
    val userLocationState = rememberUserLocationState(locationProvider)
    LaunchedEffect(cameraState.isCameraMoving) {
        if (cameraState.moveReason == CameraMoveReason.GESTURE) {
            state.eventSink(ShowLocationEvents.TrackMyLocation(false))
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(skipHiddenState = false, initialValue = SheetValue.Hidden)
    )
    MapBottomSheetScaffold(
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
        mapContent = {
            UserLocationPuck(
                cameraState = cameraState,
                locationState = userLocationState,
                trackUserLocation = state.isTrackMyLocation
            )
        },
        overlayContent = {
            when (val mode = state.mode) {
                is ShowLocationMode.Static -> {
                    val pinVariant = if (mode.assetType == AssetType.PIN) {
                        PinVariant.PinnedLocation
                    } else {
                        PinVariant.UserLocation(
                            avatarData = AvatarData(mode.senderId.value, mode.senderName, mode.senderAvatarUrl, AvatarSize.UserListItem),
                            isLive = false
                        )
                    }
                    val position = Position(
                        latitude = mode.location.lat,
                        longitude = mode.location.lon
                    )
                    MapProjected(target = position, cameraState = cameraState) {
                        LocationPinMarker(variant = pinVariant)
                    }
                }
                ShowLocationMode.Live -> {
                    // TODO: Show pins for all active live location sharers
                }
            }


            LocationFloatingActionButton(
                isMapCenteredOnUser = state.isTrackMyLocation,
                onClick = { state.eventSink(ShowLocationEvents.TrackMyLocation(true)) },
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
