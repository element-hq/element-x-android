/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.share

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser

private const val APP_NAME = "ApplicationName"

class ShareLocationStateProvider : PreviewParameterProvider<ShareLocationState> {
    override val values: Sequence<ShareLocationState>
        get() = sequenceOf(
            aShareLocationState(
                permissionDialog = ShareLocationState.Dialog.None,
                trackUserPosition = false,
                hasLocationPermission = false,
            ),
            aShareLocationState(
                permissionDialog = ShareLocationState.Dialog.PermissionDenied,
                trackUserPosition = false,
                hasLocationPermission = false,
            ),
            aShareLocationState(
                permissionDialog = ShareLocationState.Dialog.PermissionRationale,
                trackUserPosition = false,
                hasLocationPermission = false,
            ),
            aShareLocationState(
                permissionDialog = ShareLocationState.Dialog.LocationServiceDisabled,
                trackUserPosition = false,
                hasLocationPermission = true,
            ),
            aShareLocationState(
                permissionDialog = ShareLocationState.Dialog.None,
                trackUserPosition = false,
                hasLocationPermission = true,
            ),
            aShareLocationState(
                permissionDialog = ShareLocationState.Dialog.None,
                trackUserPosition = true,
                hasLocationPermission = true,
            ),
            aShareLocationState(
                permissionDialog = ShareLocationState.Dialog.LiveLocationDuration,
                trackUserPosition = true,
                hasLocationPermission = true,
                canShareLiveLocation = true,
            ),
        )
}

private fun aShareLocationState(
    currentUser: MatrixUser = MatrixUser(UserId("@user:matrix.org")),
    permissionDialog: ShareLocationState.Dialog,
    trackUserPosition: Boolean,
    hasLocationPermission: Boolean,
    canShareLiveLocation: Boolean = false,
): ShareLocationState {
    return ShareLocationState(
        currentUser = currentUser,
        dialogState = permissionDialog,
        trackUserLocation = trackUserPosition,
        hasLocationPermission = hasLocationPermission,
        canShareLiveLocation = canShareLiveLocation,
        appName = APP_NAME,
        eventSink = {}
    )
}
