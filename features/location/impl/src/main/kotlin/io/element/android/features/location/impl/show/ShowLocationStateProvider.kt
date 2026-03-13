/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.show

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.location.api.Location
import io.element.android.features.location.impl.common.ui.LocationConstraintsDialogState
import io.element.android.features.location.impl.common.ui.LocationMarkerData
import io.element.android.libraries.designsystem.components.PinVariant
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.location.AssetType
import kotlinx.collections.immutable.toImmutableList

private const val APP_NAME = "ApplicationName"

class ShowLocationStateProvider : PreviewParameterProvider<ShowLocationState> {
    override val values: Sequence<ShowLocationState>
        get() = sequenceOf(
            aShowLocationState(),
            aShowLocationState(
                constraintsDialogState = LocationConstraintsDialogState.PermissionDenied,
            ),
            aShowLocationState(
                constraintsDialogState = LocationConstraintsDialogState.PermissionRationale,
            ),
            aShowLocationState(
                constraintsDialogState = LocationConstraintsDialogState.LocationServiceDisabled,
                hasLocationPermission = true,
            ),
            aShowLocationState(
                hasLocationPermission = true,
            ),
            aShowLocationState(
                hasLocationPermission = true,
                isTrackMyLocation = true,
            ),
        )
}

private val defaultLocation = Location(1.23, 2.34, 4f)
private val defaultSenderId = UserId("@alice:matrix.org")
private const val defaultSenderName = "Alice"

fun aShowLocationState(
    constraintsDialogState: LocationConstraintsDialogState = LocationConstraintsDialogState.None,
    markers: List<LocationMarkerData> = listOf(aLocationMarkerData()),
    locationShares: List<LocationShareItem> = listOf(aLocationShareItem()),
    hasLocationPermission: Boolean = false,
    isTrackMyLocation: Boolean = false,
    appName: String = APP_NAME,
    eventSink: (ShowLocationEvent) -> Unit = {},
): ShowLocationState {
    return ShowLocationState(
        dialogState = constraintsDialogState,
        markers = markers.toImmutableList(),
        locationShares = locationShares.toImmutableList(),
        hasLocationPermission = hasLocationPermission,
        isTrackMyLocation = isTrackMyLocation,
        appName = appName,
        eventSink = eventSink,
    )
}

fun aLocationMarkerData(
    id: String = defaultSenderId.value,
    location: Location = defaultLocation,
    variant: PinVariant = PinVariant.UserLocation(
        avatarData = AvatarData(
            id = defaultSenderId.value,
            name = defaultSenderName,
            url = null,
            size = AvatarSize.LocationPin,
        ),
        isLive = false,
    ),
) = LocationMarkerData(
    id = id,
    location = location,
    variant = variant,
)

fun aLocationShareItem(
    userId: UserId = defaultSenderId,
    displayName: String = defaultSenderName,
    avatarData: AvatarData = AvatarData(
        id = defaultSenderId.value,
        name = defaultSenderName,
        url = null,
        size = AvatarSize.UserListItem,
    ),
    formattedTimestamp: String = "Shared 1 min ago",
    location: Location = defaultLocation,
    isLive: Boolean = false,
    assetType: AssetType? = null,
) = LocationShareItem(
    userId = userId,
    displayName = displayName,
    avatarData = avatarData,
    formattedTimestamp = formattedTimestamp,
    location = location,
    isLive = isLive,
    assetType = assetType,
)
