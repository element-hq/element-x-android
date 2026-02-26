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
    hasLocationPermission: Boolean = false,
    isTrackMyLocation: Boolean = false,
    appName: String = APP_NAME,
    eventSink: (ShowLocationEvents) -> Unit = {},
) = ShowLocationState(
    permissionDialog = permissionDialog,
    mode = mode,
    hasLocationPermission = hasLocationPermission,
    isTrackMyLocation = isTrackMyLocation,
    appName = appName,
    eventSink = eventSink,
)

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
