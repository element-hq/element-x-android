/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.send

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

private const val APP_NAME = "ApplicationName"

class SendLocationStateProvider : PreviewParameterProvider<SendLocationState> {
    override val values: Sequence<SendLocationState>
        get() = sequenceOf(
            aSendLocationState(
                permissionDialog = SendLocationState.Dialog.None,
                mode = SendLocationState.Mode.PinLocation,
                hasLocationPermission = false,
            ),
            aSendLocationState(
                permissionDialog = SendLocationState.Dialog.PermissionDenied,
                mode = SendLocationState.Mode.PinLocation,
                hasLocationPermission = false,
            ),
            aSendLocationState(
                permissionDialog = SendLocationState.Dialog.PermissionRationale,
                mode = SendLocationState.Mode.PinLocation,
                hasLocationPermission = false,
            ),
            aSendLocationState(
                permissionDialog = SendLocationState.Dialog.None,
                mode = SendLocationState.Mode.PinLocation,
                hasLocationPermission = true,
            ),
            aSendLocationState(
                permissionDialog = SendLocationState.Dialog.None,
                mode = SendLocationState.Mode.SenderLocation,
                hasLocationPermission = true,
            ),
        )
}

private fun aSendLocationState(
    permissionDialog: SendLocationState.Dialog,
    mode: SendLocationState.Mode,
    hasLocationPermission: Boolean,
): SendLocationState {
    return SendLocationState(
        permissionDialog = permissionDialog,
        mode = mode,
        hasLocationPermission = hasLocationPermission,
        appName = APP_NAME,
        eventSink = {}
    )
}
