/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package io.element.android.features.location.impl.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.location.api.Location
import io.element.android.features.location.api.internal.centerBottomEdge
import io.element.android.features.location.impl.R
import io.element.android.features.location.impl.common.MapDefaults
import io.element.android.features.location.impl.common.ui.LocationConstraintsDialog
import io.element.android.features.location.impl.common.ui.LocationFloatingActionButton
import io.element.android.features.location.impl.common.ui.MapBottomSheetScaffold
import io.element.android.features.location.impl.common.ui.UserLocationPuck
import io.element.android.features.location.impl.common.ui.rememberUserLocationState
import io.element.android.libraries.androidutils.system.toast
import io.element.android.libraries.designsystem.components.LocationPin
import io.element.android.libraries.designsystem.components.PinVariant
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ListDialog
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.list.RadioButtonListItem
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.location.UserLocationState
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareLocationView(
    state: ShareLocationState,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    when (val dialogState = state.dialogState) {
        ShareLocationState.Dialog.None -> Unit
        is ShareLocationState.Dialog.Constraints -> LocationConstraintsDialog(
            state = dialogState.state,
            appName = state.appName,
            onRequestPermissions = { state.eventSink(ShareLocationEvent.RequestPermissions) },
            onOpenAppSettings = { state.eventSink(ShareLocationEvent.OpenAppSettings) },
            onOpenLocationSettings = { state.eventSink(ShareLocationEvent.OpenLocationSettings) },
            onDismiss = { state.eventSink(ShareLocationEvent.DismissDialog) },
        )
        is ShareLocationState.Dialog.LiveLocationDurations -> LiveLocationDurationDialog(
            durations = dialogState.durations,
            onSelectDuration = { duration ->
                state.eventSink(ShareLocationEvent.StartLiveLocationShare(duration))
                context.toast("Not implemented yet!")
                navigateUp()
            },
            onDismiss = { state.eventSink(ShareLocationEvent.DismissDialog) },
        )
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded)
    )
    val cameraState = rememberCameraState(firstPosition = MapDefaults.defaultCameraPosition)
    val userLocationState = rememberUserLocationState(state.hasLocationPermission)

    LaunchedEffect(cameraState.isCameraMoving) {
        if (cameraState.moveReason == CameraMoveReason.GESTURE) {
            state.eventSink(ShareLocationEvent.StopTrackingUserLocation)
        }
    }

    MapBottomSheetScaffold(
        cameraState = cameraState,
        modifier = modifier,
        scaffoldState = scaffoldState,
        sheetDragHandle = null,
        sheetSwipeEnabled = false,
        topBar = {
            TopAppBar(
                titleStr = stringResource(CommonStrings.screen_share_location_title),
                navigationIcon = {
                    BackButton(onClick = navigateUp)
                },
            )
        },
        sheetContent = {
            BottomSheetContent(
                cameraState = cameraState,
                state = state,
                userLocationState = userLocationState,
                navigateUp = navigateUp
            )
        },
        mapContent = {
            UserLocationPuck(
                cameraState = cameraState,
                locationState = userLocationState,
                trackUserLocation = state.trackUserLocation
            )
        },
        overlayContent = { sheetPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(sheetPadding)
            ) {
                val variant = if (state.trackUserLocation) {
                    PinVariant.UserLocation(isLive = false, avatarData = state.currentUser.getAvatarData(AvatarSize.LocationPin))
                } else {
                    PinVariant.PinnedLocation
                }
                LocationPin(
                    variant = variant,
                    modifier = Modifier.centerBottomEdge(this),
                )
            }
            LocationFloatingActionButton(
                isMapCenteredOnUser = state.trackUserLocation,
                onClick = { state.eventSink(ShareLocationEvent.StartTrackingUserLocation) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(all = 16.dp),
            )
        }
    )
}

@Composable
private fun BottomSheetContent(
    cameraState: CameraState,
    state: ShareLocationState,
    userLocationState: UserLocationState,
    navigateUp: () -> Unit,
) {
    Spacer(Modifier.height(20.dp))
    val userLocation = userLocationState.location
    if (state.trackUserLocation && userLocation != null) {
        ShareCurrentLocationItem {
            state.eventSink(
                ShareLocationEvent.ShareStaticLocation(
                    location = Location(
                        lat = userLocation.position.latitude,
                        lon = userLocation.position.longitude
                    ),
                    isPinned = false
                )
            )
            navigateUp()
        }
    } else {
        SharePinLocationItem(
            onClick = {
                val positionTarget = cameraState.position.target
                state.eventSink(
                    ShareLocationEvent.ShareStaticLocation(
                        location = Location(lat = positionTarget.latitude, lon = positionTarget.longitude),
                        isPinned = true
                    )
                )
                navigateUp()
            }
        )
    }
    if (state.canShareLiveLocation) {
        ShareLiveLocationItem {
            state.eventSink(ShareLocationEvent.ShowLiveLocationDurationPicker)
        }
    }
}

@Composable
private fun ShareCurrentLocationItem(
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(stringResource(CommonStrings.screen_share_my_location_action))
        },
        onClick = onClick,
        leadingContent = ListItemContent.Icon(
            iconSource = IconSource.Vector(CompoundIcons.LocationNavigatorCentred())
        )
    )
}

@Composable
private fun SharePinLocationItem(
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(stringResource(CommonStrings.screen_share_this_location_action))
        },
        onClick = onClick,
        leadingContent = ListItemContent.Icon(
            iconSource = IconSource.Vector(CompoundIcons.LocationNavigator())
        )
    )
}

@Composable
private fun ShareLiveLocationItem(
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(stringResource(CommonStrings.action_share_live_location))
        },
        onClick = onClick,
        leadingContent = ListItemContent.Icon(
            iconSource = IconSource.Vector(CompoundIcons.LocationPinSolid()),
            tintColor = ElementTheme.colors.iconAccentPrimary,
        )
    )
}

@Composable
private fun LiveLocationDurationDialog(
    durations: ImmutableList<LiveLocationDuration>,
    onSelectDuration: (Duration) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    ListDialog(
        title = stringResource(R.string.screen_share_location_live_location_duration_picker_title),
        submitText = stringResource(CommonStrings.action_continue),
        onSubmit = { onSelectDuration(durations[selectedIndex].duration) },
        onDismissRequest = onDismiss,
        applyPaddingToContents = false,
        verticalArrangement = Arrangement.Top
    ) {
        itemsIndexed(durations) { index, duration ->
            RadioButtonListItem(
                headline = duration.formatted,
                selected = index == selectedIndex,
                onSelect = { selectedIndex = index },
                compactLayout = true,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ShareLocationViewPreview(
    @PreviewParameter(ShareLocationStateProvider::class) state: ShareLocationState
) = ElementPreview {
    ShareLocationView(
        state = state,
        navigateUp = {},
    )
}
