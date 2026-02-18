/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.share

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

private const val APP_NAME = "ApplicationName"

class ShareLocationStateProvider : PreviewParameterProvider<ShareLocationState> {
    override val values: Sequence<ShareLocationState>
        get() = sequenceOf(
            aShareLocationState(
                permissionDialog = ShareLocationState.Dialog.None,
                mode = ShareLocationState.Mode.PinLocation,
                hasLocationPermission = false,
            ),
            aShareLocationState(
                permissionDialog = ShareLocationState.Dialog.PermissionDenied,
                mode = ShareLocationState.Mode.PinLocation,
                hasLocationPermission = false,
            ),
            aShareLocationState(
                permissionDialog = ShareLocationState.Dialog.PermissionRationale,
                mode = ShareLocationState.Mode.PinLocation,
                hasLocationPermission = false,
            ),
            aShareLocationState(
                permissionDialog = ShareLocationState.Dialog.None,
                mode = ShareLocationState.Mode.PinLocation,
                hasLocationPermission = true,
            ),
            aShareLocationState(
                permissionDialog = ShareLocationState.Dialog.None,
                mode = ShareLocationState.Mode.SenderLocation,
                hasLocationPermission = true,
            ),
        )
}

private fun aShareLocationState(
    permissionDialog: ShareLocationState.Dialog,
    mode: ShareLocationState.Mode,
    hasLocationPermission: Boolean,
    canShareLiveLocation: Boolean = false,
): ShareLocationState {
    return ShareLocationState(
        permissionDialog = permissionDialog,
        mode = mode,
        hasLocationPermission = hasLocationPermission,
        canShareLiveLocation = canShareLiveLocation,
        appName = APP_NAME,
        eventSink = {}
    )
}
