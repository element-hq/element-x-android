/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.login.impl.screens.qrcode.scan

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction

open class QrCodeScanStateProvider : PreviewParameterProvider<QrCodeScanState> {
    override val values: Sequence<QrCodeScanState>
        get() = sequenceOf(
            aQrCodeScanState(),
            aQrCodeScanState(isScanning = false, authenticationAction = AsyncAction.Loading),
            aQrCodeScanState(isScanning = false, authenticationAction = AsyncAction.Failure(Exception("Error"))),
            // Add other state here
        )
}

fun aQrCodeScanState(
    isScanning: Boolean = true,
    authenticationAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (QrCodeScanEvents) -> Unit = {},
) = QrCodeScanState(
    isScanning = isScanning,
    authenticationAction = authenticationAction,
    eventSink = eventSink
)
