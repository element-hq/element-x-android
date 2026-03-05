/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.show

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.location.api.Location
import io.element.android.features.location.api.ShowLocationMode
import io.element.android.features.location.impl.common.MapDefaults
import io.element.android.features.location.impl.common.PermissionDeniedDialog
import io.element.android.features.location.impl.common.PermissionRationaleDialog
import io.element.android.features.location.impl.common.ui.LocationFloatingActionButton
import io.element.android.features.location.impl.common.ui.LocationMarkerData
import io.element.android.features.location.impl.common.ui.LocationPinMarkers
import io.element.android.features.location.impl.common.ui.MapBottomSheetScaffold
import io.element.android.features.location.impl.common.ui.UserLocationPuck
import io.element.android.libraries.designsystem.components.PinVariant
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
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
            when (val mode = state.mode) {
                is ShowLocationMode.Static -> {
                    val pinVariant = if (mode.assetType == AssetType.PIN) {
                        PinVariant.PinnedLocation
                    } else {
                        PinVariant.UserLocation(
                            avatarData = AvatarData(mode.senderId.value, mode.senderName, mode.senderAvatarUrl, AvatarSize.UserListItem),
                            isLive = true
                        )
                    }
                    // Generate test markers around the original location
                    val testMarkers = remember {
                        buildList {
                            // Add the original marker
                            add(
                                LocationMarkerData(
                                    id = "original",
                                    location = mode.location,
                                    variant = pinVariant
                                )
                            )
                            // Generate 10 random points within 50 meters
                            val radiusInMeters = 50.0
                            val metersPerDegreeLat = 111_320.0
                            val metersPerDegreeLon = 111_320.0 * cos(Math.toRadians(mode.location.lat))
                            val variants = listOf(
                                PinVariant.StaleLocation,
                                PinVariant.UserLocation(AvatarData("@alice", "Alice", null, AvatarSize.TimelineSender), isLive = true),
                                PinVariant.UserLocation(AvatarData("@bob", "Bob", null, AvatarSize.TimelineSender), isLive = true),
                                PinVariant.UserLocation(AvatarData("@cassy", "Cassy", null, AvatarSize.TimelineSender), isLive = true),
                                PinVariant.UserLocation(AvatarData("@daisy", "Daisy", null, AvatarSize.TimelineSender), isLive = true),
                                PinVariant.UserLocation(AvatarData("@en", "G", null, AvatarSize.TimelineSender), isLive = true),
                                PinVariant.UserLocation(AvatarData("@f", "H", null, AvatarSize.TimelineSender), isLive = true),
                                PinVariant.UserLocation(AvatarData("@g", "I", null, AvatarSize.TimelineSender), isLive = true),
                                PinVariant.UserLocation(AvatarData("@h", "J", null, AvatarSize.TimelineSender), isLive = true),
                                PinVariant.UserLocation(AvatarData("@i", "K", null, AvatarSize.TimelineSender), isLive = true),
                            )
                            repeat(10) { index ->
                                // Random point in a circle using sqrt for uniform distribution
                                val angle = Random.nextDouble() * 2 * Math.PI
                                val distance = sqrt(Random.nextDouble()) * radiusInMeters
                                val latOffset = (distance * cos(angle)) / metersPerDegreeLat
                                val lonOffset = (distance * sin(angle)) / metersPerDegreeLon
                                add(
                                    LocationMarkerData(
                                        id = "test_$index",
                                        location = Location(
                                            lat = mode.location.lat + latOffset,
                                            lon = mode.location.lon + lonOffset
                                        ),
                                        variant = variants[index % (variants.size-1)]
                                    )
                                )
                            }
                        }
                    }
                    LocationPinMarkers(testMarkers)
                }
                ShowLocationMode.Live -> {
                    // TODO: Show pins for all active live location sharers
                }
            }

        },
        overlayContent = {
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
