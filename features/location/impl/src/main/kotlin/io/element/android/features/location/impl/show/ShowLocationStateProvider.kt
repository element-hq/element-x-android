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
import io.element.android.features.location.api.ShowLocationMode
import io.element.android.features.location.impl.common.ui.LocationMarkerData
import io.element.android.libraries.designsystem.components.PinVariant
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.location.AssetType

private const val APP_NAME = "ApplicationName"

class ShowLocationStateProvider : PreviewParameterProvider<ShowLocationState> {
    override val values: Sequence<ShowLocationState>
        get() = sequenceOf(
            aShowLocationState(),
            aShowLocationState(
                permissionDialog = ShowLocationState.Dialog.PermissionDenied,
            ),
            aShowLocationState(
                permissionDialog = ShowLocationState.Dialog.PermissionRationale,
            ),
            aShowLocationState(
                hasLocationPermission = true,
            ),
            aShowLocationState(
                hasLocationPermission = true,
                isTrackMyLocation = true,
            ),
            aShowLocationState(
                mode = aStaticLocationMode(senderName = "My favourite place!"),
            ),
            aShowLocationState(
                mode = aStaticLocationMode(
                    senderName = "For some reason I decided to write a small essay that wraps at just two lines!"
                ),
            ),
            aShowLocationState(
                mode = ShowLocationMode.Live,
            ),
        )
}

fun aShowLocationState(
    permissionDialog: ShowLocationState.Dialog = ShowLocationState.Dialog.None,
    mode: ShowLocationMode = aStaticLocationMode(),
    markers: List<LocationMarkerData>? = null,
    locationSharers: List<LocationShareItem>? = null,
    hasLocationPermission: Boolean = false,
    isTrackMyLocation: Boolean = false,
    appName: String = APP_NAME,
    eventSink: (ShowLocationEvents) -> Unit = {},
): ShowLocationState {
    val effectiveMarkers = markers ?: when (mode) {
        is ShowLocationMode.Static -> listOf(
            LocationMarkerData(
                id = mode.senderId.value,
                location = mode.location,
                variant = if (mode.assetType == AssetType.PIN) {
                    PinVariant.PinnedLocation
                } else {
                    PinVariant.UserLocation(
                        avatarData = AvatarData(
                            id = mode.senderId.value,
                            name = mode.senderName,
                            url = mode.senderAvatarUrl,
                            size = AvatarSize.UserListItem,
                        ),
                        isLive = true,
                    )
                }
            )
        )
        ShowLocationMode.Live -> emptyList()
    }
    val effectiveLocationSharers = locationSharers ?: when (mode) {
        is ShowLocationMode.Static -> listOf(
            LocationShareItem(
                userId = mode.senderId,
                displayName = mode.senderName,
                avatarData = AvatarData(
                    id = mode.senderId.value,
                    name = mode.senderName,
                    url = mode.senderAvatarUrl,
                    size = AvatarSize.UserListItem,
                ),
                formattedTimestamp = "Shared 1 min ago",
                isLive = false,
                assetType = mode.assetType,
                location = mode.location,
            )
        )
        ShowLocationMode.Live -> emptyList()
    }
    return ShowLocationState(
        permissionDialog = permissionDialog,
        mode = mode,
        markers = effectiveMarkers,
        locationShares = effectiveLocationSharers,
        hasLocationPermission = hasLocationPermission,
        isTrackMyLocation = isTrackMyLocation,
        appName = appName,
        eventSink = eventSink,
    )
}

fun aStaticLocationMode(
    location: Location = Location(1.23, 2.34, 4f),
    senderName: String = "Alice",
    senderId: UserId = UserId("@alice:matrix.org"),
    senderAvatarUrl: String? = null,
    timestamp: Long = System.currentTimeMillis(),
    assetType: AssetType? = null,
) = ShowLocationMode.Static(
    location = location,
    senderName = senderName,
    senderId = senderId,
    senderAvatarUrl = senderAvatarUrl,
    timestamp = timestamp,
    assetType = assetType,
)
