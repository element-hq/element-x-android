/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.intro

import android.Manifest
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.permissions.api.PermissionsState
import io.element.android.libraries.permissions.api.aPermissionsState

open class QrCodeIntroStateProvider : PreviewParameterProvider<QrCodeIntroState> {
    override val values: Sequence<QrCodeIntroState>
        get() = sequenceOf(
            aQrCodeIntroState(),
            aQrCodeIntroState(cameraPermissionState = aPermissionsState(showDialog = true, permission = Manifest.permission.CAMERA)),
            // Add other state here
        )
}

fun aQrCodeIntroState(
    appName: String = "AppName",
    desktopAppName: String = "Element",
    cameraPermissionState: PermissionsState = aPermissionsState(
        showDialog = false,
        permission = Manifest.permission.CAMERA,
    ),
    canContinue: Boolean = false,
    eventSink: (QrCodeIntroEvents) -> Unit = {},
) = QrCodeIntroState(
    appName = appName,
    desktopAppName = desktopAppName,
    cameraPermissionState = cameraPermissionState,
    canContinue = canContinue,
    eventSink = eventSink
)
