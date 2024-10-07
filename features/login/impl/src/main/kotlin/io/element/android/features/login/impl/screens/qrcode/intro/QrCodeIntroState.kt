/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.intro

import io.element.android.libraries.permissions.api.PermissionsState

data class QrCodeIntroState(
    val appName: String,
    val desktopAppName: String,
    val cameraPermissionState: PermissionsState,
    val canContinue: Boolean,
    val eventSink: (QrCodeIntroEvents) -> Unit
)
