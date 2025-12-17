/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.show

import io.element.android.features.location.api.Location

data class ShowLocationState(
    val permissionDialog: Dialog,
    val location: Location,
    val description: String?,
    val hasLocationPermission: Boolean,
    val isTrackMyLocation: Boolean,
    val appName: String,
    val eventSink: (ShowLocationEvents) -> Unit,
) {
    sealed interface Dialog {
        data object None : Dialog
        data object PermissionRationale : Dialog
        data object PermissionDenied : Dialog
    }
}
