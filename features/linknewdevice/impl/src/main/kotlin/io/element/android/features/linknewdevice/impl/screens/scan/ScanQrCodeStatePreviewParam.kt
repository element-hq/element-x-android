/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.scan

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction

open class ScanQrCodeStatePreviewParam : PreviewParameterProvider<ScanQrCodeState> {
    override val values: Sequence<ScanQrCodeState>
        get() = sequenceOf(
            aScanQrCodeState(),
            aScanQrCodeState(scanAction = AsyncAction.Loading),
            aScanQrCodeState(scanAction = AsyncAction.Success(Unit)),
            aScanQrCodeState(scanAction = AsyncAction.Failure(Exception("Scan failed"))),
        )
}

fun aScanQrCodeState(
    scanAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (ScanQrCodeEvent) -> Unit = {},
) = ScanQrCodeState(
    scanAction = scanAction,
    eventSink = eventSink
)
