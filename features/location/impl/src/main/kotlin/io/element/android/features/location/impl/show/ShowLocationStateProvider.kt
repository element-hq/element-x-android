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
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.location.AssetType
import kotlinx.collections.immutable.toImmutableList

class ShowLocationStateProvider : PreviewParameterProvider<ShowLocationState> {
    override val values: Sequence<ShowLocationState>
        get() = sequenceOf(
            aShowLocationState(),
            aShowLocationState(isLive = true),
            aShowLocationState(isLive = true, locationShares = emptyList()),
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

private const val APP_NAME = "ApplicationName"

fun aShowLocationState(
    isLive: Boolean = false,
    constraintsDialogState: LocationConstraintsDialogState = LocationConstraintsDialogState.None,
    locationShares: List<LocationShareItem> = listOf(aLocationShareItem(isLive = isLive)),
    focusedLocation: LocationShareItem? = locationShares.firstOrNull(),
    hasLocationPermission: Boolean = false,
    isTrackMyLocation: Boolean = false,
    appName: String = APP_NAME,
    eventSink: (ShowLocationEvent) -> Unit = {},
): ShowLocationState {
    return ShowLocationState(
        dialogState = constraintsDialogState,
        locationShares = locationShares.toImmutableList(),
        focusedLocation = focusedLocation,
        hasLocationPermission = hasLocationPermission,
        isTrackMyLocation = isTrackMyLocation,
        appName = appName,
        isLive = isLive,
        eventSink = eventSink,
    )
}

fun aLocationShareItem(
    userId: UserId = UserId("@alice:matrix.org"),
    displayName: String = "Alice",
    avatarData: AvatarData = AvatarData(
        id = userId.value,
        name = displayName,
        url = null,
        size = AvatarSize.UserListItem,
    ),
    isLive: Boolean = false,
    assetType: AssetType? = null,
    formattedTimestamp: String = "Shared 1 min ago",
    location: Location = Location(1.23, 2.34, 4f),
) = LocationShareItem(
    userId = userId,
    displayName = displayName,
    avatarData = avatarData,
    formattedTimestamp = formattedTimestamp,
    location = location,
    isLive = isLive,
    assetType = assetType,
)
