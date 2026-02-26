/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.share

data class ShareLocationState(
    val dialogState: Dialog,
    val trackUserLocation: Boolean,
    val hasLocationPermission: Boolean,
    val appName: String,
    val canShareLiveLocation: Boolean,
    val eventSink: (ShareLocationEvent) -> Unit,
) {
    sealed interface Dialog {
        data object None : Dialog
        data object PermissionRationale : Dialog
        data object PermissionDenied : Dialog
        data object LiveLocationDuration : Dialog
    }
}
