/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.scan

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData

data class QrCodeScanState(
    val isScanning: Boolean,
    val authenticationAction: AsyncAction<MatrixQrCodeLoginData>,
    val eventSink: (QrCodeScanEvents) -> Unit
)
