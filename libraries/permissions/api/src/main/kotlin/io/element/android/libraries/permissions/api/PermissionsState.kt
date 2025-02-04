/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.permissions.api

data class PermissionsState(
    // For instance Manifest.permission.POST_NOTIFICATIONS
    val permission: String,
    val permissionGranted: Boolean,
    val shouldShowRationale: Boolean,
    val showDialog: Boolean,
    val permissionAlreadyAsked: Boolean,
    // If true, there is no need to ask again, the system dialog will not be displayed
    val permissionAlreadyDenied: Boolean,
    val eventSink: (PermissionsEvents) -> Unit
)
