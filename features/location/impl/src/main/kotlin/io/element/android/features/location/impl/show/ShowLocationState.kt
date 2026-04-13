/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.show

import io.element.android.features.location.api.Location
import io.element.android.features.location.impl.common.ui.LocationConstraintsDialogState
import io.element.android.features.location.impl.common.ui.LocationMarkerData
import io.element.android.libraries.designsystem.components.PinVariant
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.location.AssetType
import kotlinx.collections.immutable.ImmutableList

data class ShowLocationState(
    val isLive: Boolean,
    val dialogState: LocationConstraintsDialogState,
    val locationShares: ImmutableList<LocationShareItem>,
    val focusedLocation: LocationShareItem?,
    val hasLocationPermission: Boolean,
    val isTrackMyLocation: Boolean,
    val appName: String,
    val eventSink: (ShowLocationEvent) -> Unit,
) {
    val isSheetDraggable = isLive && locationShares.isNotEmpty()
}

data class LocationShareItem(
    val userId: UserId,
    val displayName: String,
    val avatarData: AvatarData,
    val formattedTimestamp: String,
    val location: Location,
    val isLive: Boolean,
    val assetType: AssetType?,
)

fun LocationShareItem.toMarkerData(): LocationMarkerData {
    val pinVariant = if (assetType == AssetType.PIN) {
        PinVariant.PinnedLocation
    } else {
        PinVariant.UserLocation(
            avatarData = avatarData,
            isLive = isLive,
        )
    }
    return LocationMarkerData(
        id = userId.value,
        location = location,
        variant = pinVariant,
    )
}
