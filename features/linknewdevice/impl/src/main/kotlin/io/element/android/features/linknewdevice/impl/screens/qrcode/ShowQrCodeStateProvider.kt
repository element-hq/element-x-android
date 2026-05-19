/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.qrcode

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData

class ShowQrCodeStateProvider : PreviewParameterProvider<ShowQrCodeState> {
    override val values: Sequence<ShowQrCodeState>
        get() = sequenceOf(
            aShowQrCodeState(),
            aShowQrCodeState(
                data1 = AsyncData.Loading(),
            ),
            aShowQrCodeState(
                data1 = AsyncData.Success("DATA"),
                data2 = AsyncData.Success("DATA2"),
                dataToRender = 2,
            ),
        )
}

private fun aShowQrCodeState(
    data1: AsyncData<String> = AsyncData.Success("DATA"),
    data2: AsyncData<String> = AsyncData.Uninitialized,
    dataToRender: Int = 1,
) = ShowQrCodeState(
    data1 = data1,
    data2 = data2,
    dataToRender = dataToRender,
)
