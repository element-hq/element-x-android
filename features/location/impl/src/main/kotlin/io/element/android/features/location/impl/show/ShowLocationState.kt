/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.show

import io.element.android.features.location.api.Location
import io.element.android.features.location.api.ShowLocationMode
import io.element.android.features.location.impl.common.ui.LocationConstraintsDialogState
import io.element.android.features.location.impl.common.ui.LocationMarkerData
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.location.AssetType

data class ShowLocationState(
    val dialogState: LocationConstraintsDialogState,
    val mode: ShowLocationMode,
    val markers: List<LocationMarkerData>,
    val locationShares: List<LocationShareItem>,
    val hasLocationPermission: Boolean,
    val isTrackMyLocation: Boolean,
    val appName: String,
    val eventSink: (ShowLocationEvent) -> Unit,
) {
    val isSheetDraggable = locationShares.any { item -> item.isLive }
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
