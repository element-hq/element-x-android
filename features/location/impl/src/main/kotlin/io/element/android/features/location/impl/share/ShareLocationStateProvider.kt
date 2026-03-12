/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.share

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.location.impl.common.ui.LocationConstraintsDialogState
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser

private const val APP_NAME = "ApplicationName"

class ShareLocationStateProvider : PreviewParameterProvider<ShareLocationState> {
    override val values: Sequence<ShareLocationState>
        get() = sequenceOf(
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.None,
                trackUserPosition = false,
                hasLocationPermission = false,
            ),
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.PermissionDenied),
                trackUserPosition = false,
                hasLocationPermission = false,
            ),
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.PermissionRationale),
                trackUserPosition = false,
                hasLocationPermission = false,
            ),
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.LocationServiceDisabled),
                trackUserPosition = false,
                hasLocationPermission = true,
            ),
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.None,
                trackUserPosition = false,
                hasLocationPermission = true,
            ),
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.None,
                trackUserPosition = true,
                hasLocationPermission = true,
            ),
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.LiveLocationDuration,
                trackUserPosition = true,
                hasLocationPermission = true,
                canShareLiveLocation = true,
            ),
        )
}

private fun aShareLocationState(
    currentUser: MatrixUser = MatrixUser(UserId("@user:matrix.org")),
    dialogState: ShareLocationState.Dialog,
    trackUserPosition: Boolean,
    hasLocationPermission: Boolean,
    canShareLiveLocation: Boolean = false,
): ShareLocationState {
    return ShareLocationState(
        currentUser = currentUser,
        dialogState = dialogState,
        trackUserLocation = trackUserPosition,
        hasLocationPermission = hasLocationPermission,
        canShareLiveLocation = canShareLiveLocation,
        appName = APP_NAME,
        eventSink = {}
    )
}
