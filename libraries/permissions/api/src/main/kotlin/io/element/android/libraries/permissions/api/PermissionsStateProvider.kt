/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.permissions.api

import android.Manifest
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class PermissionsStateProvider : PreviewParameterProvider<PermissionsState> {
    override val values: Sequence<PermissionsState>
        get() = sequenceOf(
            aPermissionsState(showDialog = true, permission = Manifest.permission.POST_NOTIFICATIONS),
            aPermissionsState(showDialog = true, permission = Manifest.permission.CAMERA),
            aPermissionsState(showDialog = true, permission = Manifest.permission.RECORD_AUDIO),
            aPermissionsState(showDialog = true, permission = Manifest.permission.INTERNET),
        )
}

fun aPermissionsState(
    showDialog: Boolean,
    permission: String = Manifest.permission.POST_NOTIFICATIONS,
    permissionGranted: Boolean = false,
) = PermissionsState(
    permission = permission,
    permissionGranted = permissionGranted,
    shouldShowRationale = false,
    showDialog = showDialog,
    permissionAlreadyAsked = false,
    permissionAlreadyDenied = false,
    eventSink = {}
)
