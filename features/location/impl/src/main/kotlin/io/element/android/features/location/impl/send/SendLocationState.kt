/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.send

data class SendLocationState(
    val permissionDialog: Dialog,
    val mode: Mode,
    val hasLocationPermission: Boolean,
    val appName: String,
    val eventSink: (SendLocationEvents) -> Unit,
) {
    sealed interface Mode {
        data object SenderLocation : Mode
        data object PinLocation : Mode
    }

    sealed interface Dialog {
        data object None : Dialog
        data object PermissionRationale : Dialog
        data object PermissionDenied : Dialog
    }
}
